package io.renren.modules.job.utils;

import io.renren.common.utils.Constant;
import io.renren.common.utils.SpringContextUtils;
import io.renren.modules.job.entity.ScheduleJobEntity;
import io.renren.modules.job.entity.ScheduleJobLogEntity;
import io.renren.modules.job.service.RestTemplateService;
import io.renren.modules.job.service.ScheduleJobLogService;
import io.renren.modules.job.service.ScheduleJobService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by pxf on 2018-6-14
 *
 * 执行任务状态查询job
 */
public class ScheduleStateJob extends QuartzJobBean {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ExecutorService service = Executors.newSingleThreadExecutor();
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        ScheduleJobEntity scheduleJob=(ScheduleJobEntity)context.getMergedJobDataMap().get(ScheduleJobEntity.JOB_PARAM_KEY);
        String blockIds=scheduleJob.getBlockJobIds();
        ScheduleJobService scheduleJobService = (ScheduleJobService) SpringContextUtils.getBean("scheduleJobService");
        RestTemplateService restTemplate = (RestTemplateService) SpringContextUtils.getBean("restTemplateService");
        //获取spring bean
        ScheduleJobLogService scheduleJobLogService = (ScheduleJobLogService) SpringContextUtils.getBean("scheduleJobLogService");
        //数据库保存执行记录
        ScheduleJobLogEntity log = new ScheduleJobLogEntity();
        log.setJobId(scheduleJob.getJobId());
        log.setBeanName(scheduleJob.getBeanName());
        log.setMethodName(scheduleJob.getMethodName());
        log.setParams(scheduleJob.getParams());
        log.setCreateTime(new Date());
        //任务开始时间
        long startTime = System.currentTimeMillis();
        long times=0L;
        //执行任务
        logger.info("查询执行状态任务准备执行，任务ID：" + scheduleJob.getJobId());
       if(scheduleJob.getMode()==Constant.ScheduleMode.API.getValue()){
            //restful-api
            if(StringUtils.isEmpty(scheduleJob.getQueryApi())){
                logger.error("任务执行失败，任务ID：" + scheduleJob.getJobId()+":未设置相关api");
                times = System.currentTimeMillis() - startTime;
                log.setTimes((int)times);
                //0 成功    1失败
                log.setStatus(1);
                log.setType(Constant.LogType.EXCUTE.getValue());
                log.setMessage("任务执行失败--->找不到对应的调度api");
                scheduleJobLogService.insert(log);
                scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                scheduleJobService.update(scheduleJob);
            }else{
                JSONObject result=restTemplate.postForObject(scheduleJob.getQueryApi(),null,JSONObject.class);
                if(result!=null&&result.getString("code").equals(scheduleJob.getQuerySuccessValue())){
                    //0 成功    1失败
                    log.setStatus(0);
                    times = System.currentTimeMillis() - startTime;
                    log.setTimes((int)times);
                    logger.info("执行状态任务完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
                    log.setType(Constant.LogType.EXCUTE.getValue());
                    log.setMessage("查询执行状态任务执行成功");
                    scheduleJobLogService.insert(log);
                    scheduleJob.setState(Constant.ScheduleStates.EXCUTE_SUCCESS.getValue());
                    scheduleJobService.update(scheduleJob);
                }else{
                    logger.error("任务执行失败，任务ID：" + scheduleJob.getJobId()+":未设置相关api");
                    times = System.currentTimeMillis() - startTime;
                    log.setTimes((int)times);
                    //0 成功    1失败
                    log.setStatus(1);
                    log.setType(Constant.LogType.EXCUTE.getValue());
                    log.setMessage("任务执行失败");
                    scheduleJobLogService.insert(log);
                    scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                    scheduleJobService.update(scheduleJob);
                }
            }

        }else{
            //shell脚本
            if(StringUtils.isEmpty(scheduleJob.getClientIp())){
                times = System.currentTimeMillis() - startTime;
                log.setTimes((int)times);
                //0 成功    1失败
                log.setStatus(1);
                log.setType(Constant.LogType.EXCUTE.getValue());
                log.setMessage("执行状态任务失败--->未设置客户端ip");
                scheduleJobLogService.insert(log);
                scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                scheduleJobService.update(scheduleJob);
            }else{
                HashMap<String,String> map=new HashMap<String,String>();
                map.put("shell",scheduleJob.getQueryShell());
                //JSONObject result=restTemplate.postForObject(scheduleJob.getClientIp()+":"+Constant.Client.PORT.getValue()+"/client/api/query",map,JSONObject.class);
                JSONObject result=restTemplate.postForObject(scheduleJob.getClientIp()+"/client/api/query",map,JSONObject.class);
                //todo 这个地方的放回类型多种多样，再优化
                if(result!=null&&result.getString("code").equals("00000")){

                    if(result.getString("msg").contains(scheduleJob.getQuerySuccessValue())){
                        times = System.currentTimeMillis() - startTime;
                        log.setTimes((int)times);
                        logger.info("任务执行完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
                        //0 成功    1失败
                        log.setStatus(0);
                        log.setType(Constant.LogType.EXCUTE.getValue());
                        log.setMessage("任务执行成功:返回值-->"+result.getString("msg"));
                        scheduleJobLogService.insert(log);
                        scheduleJob.setState(Constant.ScheduleStates.EXCUTE_SUCCESS.getValue());
                        scheduleJobService.update(scheduleJob);
                    }else{
                        times = System.currentTimeMillis() - startTime;
                        log.setTimes((int)times);
                        logger.info("任务执行完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
                        //0 成功    1失败
                        log.setStatus(1);
                        log.setType(Constant.LogType.EXCUTE.getValue());
                        log.setMessage("任务执行失败:返回值-->"+result.getString("msg"));
                        scheduleJobLogService.insert(log);
                        scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                        scheduleJobService.update(scheduleJob);
                    }

                }else{
                    logger.error("任务执行失败，任务ID：" + scheduleJob.getJobId()+result.getString("msg"));
                    times = System.currentTimeMillis() - startTime;
                    log.setTimes((int)times);
                    //0 成功    1失败
                    log.setStatus(1);
                    log.setType(Constant.LogType.EXCUTE.getValue());
                    log.setMessage("任务执行失败");
                    scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                    scheduleJobService.update(scheduleJob);
                }

            }

        }
    }
}