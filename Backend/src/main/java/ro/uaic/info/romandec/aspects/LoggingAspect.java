package ro.uaic.info.romandec.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* ro.uaic.info.romandec.controllers..*.*(..))")
    public void beforeControllerMethod(JoinPoint joinPoint) {
        System.out.println("Before executing controller method: " + joinPoint.getSignature().toShortString());
    }

    @Before("execution(* ro.uaic.info.romandec.services..*.*(..))")
    public void beforeServiceMethod(JoinPoint joinPoint) {
        System.out.println("Before executing service method: " + joinPoint.getSignature().toShortString());
    }

    @AfterReturning(pointcut = "execution(* ro.uaic.info.romandec.repository..*.*(..))", returning = "result")
    public void countListItemsAndLogMethod(JoinPoint joinPoint, Object result) {
        if (result instanceof Collection) {
            Collection<?> collection = (Collection<?>) result;
            String methodName = joinPoint.getSignature().toShortString();
            System.out.println("Method " + methodName + " returned a list with " + collection.size() + " items.");
        }
    }
}
