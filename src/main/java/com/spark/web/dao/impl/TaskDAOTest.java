package com.spark.web.dao.impl;


import com.spark.web.dao.ITaskDAO;
import com.spark.web.domain.Task;

/**
 * 任务管理DAO测试类
 * @author Administrator
 *
 */
public class TaskDAOTest {
	
	public static void main(String[] args) {
		ITaskDAO taskDAO = DAOFactory.getTaskDAO();
		Task task = taskDAO.findById(1);
		System.out.println(task.getTaskName());  
	}
	
}
