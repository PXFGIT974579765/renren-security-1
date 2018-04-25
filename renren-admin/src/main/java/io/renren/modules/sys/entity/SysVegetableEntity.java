package io.renren.modules.sys.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Transient;

import com.baomidou.mybatisplus.annotations.*;

/**
 * 蔬菜实体
 * 
 * @author ryanyang
 * 
 * @date 2018年4月18日
 */
@TableName("sys_vegetable")
public class SysVegetableEntity implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/**
	 * 蔬菜ID
	 */
	@TableId
	private long id;
	
	/**
	 * 平均价格
	 */
	private double avePrice;
	
	/**
	 * 最高价格
	 */
	private double hPrice;
	
	/**
	 * 最低价格
	 */
	private double lPrice;
	
	/**
	 * 蔬菜种类
	 */
	private String name;
	
	/**
	 * 调查来源区域
	 */
	private String area;
	
	/**
	 * 时间
	 */
	private String time;
	
	/**
	 * 预测最高价格
	 */
	private Double predicHPrice;
	
	/**
	 * 预测最低价格
	 */
	private Double predicLPrice;
	
	private String updateTime;

	public SysVegetableEntity() {
		
	}
    public SysVegetableEntity(double price, String time) {
		this.avePrice=price;
		this.time=time;
	}

	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public double getAvePrice() {
		return avePrice;
	}
	public void setAvePrice(double avePrice) {
		this.avePrice = avePrice;
	}
	public double gethPrice() {
		return hPrice;
	}
	public void sethPrice(double hPrice) {
		this.hPrice = hPrice;
	}
	public double getlPrice() {
		return lPrice;
	}
	public void setlPrice(double lPrice) {
		this.lPrice = lPrice;
	}
	

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	public Double getPredicHPrice() {
		return predicHPrice;
	}
	public void setPredicHPrice(Double predicHPrice) {
		this.predicHPrice = predicHPrice;
	}
	public Double getPredicLPrice() {
		return predicLPrice;
	}
	public void setPredicLPrice(Double predicLPrice) {
		this.predicLPrice = predicLPrice;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
}
