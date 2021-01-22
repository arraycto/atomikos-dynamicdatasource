package com.frank.xa.aspects;

import com.frank.xa.annotation.DataSource;
import com.frank.xa.config.DataSourceContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 动态数据源切换处理器
 * 切面应当优于 @Transactional 执行（@Transactional会在业务代码调用前获取DataSource），
 * 使用手动切换DataSourceContextHolder.setDBType("")在有事物控制的情况下会失效
 */
@Aspect
@Order(-1) //此处设置优先级在事务优先级之前（事务切面优先级为默认Integer.MAX_VALUE）
@Component
public class DynamicDataSourceAspect {

    Logger logger = LoggerFactory.getLogger(DynamicDataSourceAspect.class);

    /**
     * 切换数据源
     *
     * @param point
     * @param dataSource
     */
    @Before("@annotation(dataSource)")
    public void switchDataSource(JoinPoint point, DataSource dataSource) {
        if (StringUtils.isEmpty(dataSource.value())) return;
        if (!DataSourceContextHolder.isValidKey(dataSource.value())) {
            logger.error("Invalid key [{}],please check with method[{}]", dataSource.value(), point.getSignature());
        } else {
            DataSourceContextHolder.setDBType(dataSource.value());
            logger.info("==> Swith DataSource to [{}] in method [{}]", DataSourceContextHolder.getDBName(), point.getSignature());
        }
    }

    @After("@annotation(dataSource)")
    public void restoreDataSource(JoinPoint point, DataSource dataSource) {
        //每次请求完须清除，防止污染线程池中的线程（缺省mybatis会自动获取默认数据源）
        DataSourceContextHolder.clearDBType();
        logger.info("<== Revert DataSource : {} -> {}", dataSource.value(), point.getSignature());
    }
}
