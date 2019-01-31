package cn.jerry.springboot.demo.db.dao.oracle.xxx;

import cn.jerry.springboot.demo.db.dto.TestDto;

import java.util.List;


public interface TestDao {
    List<TestDto> testQuery(String param);
}