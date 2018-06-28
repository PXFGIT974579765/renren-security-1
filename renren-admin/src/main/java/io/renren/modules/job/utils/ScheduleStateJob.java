package io.renren.modules.job.utils;

import io.renren.common.utils.Constant;
import io.renren.common.utils.SpringContextUtils;
import io.renren.modules.job.entity.ScheduleJobEntity;
import io.renren.modules.job.entity.ScheduleJobLogEntity;
import io.renren.modules.job.service.RestTemplateService;
import io.renren.modules.job.service.ScheduleJobLogService;
import io.renren.modules.job.service.ScheduleJobService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by pxf on 2018-6-14
 *
 * 执行任务状态查询job
 */
public class ScheduleStateJob extends QuartzJobBean {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final AtomicBoolean flag = new AtomicBoolean(true);
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
        StateProcess process=new StateProcess(scheduleJob,scheduleJobService,restTemplate,scheduleJobLogService,log);
        process.initLog();
        Integer count=0;
        while(flag.get()&&count<=scheduleJob.getExcuteIntervalCounts()) {

            count++;
            try {
                if(scheduleJob.getMode()==Constant.ScheduleMode.API.getValue()){
                    process.apiProcess(new StateProcess.CallBack() {
                        @Override
                        public void callBack(boolean fail) {
                            flag.set(fail);
                        }
                    });
                }else{
                    process.shellProcess(new StateProcess.CallBack() {
                        @Override
                        public void callBack(boolean fail) {
                            flag.set(fail);
                        }
                    });

                }
            }catch (Exception e){
                flag.set(true);
            }

            try {
                if(flag.get()){
                    TimeUnit.SECONDS.sleep(scheduleJob.getExcuteIntervalTimes());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}