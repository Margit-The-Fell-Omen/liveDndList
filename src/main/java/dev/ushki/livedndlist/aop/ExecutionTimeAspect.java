package dev.ushki.livedndlist.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

  @Around("execution(* dev.ushki.livedndlist.service..*(..))")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

    long start = System.currentTimeMillis();

    String methodName = joinPoint.getSignature().toShortString();

    try {
      Object result = joinPoint.proceed();

      long duration = System.currentTimeMillis() - start;
      log.info("Method {} executed in {} ms", methodName, duration);

      return result;

    } catch (Throwable ex) {
      long duration = System.currentTimeMillis() - start;
      log.error("Method {} finished with error in {} ms: {}",
          methodName, duration, ex.getMessage());
      throw ex;
    }
  }
}
