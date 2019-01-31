package cn.jerry.fabric.api.fabric.controller;

import cn.jerry.fabric.api.fabric.startup.StartupListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class WelcomeController {
    private static Logger logger = LogManager.getLogger();

    @RequestMapping(value = {"/", "/hello", "/hello.do"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Map<String, Object> hello() {
        logger.info("hello......");
        Map<String, Object> server = new HashMap<>();
        server.put("requestTime", new Date());
        server.put("sysStartTime", new Date(StartupListener.startTime));
        return server;
    }
}