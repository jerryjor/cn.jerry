package cn.jerry.springboot.demo.db.service;

import cn.jerry.springboot.demo.db.dao.oracle.xxx.TestDao;
import cn.jerry.springboot.demo.db.dto.TestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    @Autowired
    private TestDao dao;

    public TestDto testQuery(String param) {
        // do business here
        List<TestDto> dtoList = dao.testQuery(param);
        return dtoList == null || dtoList.isEmpty() ? null : dtoList.get(0);
    }
}
