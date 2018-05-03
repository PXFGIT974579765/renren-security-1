package io.renren.modules.sys.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import io.renren.common.utils.DataPoint;
import io.renren.common.utils.RegressionLine;
import io.renren.common.utils.SortMapByValue;
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
	public List<String> getItem(String area) {
		
		return baseMapper.getItem(area);
	}

	@Override
	public List<String> getAreas() {
		return baseMapper.getAreas();
	}

	@Override
	public List<SysVegetableEntity> getTendency(Integer days, String name, String area) {
		
		return baseMapper.getTendency(days, name, area);
	}

	@Override
	public Map<String, Double> getVegetableTotalTendency(Integer days,String area) {
		List<String> itemList = this.getItem(area);
		
		Map<String, Double> map = new HashMap<String, Double>( );
		for (String item : itemList) {
			List<SysVegetableEntity> veList = this.getTendency(days, item, area);
			double sum = 0.0;
			for (SysVegetableEntity sysVegetableEntity : veList) {
				sum += (sysVegetableEntity.gethPrice()+sysVegetableEntity.getlPrice())/2;
			}
			map.put(item, sum/veList.size());
		}
		
		
		return SortMapByValue.sortByValue(map,true);
	}

	@Override
	public Map<String, Double> getVegetableTopFiveHPriecTendency(Integer days, String area) {
		Map<String, Double> tmMap = this.getVegetableTotalPriceTendency(days, area, "hprice");
		
		return tmMap;
	}

	@Override
	public Map<String, Double> getVegetableTopFiveLPriecTendency(Integer days, String area) {
		Map<String, Double> tmMap = this.getVegetableTotalPriceTendency(days, area, "lprice");
		return tmMap;
	}

	@Override
	public List<SysVegetableEntity> getPriceTendency(Integer days, String name, String area) {
		
		return baseMapper.getPriceTendency(days, name, area);
	}

	@Override
	public Map<String, Double> getVegetableTotalPriceTendency(Integer days, String area,String priceType) {
        List<String> itemList = this.getItem(area);
        RegressionLine line = new RegressionLine();
		Map<String, Double> map = new HashMap<String, Double>( );
		for (String item : itemList) {
			List<SysVegetableEntity> veList = this.getTendency(days, item, area);
			line = new RegressionLine();
			for(int i = 1 ; i <= veList.size(); i++) {
				if(priceType == "hprice") {
			        line.addDataPoint(new DataPoint(i, veList.get(i-1).gethPrice()));
				}else if(priceType == "lprice"){
					line.addDataPoint(new DataPoint(i, veList.get(i-1).getlPrice()));
				}else {
					line.addDataPoint(new DataPoint(i, veList.get(i-1).getAvePrice()));
				}
			}
			map.put(item, line.getA1());
		}
		
		
		return SortMapByValue.sortByValue(map,true);
	}

	@Override
	public List<SysVegetableEntity> queryByArea(String area) {
		List<SysVegetableEntity> list=this.baseMapper.queryByArea(area);
		String dateStr;
		if(list==null||list.size()<=0){
			return null;
		}
		dateStr=list.get(0).getTime();
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date=format.parse(dateStr.toString());
			Calendar calendar=Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_MONTH,-3);
			String threeAgoTimeStr=format.format(calendar.getTime());
			calendar.add(Calendar.DAY_OF_MONTH,1);
			String twoAgoTimeStr=format.format(calendar.getTime());
			calendar.add(Calendar.DAY_OF_MONTH,1);
			String oneAgoTimeStr=format.format(calendar.getTime());
			List<SysVegetableEntity> threeAgoLists=this.baseMapper.queryByAreaTime(threeAgoTimeStr,dateStr,area);
			List<SysVegetableEntity> threeAgoList=new ArrayList<SysVegetableEntity>();
			List<SysVegetableEntity> twoAgoList=new ArrayList<SysVegetableEntity>();
			List<SysVegetableEntity> oneAgoList=new ArrayList<SysVegetableEntity>();
			for(SysVegetableEntity item:threeAgoLists){
				if(item.getTime().equals(threeAgoTimeStr)){
					threeAgoList.add(item);
				}else if(item.getTime().equals(twoAgoTimeStr)){
					twoAgoList.add(item);
				}else if(item.getTime().equals(oneAgoTimeStr)){
					oneAgoList.add(item);
				}
			}
			for(SysVegetableEntity entity:list){
				String name=entity.getName();
				SysVegetableEntity three=this.getItemFromListByName(name,threeAgoList);
				SysVegetableEntity two=this.getItemFromListByName(name,twoAgoList);
				SysVegetableEntity one=this.getItemFromListByName(name,oneAgoList);
                double threeL=three==null?0.0:(three.getlPrice()==0.0?0.0:three.getlPrice());
                double threeH=three==null?0.0:(three.gethPrice()==0.0?0.0:three.gethPrice());
                double twoL=three==null?0.0:(two.getlPrice()==0.0?0.0:two.getlPrice());
                double twoH=three==null?0.0:(two.gethPrice()==0.0?0.0:two.gethPrice());
                double oneL=three==null?0.0:(one.getlPrice()==0.0?0.0:one.getlPrice());
                double oneH=three==null?0.0:(one.gethPrice()==0.0?0.0:one.gethPrice());
				double avegL=Math.round((threeL+twoL+oneL)/3.0*100)/100.0;
				double avegH=Math.round((threeH+twoH+oneH)/3.0*100)/100.0;
				entity.setThreeAgoHPrice(avegH);
				entity.setThreeAgoLPrice(avegL);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return list;
	}
   /*
   * 通过蔬菜名字从蔬菜列表里面获取单个蔬菜
    */
	public SysVegetableEntity getItemFromListByName(String name,List<SysVegetableEntity> list){
		SysVegetableEntity entity=null;
		for(SysVegetableEntity item:list){
			if(item.getName().equals(name)){
				entity=item;
				break;
			}
		}
		return entity;
	}

	

}
