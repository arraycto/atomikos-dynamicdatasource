package com.frank.xa;

import com.frank.xa.entity.User1;
import com.frank.xa.entity.User;
import com.frank.xa.service.UserService;
import com.frank.xa.service.UserService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frank_Lei
 * @Description
 * @CreateTime 2020年06月23日 08:30:00
 */
@RestController
@RequestMapping("/test")
public class ControllerTest {

    @Autowired
    @Qualifier("mininglampJdbcTemplate")
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserService userService;

    @Autowired
    UserService1 userService1;

    @GetMapping("/testMuti")
    @Transactional
    public void testMuti() {

        User user = new User();
        user.setId(1);
        user.setAge(30);
        user.setPassword("Hello Atomikos! 66666666666666");
        user.setUsername("mysql_TEST");
        user.setSex(1);

        User1 user1 = new User1();
        user1.setId(1);
        user1.setAge(30);
        user1.setPassword("Hello Atomikos! 6666666666666666");
        user1.setUsername("mysql_TEST");
        user1.setSex(1);

//        userService1.add(user1);
//        userService.add(user);
//        userService.updateBykey(user1);
//        userService.updateBykey1(user1);
        userService.updateBykey(user); // test

        userService1.updateBykey(user1); // mininglamp

        userService.addToIntegration(user1); // integration

//        userService1.addToIntegration(user1); // integration

//        List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from user_test1");
//        System.out.println(list);
//        throw new RuntimeException("muti datasource transaction test!");
    }
}
