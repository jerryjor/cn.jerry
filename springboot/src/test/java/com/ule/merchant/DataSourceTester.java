package com.ule.merchant;

import com.ule.merchant.demo.MainConfiguration;
import com.ule.merchant.demo.db.dao.oracle.xxx.TestDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainConfiguration.class)
public class DataSourceTester {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private TestDao dao;

    @Test
    public void test() {
        System.out.println(dao.testQuery("54654"));
    }
}
