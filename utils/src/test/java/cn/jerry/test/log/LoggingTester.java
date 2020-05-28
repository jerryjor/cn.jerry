package cn.jerry.test.log;

import cn.jerry.log4j2.annotation.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggingTester {
	private static Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
        System.out.println(logger.getClass().getName());
		logger.info("what's up?");
	}
}
