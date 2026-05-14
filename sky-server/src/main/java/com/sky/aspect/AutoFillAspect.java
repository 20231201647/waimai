package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Pointcut("@annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充...");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        Object entity = args[0];

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if (operationType == OperationType.INSERT) {
            try {
                Field createTime = entity.getClass().getDeclaredField(AutoFillConstant.CREATE_TIME);
                Field createUser = entity.getClass().getDeclaredField(AutoFillConstant.CREATE_USER);
                Field updateTime = entity.getClass().getDeclaredField(AutoFillConstant.UPDATE_TIME);
                Field updateUser = entity.getClass().getDeclaredField(AutoFillConstant.UPDATE_USER);

                createTime.setAccessible(true);
                createUser.setAccessible(true);
                updateTime.setAccessible(true);
                updateUser.setAccessible(true);

                if (createTime.get(entity) == null) {
                    createTime.set(entity, now);
                }
                if (createUser.get(entity) == null) {
                    createUser.set(entity, currentId);
                }
                if (updateTime.get(entity) == null) {
                    updateTime.set(entity, now);
                }
                if (updateUser.get(entity) == null) {
                    updateUser.set(entity, currentId);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("公共字段自动填充失败：{}", e.getMessage());
            }
        } else if (operationType == OperationType.UPDATE) {
            try {
                Field updateTime = entity.getClass().getDeclaredField(AutoFillConstant.UPDATE_TIME);
                Field updateUser = entity.getClass().getDeclaredField(AutoFillConstant.UPDATE_USER);

                updateTime.setAccessible(true);
                updateUser.setAccessible(true);

                if (updateTime.get(entity) == null) {
                    updateTime.set(entity, now);
                }
                if (updateUser.get(entity) == null) {
                    updateUser.set(entity, currentId);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("公共字段自动填充失败：{}", e.getMessage());
            }
        }
    }
}
