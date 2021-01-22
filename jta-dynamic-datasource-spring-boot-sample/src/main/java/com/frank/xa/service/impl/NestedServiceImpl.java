package com.frank.xa.service.impl;

import com.frank.xa.annotation.DataSource;
import com.frank.xa.entity.User1;
import com.frank.xa.mapper.UserMapper1;
import com.frank.xa.service.UserService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 * User: Frank_Lei 00707
 * Date: 2020-07-02
 * Time: 9:21
 */
@Service
public class NestedServiceImpl implements UserService1 {

    @Autowired
    UserMapper1 userMapper1;

    @Override
    @DataSource("mininglamp")
    public int add(User1 user) {
        return userMapper1.insert(user);
    }

    @Override
    @DataSource("integration")
    public int addToIntegration(User1 user) {
        return userMapper1.insert(user);
    }

    @Override
    @DataSource("mininglamp")
    public int updateBykey(User1 user) {
        return userMapper1.updateByPrimaryKey(user);
    }

    @Override
    public int updateBykey1(User1 user) {
        return 0;
    }

    @Override
    public void nestedDatasource() {

    }
}
