

package io.renren.modules.job.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import io.renren.modules.job.entity.ScheduleJobEntity;

import java.util.List;
import java.util.Map;

/**
 * 定时任务

 */
public interface ScheduleJobDao extends BaseMapper<ScheduleJobEntity> {
	
	/**
	 * 批量更新状态
	 */
	int updateBatch(Map<String, Object> map);

    List<ScheduleJobEntity> queryAll();
}
