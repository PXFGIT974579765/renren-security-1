

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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 调度定时任务
 */
public class ScheduleJob extends QuartzJobBean {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private ExecutorService service = Executors.newSingleThreadExecutor();
    //阻塞超时时间
	private Long blockTimeout=1000*10*1L;
	//阻塞时时隔多少秒再进行查询
	private Long blockSleepIntervalTime=2*1000L;
	
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		ScheduleJobEntity scheduleJob=(ScheduleJobEntity)context.getMergedJobDataMap().get(ScheduleJobEntity.JOB_PARAM_KEY);
		String blockIds=scheduleJob.getBlockJobIds();
		ScheduleJobService scheduleJobService = (ScheduleJobService) SpringContextUtils.getBean("scheduleJobService");
		RestTemplateService restTemplate = (RestTemplateService) SpringContextUtils.getBean("restTemplateService");
		//获取spring bean
		ScheduleJobLogService scheduleJobLogService = (ScheduleJobLogService) SpringContextUtils.getBean("scheduleJobLogService");
		//阻塞标志
		AtomicBoolean block= new AtomicBoolean(false);
		//是否已解决阻塞标志
		boolean resolvedBlock=true;
		if(StringUtils.isNotEmpty(blockIds)){
			//如果依赖于其他任务,等待其他任务执行成功
			scheduleJob.setState(Constant.ScheduleStates.PAUSED.getValue());
			scheduleJobService.update(scheduleJob);
			List idArry=Arrays.asList(blockIds.split(","));
			List<ScheduleJobEntity> jobs=scheduleJobService.selectBatchIds(idArry);
			block.set(true);
			long startTime=System.currentTimeMillis();
			while (block.get()){
				block.set(false);
				jobs.forEach(job->{
					//if(job.getStatus()==Constant.ScheduleStatus.NORMAL.getValue()){
						if(job.getState()!=Constant.ScheduleStates.EXCUTE_SUCCESS.getValue()
								&&job.getState()!=Constant.ScheduleStates.NOMORL.getValue()){
							block.set(true);
						}
					//}else{
						//block.set(true);
					//}
				});
				if(block.get()){
					try {
						Thread.sleep(blockSleepIntervalTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				long allTime=System.currentTimeMillis()-startTime;
				//防止等待超时，30分钟如果还在阻塞的话就跳出循环
				if((allTime>blockTimeout)&&block.get()){
					block.set(false);
					resolvedBlock=false;
				}
			}
		}

		if(!resolvedBlock){
			logger.info("任务阻塞超时，任务ID：" + scheduleJob.getJobId());
			return;
		}

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
		logger.info("任务准备调度，任务ID：" + scheduleJob.getJobId());
		//判断任务调度方式 0 类名方法名参数 ，1 restful-api , 2 shell脚本
		if(scheduleJob.getMode()==Constant.ScheduleMode.CLASS.getValue()){
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
					log.setTimes((int)times);
					//0成功  1 失败
					log.setStatus(0);
					log.setMessage("任务调度执行成功");
					log.setType(Constant.LogType.DISPATCH.getValue());
					scheduleJobLogService.insert(log);
					logger.info("任务调度执行完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
					scheduleJob.setState(Constant.ScheduleStates.EXCUTE_SUCCESS.getValue());
					scheduleJobService.update(scheduleJob);
				}else{
					//任务执行总时长
					times = System.currentTimeMillis() - startTime;
					log.setTimes((int)times);
					logger.error("任务调度执行失败，任务ID：" + scheduleJob.getJobId()+":找不到对应的类名方法名");
					//0 成功    1失败
					log.setStatus(1);
					log.setType(Constant.LogType.DISPATCH.getValue());
					log.setMessage("任务调度执行失败--->找不到对应的类名方法名");
					scheduleJobLogService.insert(log);
					scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
					scheduleJobService.update(scheduleJob);
				}

			} catch (Exception e) {
				logger.error("任务调度执行失败，任务ID：" + scheduleJob.getJobId(), e);
				//任务执行总时长
				times = System.currentTimeMillis() - startTime;
				log.setTimes((int)times);
				//0 成功    1失败
				log.setStatus(1);
				log.setType(Constant.LogType.DISPATCH.getValue());
				log.setMessage("任务调度执行失败");
				log.setError(StringUtils.substring(e.toString(), 0, 2000));
				scheduleJobLogService.insert(log);
				scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
				scheduleJobService.update(scheduleJob);
			}

		}else if(scheduleJob.getMode()==Constant.ScheduleMode.API.getValue()){
			//restful-api
			if(StringUtils.isEmpty(scheduleJob.getDispatchApi())){
				logger.error("任务调度失败，任务ID：" + scheduleJob.getJobId()+":未设置相关api");
				times = System.currentTimeMillis() - startTime;
				log.setTimes((int)times);
				//0 成功    1失败
				log.setStatus(1);
				log.setType(Constant.LogType.DISPATCH.getValue());
				log.setMessage("任务执行失败--->找不到对应的调度api");
				scheduleJobLogService.insert(log);
				scheduleJob.setState(Constant.ScheduleStates.DISPATCH_FAILE.getValue());
				scheduleJobService.update(scheduleJob);
			}else{
				String result=restTemplate.postForObject(scheduleJob.getDispatchApi(),null,String.class);
				if(result!=null&&result.equals(scheduleJob.getDispatchSuccessValue())){
					//0 成功    1失败
					log.setStatus(0);
					log.setType(Constant.LogType.DISPATCH.getValue());
					times = System.currentTimeMillis() - startTime;
					log.setTimes((int)times);
					//任务执行结果是否需要单独查询 0不需要 ，1需要
					if(scheduleJob.getNeedQueryFlag()==0){
						logger.info("任务调度执行完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
						log.setMessage("任务调度执行成功");
						scheduleJobLogService.insert(log);
						scheduleJob.setState(Constant.ScheduleStates.EXCUTE_SUCCESS.getValue());
						scheduleJobService.update(scheduleJob);
					}else{
						log.setMessage("任务调度成功");
						scheduleJobLogService.insert(log);
						scheduleJob.setState(Constant.ScheduleStates.DISPATCH_SUCCESS.getValue());
						scheduleJobService.update(scheduleJob);
						//todo 写查询任务代码
						scheduleJobService.createJobForQueryState(new Long[scheduleJob.getJobId().intValue()]);

					}
				}else{
					times = System.currentTimeMillis() - startTime;
					logger.error("任务调度执行失败，任务ID：" + scheduleJob.getJobId()+"  总共耗时：" + times + "毫秒");
					log.setTimes((int)times);
					//0 成功    1失败
					log.setStatus(1);
					log.setType(Constant.LogType.DISPATCH.getValue());
					log.setMessage("任务调度执行失败");
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
				log.setType(Constant.LogType.DISPATCH.getValue());
				log.setMessage("任务调度执行失败--->找不到对应的调度api");
				scheduleJobLogService.insert(log);
				scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
				scheduleJobService.update(scheduleJob);
			}else{
 				HashMap<String,String> map=new HashMap<String,String>();
				map.put("shell",scheduleJob.getDispatchShell());
				//String result=restTemplate.postForObject(scheduleJob.getClientIp()+":"+Constant.Client.PORT.getValue()+"/api/dispatch",map,String.class);
				JSONObject result=restTemplate.postForObject(scheduleJob.getClientIp()+"/client/api/dispatch",map,JSONObject.class);
				if(result!=null&&result.getString("code").equals("00000")){
					//0 成功    1失败
					times = System.currentTimeMillis() - startTime;
					log.setTimes((int)times);
					log.setStatus(0);
					log.setMessage(result.getString("msg"));
					log.setType(Constant.LogType.DISPATCH.getValue());
					//任务执行结果是否需要单独查询 0不需要 ，1需要
					if(scheduleJob.getNeedQueryFlag()==0){
						logger.info("任务调度执行完毕，任务ID：" + scheduleJob.getJobId() + "  总共耗时：" + times + "毫秒");
						//log.setMessage("任务调度执行成功");
						scheduleJobLogService.insert(log);
						scheduleJob.setState(Constant.ScheduleStates.EXCUTE_SUCCESS.getValue());
						scheduleJobService.update(scheduleJob);
					}else{
						//log.setMessage("任务调度成功");
						scheduleJobLogService.insert(log);
						scheduleJob.setState(Constant.ScheduleStates.DISPATCH_SUCCESS.getValue());
						scheduleJobService.update(scheduleJob);
						//todo 写查询任务代码
						scheduleJobService.createJobForQueryState(new Long[scheduleJob.getJobId().intValue()]);
					}
				}else{
					logger.error("任务调度执行失败，任务ID：" + scheduleJob.getJobId()+":未设置相关api");
					times = System.currentTimeMillis() - startTime;
					log.setTimes((int)times);
					//0 成功    1失败
					log.setStatus(1);
					log.setType(Constant.LogType.DISPATCH.getValue());
					log.setMessage("任务调度失败");
					scheduleJob.setState(Constant.ScheduleStates.EXCUTE_FAIL.getValue());
					scheduleJobService.update(scheduleJob);
				}

			}

		}







        

        

        

    }
}
