package com.ambition.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author Elewin
 * @date 2020-05-04 5:00 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Configuration
//@ComponentScan(value = "com.ambition")
//@EnableTransactionManagement
//@EnableAspectJAutoProxy(exposeProxy = true)
public class TransactionConfig {

	@Bean
	public DataSource dataSource() {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		dataSource.setUrl("jdbc:mysql://localhost:3306/slave");
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		return dataSource;
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public PlatformTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

}
