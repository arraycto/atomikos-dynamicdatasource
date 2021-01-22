package com.frank.xa;

import tk.mybatis.mapper.common.ConditionMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * 继承tk.mybatis的这3个接口就包含了crud、以及分页、条件查询的所有组件了
 * 其他普通Mapper只需要继承BaseMapper即可
 */
public interface BaseMapper<T> extends Mapper<T> ,MySqlMapper<T>, ConditionMapper<T> {
}
