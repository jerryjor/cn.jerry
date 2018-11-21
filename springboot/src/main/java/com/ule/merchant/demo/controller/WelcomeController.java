package com.ule.merchant.demo.controller;

import com.ule.merchant.demo.startup.StartupListener;
import com.ule.merchant.demo.util.LocalIpUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class WelcomeController {
	private static Logger logger = LogManager.getLogger();

	@RequestMapping(value = {"/", "/hello", "/hello.do"}, produces = MediaType.TEXT_HTML_VALUE)
	public String hello(Model model) {
		logger.info("hello......");
        Map<String, Object> server = new HashMap<>();
        server.put("ip", LocalIpUtil.getLocalIp4());
        server.put("time", new Date());
        server.put("run", (System.currentTimeMillis() - StartupListener.startTime)/1000L);
        server.put("icon", "//i0.ulecdn.com/ule/header/images/ico_return-home.png");
        List<Map<String, Object>> servers = new ArrayList<>();
        servers.add(server);
        model.addAttribute("servers", servers);
		return "hello";
	}
}