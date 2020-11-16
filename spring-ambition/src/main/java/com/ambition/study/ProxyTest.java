package com.ambition.study;

import com.ambition.config.AmbitionConfig;
import com.ambition.dao.UserDao;
import com.ambition.factory.AmbitionBeanFactory;
import com.ambition.factory.ProxyFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

/**
 * @author Elewin
 * @date 2020-01-08 10:01 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class ProxyTest {

	public static void main(String[] args) {

//		DataSource dataSource = null;
//		TransactionFactory transactionFactory =
//				new JdbcTransactionFactory();
//		Environment environment =
//				new Environment("development", transactionFactory, dataSource);
//		Configuration configuration = new Configuration(environment);
//		configuration.addMapper(AmbitionConfig.class);
//		SqlSessionFactory sqlSessionFactory =
//				new SqlSessionFactoryBuilder().build(configuration);
//
//		AmbitionConfig mapper = sqlSessionFactory.openSession().getMapper(AmbitionConfig.class);
//
//		System.out.println(mapper);

		ProxyFactory proxyFactory = new ProxyFactory();

		UserDao userDao = proxyFactory.getMapper(UserDao.class);

		userDao.query();
	}
}
