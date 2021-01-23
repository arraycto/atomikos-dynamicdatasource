package com.frank.xa.config;

import java.util.HashSet;
import java.util.Set;

/**
 * Description:通过ThreadLocal为每个线程维护一个ThreadLocalMap,
 * 用DataSourceContextHolder.setDBType来指定当前数据源
 * User: Frank_Lei 00707
 * Date: 2018-03-21
 * Time: 18:08
 */
public class DataSourceContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    /**
     * 所有动态数据源的key集合，用于切换时校验数据源
     */
    private static final Set<Object> DS_KEYS = new HashSet<>();

    public static synchronized void setDBType(String dbType) {
        contextHolder.set(dbType);
    }

    public static String getDBName() {
        return contextHolder.get();
    }

    public static void clearDBType() {
        contextHolder.remove();
    }

    static boolean registerKey(String key) {
        return DS_KEYS.add(key);
    }

    static void registerKey(Set<Object> key) {
        DS_KEYS.addAll(key);
    }

    /**
     * 校验数据源key
     */
    public static boolean isValidKey(String key) {
        return DS_KEYS.contains(key);
    }
}
