package com.frank.xa.service;

import com.frank.xa.entity.User1;
import com.frank.xa.entity.User;

/**
 * @author Frank_Lei
 * @Description
 * @CreateTime 2020年06月19日 16:04:00
 */
public interface UserService {


    int add(User user);

    int updateBykey(User user);

    int updateBykey1(User user);

    void nestedDatasource();

    int addToIntegration(User1 user);

}
