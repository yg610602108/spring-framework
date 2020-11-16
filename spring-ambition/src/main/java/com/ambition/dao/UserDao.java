package com.ambition.dao;

import org.apache.ibatis.annotations.Select;

/**
 * @author Elewin
 * @date 2020-01-08 10:30 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public interface UserDao {

	@Select("Select * from XXX")
	void query();
}
