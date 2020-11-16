package com.ambition.aop;

/**
 * @author Elewin
 * @date 2020-02-01 4:20 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

//@Aspect
//@Component
public class AmbitionAop {

	@Pointcut("execution(* com.ambition.service.CalculateImpl.*(..))")
	public void pointCut() { }

	@Before(value = "pointCut()")
	public void methodBefore(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().getName();
		System.out.println("执行目标方法【" + methodName + "】之前执行<前置通知>, 入参" + Arrays.asList(joinPoint.getArgs()));
	}

	@After(value = "pointCut()")
	public void methodAfter(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().getName();
		System.out.println("执行目标方法【" + methodName + "】之前执行<后置通知>, 入参" + Arrays.asList(joinPoint.getArgs()));
	}

	@AfterReturning(value = "pointCut()")
	public void methodReturning(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().getName();
		System.out.println("执行目标方法【" + methodName + "】之前执行<返回通知>, 入参" + Arrays.asList(joinPoint.getArgs()));
	}

	@AfterThrowing(value = "pointCut()")
	public void methodAfterThrowing(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().getName();
		System.out.println("执行目标方法【" + methodName + "】之前执行<异常通知>, 入参" + Arrays.asList(joinPoint.getArgs()));
	}

	@Around(value = "pointCut()")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		System.out.println("around before");
		// Object retVal = pjp.proceed();
		System.out.println("around after");
		return 0;
	}

}
