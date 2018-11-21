package com.ule.merchant.demo.db.dao.oracle.xxx;

import com.ule.merchant.demo.db.dto.TestDto;

import java.util.List;


public interface TestDao {
    List<TestDto> testQuery(String param);
}