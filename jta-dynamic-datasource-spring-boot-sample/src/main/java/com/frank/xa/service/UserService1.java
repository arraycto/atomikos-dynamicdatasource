package com.frank.xa.service;


import com.frank.xa.entity.User1;

/**
 * @author Frank_Lei
 * @Description
 * @CreateTime 2020年06月19日 16:04:00
 */
public interface UserService1 {


    int add(User1 user);

    int updateBykey(User1 user);

    int updateBykey1(User1 user);

    void nestedDatasource();

    int addToIntegration(User1 user);

}
