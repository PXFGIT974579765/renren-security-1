

package io.renren.modules.job.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时任务
 */
@TableName("schedule_job")
public class ScheduleJobEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 任务调度参数key
	 */
    public static final String JOB_PARAM_KEY = "JOB_PARAM_KEY";
	
	/**
	 * 任务id
	 */
	@TableId
	private Long jobId;

	/**
	 * spring bean名称
	 */
	private String beanName;
	
	/**
	 * 方法名
	 */
	private String methodName;
	
	/**
	 * 参数
	 */
	private String params;
	
	/**
	 * cron表达式
	 */
	@NotBlank(message="cron表达式不能为空")
	private String cronExpression;

	/**
	 * 任务类型，0  本地任务，1远程任务
	 */
	private Integer type;

	/**
	 * 任务名称
	 */
	@NotBlank(message="任务名称不能为空")
	private String jobName;
	/**
	 * 阻塞于任务id，用英文,隔开，如1,2,3
	 */
	private String blockJobIds;



	/**
	 * 调度的方式 0-类名方法名参数 ，1-restful-api  2-shell脚本
	 */
	private Integer mode;

	/**
	 * 远程调度的rest api，当mode为1并且选择调度方式为api时填写
	 */
	private String dispatchApi;

	/**
	 * 任务调度成功的返回值
	 */
	private String dispatchSuccessValue;

	/**
	 * 任务调度失败的返回值
	 */
	private String dispatchFailValue;



	/**
	 * 远程调度的shell 脚本，当mode为1并且选择调度方式为shell脚本时填写
	 */
	private String dispatchShell;

	/**
	 * 任务执行结果查询的shell 脚本，当mode为1并且选择调度方式为shell脚本时填写
	 */
	private String queryShell;




	/**
	 * quartz客户端部署的服务器ip地址

	 */
	private String clientIp;
	/**
	 * 任务正常过程中的状态，当status为0时有效，0,正常状态 1 阻塞状态，2 调度成功，3 调度失败，4 执行成功，5 执行失败
	 */
	private Integer state;
	/**
	 * 查询任务执行结果状态api
	 */
	private String queryApi;
	/**
	 * 任务状态查询执行成功之后的返回值
	 */
	private String querySuccessValue;
	/**
	 * 任务查询执行失败之后的返回值
	 */
	private String queryFailValue;
	/**
	 * 时隔多少秒后进行任务结果查询，单位秒
	 */
	private String intervalSeconds;


	/**
	 * 任务执行结果是否需要单独查询 0 不需要 ，1 需要
	 */
	private Integer needQueryFlag;
	/**
	 * 任务状态
	 */
	private Integer status;



	/**
	 * 备注
	 */
	private String remark;



	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 设置：任务id
	 * @param jobId 任务id
	 */
	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	/**
	 * 获取：任务id
	 * @return Long
	 */
	public Long getJobId() {
		return jobId;
	}
	
	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
	 * 设置：任务状态
	 * @param status 任务状态
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * 获取：任务状态
	 * @return String
	 */
	public Integer getStatus() {
		return status;
	}
	
	/**
	 * 设置：cron表达式
	 * @param cronExpression cron表达式
	 */
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	/**
	 * 获取：cron表达式
	 * @return String
	 */
	public String getCronExpression() {
		return cronExpression;
	}
	
	/**
	 * 设置：创建时间
	 * @param createTime 创建时间
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * 获取：创建时间
	 * @return Date
	 */
	public Date getCreateTime() {
		return createTime;
	}
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getBlockJobIds() {
		return blockJobIds;
	}

	public void setBlockJobIds(String blockJobIds) {
		this.blockJobIds = blockJobIds;
	}

	public Integer getMode() {
		return mode;
	}

	public void setMode(Integer mode) {
		this.mode = mode;
	}

	public String getClientIp() {
		return clientIp;
	}

	public String getDispatchShell() {
		return dispatchShell;
	}

	public void setDispatchShell(String dispatchShell) {
		this.dispatchShell = dispatchShell;
	}

	public String getQueryShell() {
		return queryShell;
	}

	public void setQueryShell(String queryShell) {
		this.queryShell = queryShell;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	public String getDispatchApi() {
		return dispatchApi;
	}

	public void setDispatchApi(String dispatchApi) {
		this.dispatchApi = dispatchApi;
	}

	public String getDispatchSuccessValue() {
		return dispatchSuccessValue;
	}

	public void setDispatchSuccessValue(String dispatchSuccessValue) {
		this.dispatchSuccessValue = dispatchSuccessValue;
	}

	public String getDispatchFailValue() {
		return dispatchFailValue;
	}

	public void setDispatchFailValue(String dispatchFailValue) {
		this.dispatchFailValue = dispatchFailValue;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getQueryApi() {
		return queryApi;
	}

	public void setQueryApi(String queryApi) {
		this.queryApi = queryApi;
	}

	public String getQuerySuccessValue() {
		return querySuccessValue;
	}

	public void setQuerySuccessValue(String querySuccessValue) {
		this.querySuccessValue = querySuccessValue;
	}

	public String getQueryFailValue() {
		return queryFailValue;
	}

	public void setQueryFailValue(String queryFailValue) {
		this.queryFailValue = queryFailValue;
	}

	public String getIntervalSeconds() {
		return intervalSeconds;
	}

	public void setIntervalSeconds(String intervalSeconds) {
		this.intervalSeconds = intervalSeconds;
	}

	public Integer getNeedQueryFlag() {
		return needQueryFlag;
	}

	public void setNeedQueryFlag(Integer needQueryFlag) {
		this.needQueryFlag = needQueryFlag;
	}
}
