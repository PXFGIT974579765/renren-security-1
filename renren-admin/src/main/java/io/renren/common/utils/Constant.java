

package io.renren.common.utils;

/**
 * 常量
 
 */
public class Constant {
	/** 超级管理员ID */
	public static final int SUPER_ADMIN = 1;
    /** 数据权限过滤 */
	public static final String SQL_FILTER = "sql_filter";




	public enum LogType{
	    DISPATCH(0),
        EXCUTE(1);
        private int value;
        LogType(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
	public enum Client{
	    PORT(8890);
        private int value;
        Client(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
	public enum ScheduleMode{
	    CLASS(0),
        API(1),
        SHELL(2);
        private int value;
        ScheduleMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    /**
     * 任务正常过程中的状态
     */
    public enum ScheduleStates {
        /**
         * 正常
         */
        NOMORL(0),
        /**
         * 被阻塞
         */
        PAUSED(1),
        /**
         * 调度成功
         */
        DISPATCH_SUCCESS(2),
        /**
         * 调度失败
         */
        DISPATCH_FAILE(3),
        /**
         * 执行成功
         */
        EXCUTE_SUCCESS(4),
        /**
         * 执行失败
         */
        EXCUTE_FAIL(5);


        private int value;
        ScheduleStates(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }



    }

	/**
	 * 菜单类型
	 */
    public enum MenuType {
        /**
         * 目录
         */
    	CATALOG(0),
        /**
         * 菜单
         */
        MENU(1),
        /**
         * 按钮
         */
        BUTTON(2);

        private int value;

        MenuType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
    /**
     * 定时任务状态
     */
    public enum ScheduleStatus {
        /**
         * 正常
         */
    	NORMAL(0),
        /**
         * 暂停
         */
    	PAUSE(1);

        private int value;

        ScheduleStatus(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }

    /**
     * 云服务商
     */
    public enum CloudService {
        /**
         * 七牛云
         */
        QINIU(1),
        /**
         * 阿里云
         */
        ALIYUN(2),
        /**
         * 腾讯云
         */
        QCLOUD(3);

        private int value;

        CloudService(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
