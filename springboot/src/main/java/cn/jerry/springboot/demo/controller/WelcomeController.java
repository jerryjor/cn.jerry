package cn.jerry.springboot.demo.controller;

import cn.jerry.springboot.demo.startup.StartupListener;
import cn.jerry.springboot.demo.util.LocalIpUtil;
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
        server.put("icon", "http://pic.51yuansu.com/pic2/cover/00/50/72/5816600348461_610.jpg");
        List<Map<String, Object>> servers = new ArrayList<>();
        servers.add(server);
        model.addAttribute("servers", servers);
		return "hello";
	}
}