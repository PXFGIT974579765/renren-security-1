package io.renren.modules.sys.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.service.IService;

import io.renren.modules.sys.entity.SysVegetableEntity;

/**
 * 蔬菜
 * 
 * @author ryanyang
 * 
 * @date 2018年4月23日
 */
public interface SysVegetableService extends IService<SysVegetableEntity>{
	/**
	 * 根据种类获取蔬菜
	 */
	List<SysVegetableEntity> queryByType(String type);
	
	/**
	 * 根据蔬菜名字、查询起始时间查询
	 * @param name
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	List<SysVegetableEntity> queryByNameTime(String name, String beginTime, String endTime);
	
	List<SysVegetableEntity> queryByCondition(Map<String,Object> map);
	
	List<SysVegetableEntity> queryByCondition(String name, String beginTime, String endTime, String area);

    List<String> getItem();
}
