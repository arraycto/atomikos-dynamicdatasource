package com.frank.xa.service.impl;

import com.frank.xa.annotation.DataSource;
import com.frank.xa.entity.User;
import com.frank.xa.entity.User1;
import com.frank.xa.mapper.UserMapper;
import com.frank.xa.mapper.UserMapper1;
import com.frank.xa.service.UserService;
import com.frank.xa.service.UserService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author Frank_Lei
 * @Description
 * @CreateTime 2020年06月19日 16:06:00
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserMapper1 userMapper1;

    @Autowired @Qualifier("nestedServiceImpl")
    UserService1 userService1;

    @Override
    @DataSource("integration")
    public int addToIntegration(User1 user) {
        return userMapper1.insert(user);
    }


    @Override @DataSource("test")
    public int add(User user) {
        return userMapper.insert(user);
    }

    @Override @DataSource("test")
    public int updateBykey(User user) {
        return userMapper.updateByPrimaryKey(user);
    }

    @Override @DataSource("mininglamp")
    public int updateBykey1(User user) {
        return userMapper.updateByPrimaryKey(user);
    }

    @Override
    public void nestedDatasource() {

        User user = new User();
        user.setId(1);
        user.setAge(30);
        user.setPassword("Hello Atomikos! hhhhhhhhhhh");
        user.setUsername("mysql_TEST");
        user.setSex(1);

        User1 user1 = new User1();
        user1.setId(1);
        user1.setAge(30);
        user1.setPassword("Hello Atomikos! hhhhhhhhhhhhh");
        user1.setUsername("mysql_TEST");
        user1.setSex(1);

        this.updateBykey(user);

        userService1.updateBykey(user1);
    }
}
