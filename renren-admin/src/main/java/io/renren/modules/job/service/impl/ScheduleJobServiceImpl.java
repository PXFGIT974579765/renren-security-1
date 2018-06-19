

package io.renren.modules.job.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import io.renren.common.utils.Constant;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.job.dao.ScheduleJobDao;
import io.renren.modules.job.entity.ScheduleJobEntity;
import io.renren.modules.job.service.ScheduleJobService;
import io.renren.modules.job.utils.ScheduleUtils;
import org.apache.commons.lang.StringUtils;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;

@Service("scheduleJobService")
public class ScheduleJobServiceImpl extends ServiceImpl<ScheduleJobDao, ScheduleJobEntity> implements ScheduleJobService {
	@Autowired
    private Scheduler scheduler;
	
	/**
	 * 项目启动时，初始化定时器
	 */
	@PostConstruct
	public void init(){
		List<ScheduleJobEntity> scheduleJobList = this.selectList(null);
		for(ScheduleJobEntity scheduleJob : scheduleJobList){
			CronTrigger cronTrigger = null;
			try {
				cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getJobId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if(cronTrigger == null) {
                ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
            }else {
                ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
            }
		}
	}

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		String beanName = (String)params.get("beanName");

		Page<ScheduleJobEntity> page = this.selectPage(
				new Query<ScheduleJobEntity>(params).getPage(),
				new EntityWrapper<ScheduleJobEntity>().like(StringUtils.isNotBlank(beanName),"bean_name", beanName)
		);

		return new PageUtils(page);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void save(ScheduleJobEntity scheduleJob) {
		scheduleJob.setCreateTime(new Date());
		scheduleJob.setStatus(Constant.ScheduleStatus.NORMAL.getValue());
        this.insert(scheduleJob);
        
        ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
    }
	/**
	 * 创建查询执行状态任务
	 */
	@Override
	public void createJobForQueryState(Long[] jobIds) {
		for(Long jobId : jobIds){
			ScheduleJobEntity job=this.selectById(jobId);
			//任务执行结果是否需要单独查询 0不需要 ，1需要
			System.out.println(job.getNeedQueryFlag().intValue()==1);
			System.out.println(job.getState().intValue()==Constant.ScheduleStates.EXCUTE_SUCCESS.getValue());
			if(job.getNeedQueryFlag().intValue()==1&&job.getState()==Constant.ScheduleStates.EXCUTE_SUCCESS.getValue()){
				ScheduleUtils.deleteScheduleJob(scheduler,"q_"+job.getJobId());
				ScheduleUtils.createScheduleStateQueryJob(scheduler, job);
				ScheduleUtils.runQuery(scheduler, job);
			}
		}

	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(ScheduleJobEntity scheduleJob) {
        ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
                
        this.updateById(scheduleJob);
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] jobIds) {
    	for(Long jobId : jobIds){
    		ScheduleUtils.deleteScheduleJob(scheduler, jobId);
    	}
    	
    	//删除数据
    	this.deleteBatchIds(Arrays.asList(jobIds));
	}

	@Override
    public int updateBatch(Long[] jobIds, int status){
    	Map<String, Object> map = new HashMap<>();
    	map.put("list", jobIds);
    	map.put("status", status);
    	return baseMapper.updateBatch(map);
    }
    
	@Override
	@Transactional(rollbackFor = Exception.class)
    public void run(Long[] jobIds) {
    	for(Long jobId : jobIds){
    		ScheduleUtils.run(scheduler, this.selectById(jobId));
    	}
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
    public void pause(Long[] jobIds) {
        for(Long jobId : jobIds){
    		ScheduleUtils.pauseJob(scheduler, jobId);
    	}
        
    	updateBatch(jobIds, Constant.ScheduleStatus.PAUSE.getValue());
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
    public void resume(Long[] jobIds) {
    	for(Long jobId : jobIds){
    		ScheduleUtils.resumeJob(scheduler, jobId);
    	}

    	updateBatch(jobIds, Constant.ScheduleStatus.NORMAL.getValue());
    }
/*
*  查询所有任务
 */
	@Override
	public List<ScheduleJobEntity> queryAll() {
		return baseMapper.queryAll();
	}

}
