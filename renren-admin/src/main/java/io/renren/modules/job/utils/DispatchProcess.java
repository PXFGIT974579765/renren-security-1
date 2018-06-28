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
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by pxf on 2018-6-27
 */
public class DispatchProcess {

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

    public DispatchProcess() {
    }

    public DispatchProcess(ScheduleJobEntity scheduleJob, ScheduleJobService scheduleJobService, RestTemplateService restTemplate, ScheduleJobLogService scheduleJobLogService, ScheduleJobLogEntity log) {

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
        logger.info("任务准备调度，任务ID：" + scheduleJob.getJobId());
    }

    public  void classProcess(CallBack callBack){
        //类名方法名参数
        try {
            if(!StringUtils.isEmpty(scheduleJob.getBeanName())
                    &&!StringUtils.isEmpty(scheduleJob.getMethodName())){

                ScheduleRunnable task = new ScheduleRunnable(scheduleJob.getBeanName(),
                        scheduleJob.getMethodName(), scheduleJob.getParams());
                Future<?> future = service.submit(task);
                future.get();
                //任务执行总时长
                times = System.currentTimeMillis() - startTime;
                log.setTimes(times.intValue());
                //0成功  1 失败
                log.setStatus(0);

                log.setMessage("任务调度执行成功");
                log.setType(Constant.LogType.DISPATCH.getValue());
                scheduleJobLogService.insert(log);
                logger.info("任务调度执行完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
                scheduleJob.setState(Constant.ScheduleStates.EXCUTE_SUCCESS.getValue());
                scheduleJobService.updateById(scheduleJob);
                //todo 执行child任务
                callBack.callBack(false);
                String childs=scheduleJob.getChild();
                if(!StringUtils.isEmpty(childs)){
                    scheduleJobService.notifyChildJob(childs.split(","));
                }
            }else{
                //任务执行总时长
                times = System.currentTimeMillis() - startTime;
                log.setTimes(times.intValue());
                logger.error("任务调度执行失败，任务ID：" + scheduleJob.getJobId()+":找不到对应的类名方法名");
                //0 成功    1失败
                log.setStatus(1);
                log.setType(Constant.LogType.DISPATCH.getValue());
                log.setMessage("任务调度执行失败--->找不到对应的类名方法名");
                scheduleJobLogService.insert(log);
                scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                scheduleJobService.updateById(scheduleJob);
                callBack.callBack(true);
            }

        } catch (Exception e) {
            logger.error("任务调度执行失败，任务ID：" + scheduleJob.getJobId(), e);
            //任务执行总时长
            times = System.currentTimeMillis() - startTime;
            log.setTimes(times.intValue());
            //0 成功    1失败
            log.setStatus(1);
            log.setType(Constant.LogType.DISPATCH.getValue());
            log.setMessage("任务调度执行失败");
            callBack.callBack(true);
            log.setError(StringUtils.substring(e.toString(), 0, 2000));
            scheduleJobLogService.insert(log);
            scheduleJob.setState(Constant.ScheduleStates.DISPATCH_FAILE.getValue());
            scheduleJobService.updateById(scheduleJob);
        }

    }
    public  void apiProcess(CallBack callBack){
        try {
            //restful-api
            if(StringUtils.isEmpty(scheduleJob.getDispatchApi())){
                logger.error("任务调度失败，任务ID：" + scheduleJob.getJobId()+":未设置相关api");
                times = System.currentTimeMillis() - startTime;
                log.setTimes(times.intValue());
                //0 成功    1失败
                log.setStatus(1);
                log.setType(Constant.LogType.DISPATCH.getValue());
                log.setMessage("任务执行失败--->找不到对应的调度api");
                scheduleJobLogService.insert(log);
                scheduleJob.setState(Constant.ScheduleStates.DISPATCH_FAILE.getValue());
                scheduleJobService.updateById(scheduleJob);
                callBack.callBack(true);
            }else{
                JSONObject result=restTemplate.postForObject(scheduleJob.getDispatchApi(),null,JSONObject.class);
                if(result!=null&&result.getString("code").equals(scheduleJob.getDispatchSuccessValue())){
                    //0 成功    1失败
                    log.setStatus(0);
                    log.setType(Constant.LogType.DISPATCH.getValue());
                    times = System.currentTimeMillis() - startTime;
                    log.setTimes(times.intValue());
                    //任务执行结果是否需要单独查询 0不需要 ，1需要
                    if(scheduleJob.getNeedQueryFlag()==0){
                        logger.info("任务调度执行完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
                        log.setMessage("任务调度执行成功");
                        scheduleJobLogService.insert(log);
                        scheduleJob.setState(Constant.ScheduleStates.EXCUTE_SUCCESS.getValue());
                        scheduleJobService.updateById(scheduleJob);
                        //todo 执行child任务
                        callBack.callBack(false);
                        String childs=scheduleJob.getChild();
                        if(!StringUtils.isEmpty(childs)){
                            scheduleJobService.notifyChildJob(childs.split(","));
                        }
                    }else{
                        log.setMessage("任务调度成功");
                        scheduleJobLogService.insert(log);
                        scheduleJob.setState(Constant.ScheduleStates.DISPATCH_SUCCESS.getValue());
                        scheduleJobService.updateById(scheduleJob);
                        //todo 写查询任务代码
                        callBack.callBack(false);
                        Long[] jobIds={scheduleJob.getJobId()};
                        scheduleJobService.createJobForQueryState(jobIds);

                    }
                }else{
                    times = System.currentTimeMillis() - startTime;
                    logger.error("任务调度执行失败，任务ID：" + scheduleJob.getJobId()+"  总共耗时：" + times + "毫秒");
                    log.setTimes(times.intValue());
                    //0 成功    1失败
                    log.setStatus(1);
                    log.setType(Constant.LogType.DISPATCH.getValue());
                    log.setMessage("任务调度执行失败");
                    callBack.callBack(true);
                    scheduleJobLogService.insert(log);
                    scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                    scheduleJobService.updateById(scheduleJob);
                }
            }
        }catch (Exception e){
            logger.error("任务调度执行失败，任务ID：" + scheduleJob.getJobId(), e);
            //任务执行总时长
            times = System.currentTimeMillis() - startTime;
            log.setTimes(times.intValue());
            //0 成功    1失败
            log.setStatus(1);
            log.setType(Constant.LogType.DISPATCH.getValue());
            log.setMessage("任务调度执行失败");
            callBack.callBack(true);
            log.setError(StringUtils.substring(e.toString(), 0, 2000));
            scheduleJobLogService.insert(log);
            scheduleJob.setState(Constant.ScheduleStates.DISPATCH_FAILE.getValue());
            scheduleJobService.updateById(scheduleJob);
        }

    }
    public  void shellProcess(CallBack callBack){
        try {
           //shell脚本
            if(StringUtils.isEmpty(scheduleJob.getClientIp())){
                times = System.currentTimeMillis() - startTime;
                log.setTimes(times.intValue());
                //0 成功    1失败
                log.setStatus(1);
                log.setType(Constant.LogType.DISPATCH.getValue());
                log.setMessage("任务调度执行失败--->找不到对应的调度api");
                callBack.callBack(true);
                scheduleJobLogService.insert(log);
                scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                scheduleJobService.updateById(scheduleJob);
            }else{
                HashMap<String,String> map=new HashMap<String,String>();
                map.put("shell",scheduleJob.getDispatchShell());
                JSONObject result=restTemplate.postForObject(scheduleJob.getClientIp()+":"+Constant.Client.PORT.getValue()+"/client/api/dispatch",map,JSONObject.class);
                //JSONObject result=restTemplate.postForObject(scheduleJob.getClientIp()+"/client/api/dispatch",map,JSONObject.class);
                if(result!=null&&result.getString("code").equals("00000")){
                    //0 成功    1失败
                    times = System.currentTimeMillis() - startTime;
                    log.setTimes(times.intValue());
                    log.setStatus(0);
                    log.setMessage(result.getString("msg"));
                    log.setType(Constant.LogType.DISPATCH.getValue());
                    //任务执行结果是否需要单独查询 0不需要 ，1需要
                    if(scheduleJob.getNeedQueryFlag()==0){
                        logger.info("任务调度执行完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
                        callBack.callBack(false);
                        //log.setMessage("任务调度执行成功");
                        scheduleJobLogService.insert(log);
                        scheduleJob.setState(Constant.ScheduleStates.EXCUTE_SUCCESS.getValue());
                        scheduleJobService.updateById(scheduleJob);
                        //todo 执行child任务
                        String childs=scheduleJob.getChild();
                        if(!StringUtils.isEmpty(childs)){
                            scheduleJobService.notifyChildJob(childs.split(","));
                        }
                    }else{
                        //log.setMessage("任务调度成功");
                        scheduleJobLogService.insert(log);
                        scheduleJob.setState(Constant.ScheduleStates.DISPATCH_SUCCESS.getValue());
                        Long[] jobids={scheduleJob.getJobId()};
                        scheduleJobService.updateById(scheduleJob);
                        //todo 写查询任务代码
                        callBack.callBack(false);
                        scheduleJobService.createJobForQueryState(jobids);
                    }
                }else{
                    logger.error("任务调度执行失败，任务ID：" + scheduleJob.getJobId()+":未设置相关api");
                    callBack.callBack(true);
                    times = System.currentTimeMillis() - startTime;
                    log.setTimes(times.intValue());
                    //0 成功    1失败
                    log.setStatus(1);
                    log.setError(result.getString("msg"));
                    log.setType(Constant.LogType.DISPATCH.getValue());
                    log.setMessage("任务调度失败");
                    scheduleJobLogService.insert(log);
                    scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
                    scheduleJobService.updateById(scheduleJob);
                }

            }
        }catch (Exception e){
            logger.error("任务调度执行失败，任务ID：" + scheduleJob.getJobId(), e);
            //任务执行总时长
            times = System.currentTimeMillis() - startTime;
            log.setTimes(times.intValue());
            //0 成功    1失败
            log.setStatus(1);
            log.setType(Constant.LogType.DISPATCH.getValue());
            log.setMessage("任务调度执行失败");
            callBack.callBack(true);
            log.setError(StringUtils.substring(e.toString(), 0, 2000));
            scheduleJobLogService.insert(log);
            scheduleJob.setState(Constant.ScheduleStates.DISPATCH_FAILE.getValue());
            scheduleJobService.updateById(scheduleJob);
        }

    }

    public interface CallBack{
        void callBack(boolean fail);
    }


}