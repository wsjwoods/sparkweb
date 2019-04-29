package com.spark.web.dao;


import com.spark.web.domain.Task;

/**
 * 任务管理DAO接口
 * @author Administrator
 *
 */
public interface ITaskDAO {
	
	/**
	 * 根据主键查询任务
	 * @param taskid 主键
	 * @return 任务
	 */
	Task findById(long taskid);
	
}
