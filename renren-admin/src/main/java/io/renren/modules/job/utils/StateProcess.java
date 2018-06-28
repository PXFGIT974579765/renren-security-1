package io.renren.modules.job.utils;

import io.renren.common.utils.Constant;
import io.renren.modules.job.entity.ScheduleJobEntity;
import io.renren.modules.job.entity.ScheduleJobLogEntity;
import io.renren.modules.job.service.RestTemplateService;
import io.renren.modules.job.service.ScheduleJobLogService;
import io.renren.modules.job.service.ScheduleJobService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by pxf on 2018-6-27
 */
public class StateProcess {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ScheduleJobEntity scheduleJob;
    private ScheduleJobService scheduleJobService;
    private RestTemplateService restTemplate;
    private ScheduleJobLogService scheduleJobLogService;
    private ScheduleJobLogEntity log;
    private Long startTime;
    private Long times;
    private ExecutorService service = new ScheduledThreadPoolExecutor(2,new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
    //阻塞超时时间
    private Long blockTimeout=1000*10*1L;
    //阻塞时时隔多少秒再进行查询
    private Long blockSleepIntervalTime=2*1000L;

    public StateProcess(ScheduleJobEntity scheduleJob, ScheduleJobService scheduleJobService, RestTemplateService restTemplate, ScheduleJobLogService scheduleJobLogService, ScheduleJobLogEntity log) {

        this.scheduleJob = scheduleJob;
        this.scheduleJobService = scheduleJobService;
        this.restTemplate = restTemplate;
        this.scheduleJobLogService = scheduleJobLogService;
        this.log = log;
    }

    public void initLog() {
        log.setJobId(scheduleJob.getJobId());
        log.setBeanName(scheduleJob.getBeanName());
        log.setMethodName(scheduleJob.getMethodName());
        log.setParams(scheduleJob.getParams());
        log.setCreateTime(new Date());
        log.setJobName(scheduleJob.getJobName());
        log.setMode(scheduleJob.getMode());
        //任务开始时间
        startTime = System.currentTimeMillis();
        times=0L;
        logger.info("查询执行状态任务准备执行，任务ID：" + scheduleJob.getJobId());
    }

    public  void apiProcess(CallBack callBack){
        try {
            //restful-api
            if(StringUtils.isEmpty(scheduleJob.getQueryApi())){
                callBack.callBack(true);
                logger.error("任务执行失败，任务ID：" + scheduleJob.getJobId()+":未设置相关api");
                times = System.currentTimeMillis() - startTime;
                log.setTimes(times.intValue());
                //0 成功    1失败
                log.setStatus(1);
                log.setType(Constant.LogType.EXCUTE.getValue());
                log.setMessage("任务执行失败--->找不到对应的调度api");
                scheduleJobLogService.insert(log);
                scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                scheduleJobService.updateById(scheduleJob);
            }else{
                JSONObject result=restTemplate.postForObject(scheduleJob.getQueryApi(),null,JSONObject.class);
                if(result!=null&&result.getString("msg").equals(scheduleJob.getQuerySuccessValue())){
                    //0 成功    1失败
                    callBack.callBack(false);
                    log.setStatus(0);
                    times = System.currentTimeMillis() - startTime;
                    log.setTimes(times.intValue());
                    logger.info("执行状态任务完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
                    log.setType(Constant.LogType.EXCUTE.getValue());
                    log.setMessage("查询执行状态任务执行成功");
                    scheduleJobLogService.insert(log);
                    scheduleJob.setState(Constant.ScheduleStates.EXCUTE_SUCCESS.getValue());
                    scheduleJobService.updateById(scheduleJob);
                    //todo notify 子任务
                    String childs=scheduleJob.getChild();
                    if(!StringUtils.isEmpty(childs)){
                        scheduleJobService.notifyChildJob(childs.split(","));
                    }
                }else{
                    callBack.callBack(true);
                    logger.error("任务执行失败，任务ID：" + scheduleJob.getJobId()+"成功返回标志不对");
                    times = System.currentTimeMillis() - startTime;
                    log.setTimes(times.intValue());
                    //0 成功    1失败
                    log.setStatus(1);
                    log.setType(Constant.LogType.EXCUTE.getValue());
                    log.setMessage("任务执行失败");
                    scheduleJobLogService.insert(log);
                    scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                    scheduleJobService.updateById(scheduleJob);
                }
            }
        }catch (Exception e){
            callBack.callBack(true);
            logger.error("任务执行失败，任务ID：" );
            times = System.currentTimeMillis() - startTime;
            log.setTimes(times.intValue());
            //0 成功    1失败
            log.setStatus(1);
            log.setType(Constant.LogType.EXCUTE.getValue());
            log.setMessage("任务执行失败");
            log.setError(StringUtils.substring(e.toString(), 0, 2000));
            scheduleJobLogService.insert(log);
            scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
            scheduleJobService.updateById(scheduleJob);
        }


    }
    public  void classProcess(CallBack callBack){

    }
    public  void shellProcess(CallBack callBack){
        try {
            //shell脚本
            if(StringUtils.isEmpty(scheduleJob.getClientIp())){
                callBack.callBack(true);
                times = System.currentTimeMillis() - startTime;
                log.setTimes(times.intValue());
                //0 成功    1失败
                log.setStatus(1);
                log.setType(Constant.LogType.EXCUTE.getValue());
                log.setMessage("执行状态任务失败--->未设置客户端ip");
                scheduleJobLogService.insert(log);
                scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                scheduleJobService.updateById(scheduleJob);
            }else{
                HashMap<String,String> map=new HashMap<String,String>();
                map.put("shell",scheduleJob.getQueryShell());
                JSONObject result=restTemplate.postForObject(scheduleJob.getClientIp()+":"+Constant.Client.PORT.getValue()+"/client/api/query",map,JSONObject.class);
                //JSONObject result=restTemplate.postForObject(scheduleJob.getClientIp()+"/client/api/query",map,JSONObject.class);
                if(result!=null&&result.getString("code").equals("00000")){

                    if(result.getString("msg").contains(scheduleJob.getQuerySuccessValue())){
                        callBack.callBack(false);
                        times = System.currentTimeMillis() - startTime;
                        log.setTimes(times.intValue());
                        logger.info("任务执行完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
                        //0 成功    1失败
                        log.setStatus(0);
                        log.setType(Constant.LogType.EXCUTE.getValue());
                        log.setMessage("任务执行成功:返回值-->"+result.getString("msg"));
                        scheduleJobLogService.insert(log);
                        scheduleJob.setState(Constant.ScheduleStates.EXCUTE_SUCCESS.getValue());
                        scheduleJobService.updateById(scheduleJob);
                        //todo notify 子任务
                        String childs=scheduleJob.getChild();
                        if(!StringUtils.isEmpty(childs)){
                            scheduleJobService.notifyChildJob(childs.split(","));
                        }
                    }else{
                        callBack.callBack(true);
                        times = System.currentTimeMillis() - startTime;
                        log.setTimes(times.intValue());
                        logger.info("任务执行完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
                        //0 成功    1失败
                        log.setStatus(1);
                        log.setType(Constant.LogType.EXCUTE.getValue());
                        log.setMessage("任务执行失败");
                        log.setError(result.getString("msg"));
                        scheduleJobLogService.insert(log);
                        scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                        scheduleJobService.update(scheduleJob);
                    }

                }else{
                    callBack.callBack(true);
                    logger.error("任务执行失败，任务ID：" + scheduleJob.getJobId()+result.getString("msg"));
                    times = System.currentTimeMillis() - startTime;
                    log.setTimes(times.intValue());
                    //0 成功    1失败
                    log.setStatus(1);
                    log.setType(Constant.LogType.EXCUTE.getValue());
                    log.setMessage("任务执行失败");
                    scheduleJobLogService.insert(log);
                    scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                    scheduleJobService.updateById(scheduleJob);
                }

            }
        }catch (Exception e){
            callBack.callBack(true);
            logger.error("任务执行失败，任务ID：" );
            times = System.currentTimeMillis() - startTime;
            log.setTimes(times.intValue());
            //0 成功    1失败
            log.setStatus(1);
            log.setType(Constant.LogType.EXCUTE.getValue());
            log.setMessage("任务执行失败");
            log.setError(StringUtils.substring(e.toString(), 0, 2000));
            scheduleJobLogService.insert(log);
            scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
            scheduleJobService.updateById(scheduleJob);
        }

    }

    public interface CallBack{
        void callBack(boolean fail);
    }
}