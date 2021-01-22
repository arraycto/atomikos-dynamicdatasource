package com.frank.xa.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;

/**
 * Description:
 * User: Frank_Lei 00707
 * Date: 2018-03-21
 * Time: 18:06
 */
public class DynamicDataSource extends AbstractRoutingDataSource {


    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();// 必须添加该句，否则新添加数据源无法识别到
    }

    //必须重写其方法
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDBName();
    }
}
