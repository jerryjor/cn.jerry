package cn.jerry.hajndi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.commons.lang.ArrayUtils;

import cn.jerry.logging.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadyInvocationHandler implements InvocationHandler {
	// 服务器升级版本的异常
	private static final String EXCEPTION_NOT_FOUND = "org.jboss.aop.NotFoundInDispatcherException";
	// 服务器全部关闭
	private static final String EXCEPTION_UNREACHABLE = "Service unavailable";
	// log4j2
	private static Logger log = LogManager.getLogger();

	// 真正的实现类
	private HAJNDIContext context;
	private String jndi;
	private Object bean;

	ReadyInvocationHandler(HAJNDIContext context, String jndi, Object bean) {
		super();
		this.context = context;
		this.jndi = jndi;
		this.bean = bean;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] params) {
		// 执行请求的方法
		// TODO 增加监控日志
		try {
			log.info("invoke method, JNDI:" + this.jndi + ", method:" + method.getName()
			        + ", params:" + ArrayUtils.toString(params));
			return method.invoke(this.bean, params);
		} catch (Exception ex) {
			Throwable t = findRootCause(ex);
			// 如果缓存的实例已过期，重新初始化实例并缓存
			if (EXCEPTION_NOT_FOUND.equals(t.getClass().getName())) {
				log.info("invoke method failed because bean is expired. We need reinitialize it. JNDI:" + this.jndi);
				this.bean = this.context.lookupAndCache(this.jndi);
				try {
					return method.invoke(this.bean, params);
				} catch (Exception e) {
					log.info("invoke method failed again after reinitialized. JNDI:" + this.jndi
					        + ", method:" + method.getName());
					throw new RuntimeException(ex);
				}
			}
			// 如果所有的节点已关闭，缓存的存根会失效，需要清除缓存
			else if (t.getMessage().contains(EXCEPTION_UNREACHABLE)) {
				log.info("invoke method failed because service is unavailable. JNDI:" + this.jndi);
				this.context.clearCache();
				throw new RuntimeException(t);
			}
			// 其他错误，原样抛出
			else {
				log.info("invoke method failed. JNDI:" + this.jndi + ", method:"
				        + method.getName() + ", params:" + ArrayUtils.toString(params)
				        + ", reason:" + t.getClass().getName() + ":" + t.getMessage());
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 获取最根本的原因
	 * 
	 * @param t exception
	 * @return root exception
	 */
	private Throwable findRootCause(Throwable t) {
		Throwable rootCause = t;
		while (t != null) {
			rootCause = t;
			t = t.getCause();
		}
		return rootCause;
	}

}
