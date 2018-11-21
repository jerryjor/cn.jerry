package com.ule.merchant.demo.controller;

import com.ule.merchant.demo.db.dto.TestDto;
import com.ule.merchant.demo.db.service.TestService;
import com.ule.merchant.demo.model.Result;
import com.ule.merchant.demo.util.ThrowableUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	private static Logger logger = LogManager.getLogger();

	@Autowired
	private TestService service;

	@RequestMapping(value = {"/test-db", "/test-db.do"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public Result<TestDto> testDb(String param) {
		logger.info("calling testDb......");
		// check param
        if (param == null) {
            return new Result<>("1000", "param [merchantId] is empty.");
        }
        try {
            // call service
            TestDto dto = service.testQuery(param);
            // return result
            Result<TestDto> result = new Result<>("0000", "succeed");
            result.setData(dto);
            return result;
        } catch (Exception e) {
            // log
            return new Result<>("9999", ThrowableUtil.findRootCause(e).getMessage());
        }
	}
}