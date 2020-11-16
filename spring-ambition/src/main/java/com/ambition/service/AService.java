package com.ambition.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Elewin
 * @date 2020-01-21 1:57 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
@Component(value = "aService")
public class AService {

	@Autowired
	public AService(BService bService) {
		System.out.println("aService create");
	}
}
