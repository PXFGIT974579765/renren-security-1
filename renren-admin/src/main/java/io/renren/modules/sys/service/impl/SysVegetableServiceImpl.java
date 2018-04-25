package io.renren.modules.sys.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import io.renren.modules.sys.dao.SysVegetableDao;
import io.renren.modules.sys.entity.SysVegetableEntity;
import io.renren.modules.sys.service.SysVegetableService;

/**
 * 蔬菜 
 * 
 * @author ryanyang
 * 
 * @date 2018年4月24日
 */
@Service("sysVegetableService")
public class SysVegetableServiceImpl extends ServiceImpl<SysVegetableDao, SysVegetableEntity> implements SysVegetableService{
	@Autowired
	SysVegetableDao sysVegetableDao;
	
	
	@Override
	public List<SysVegetableEntity> queryByType(String type) {
		return baseMapper.queryByType(type);
	}

	@Override
	public List<SysVegetableEntity> queryByNameTime(String name, String beginTime, String endTime) {
		
		return baseMapper.queryByNameTime(name, beginTime, endTime);
	}

	@Override
	public List<SysVegetableEntity> queryByCondition(Map<String, Object> map) {
		
		return baseMapper.selectByMap(map);
	}

	@Override
	public List<SysVegetableEntity> queryByCondition(String name, String beginTime, String endTime, String area) {
		
		return baseMapper.queryByCondition(name, beginTime, endTime, area);
	}

	@Override
	public List<String> getItem() {
		
		return baseMapper.getItem();
	}

}
