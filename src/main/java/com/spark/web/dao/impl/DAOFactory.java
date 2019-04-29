package com.spark.web.dao.impl;


import com.spark.web.dao.ITaskDAO;

/**
 * DAO工厂类
 * @author Administrator
 *
 */
public class DAOFactory {

	/**
	 * 获取任务管理DAO
	 * @return DAO
	 */
	public static ITaskDAO getTaskDAO() {
		return new TaskDAOImpl();
	}
	
}
