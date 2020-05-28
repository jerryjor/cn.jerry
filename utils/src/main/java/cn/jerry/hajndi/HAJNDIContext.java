package cn.jerry.hajndi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import cn.jerry.log4j2.annotation.LogManager;
import org.apache.logging.log4j.Logger;

public class HAJNDIContext {
    // 缓存的上下文
    private Hashtable<String, String> environment;
    // 缓存的stub
    private static HashMap<String, Object> cachedProxies = new HashMap<>();
    // log4j2
    private static Logger log = LogManager.getLogger();

    HAJNDIContext(String confFilePath) {
        super();
        if (confFilePath == null || confFilePath.trim().isEmpty()) {
            log.error("Load HAJNDIContext failed, configure file is empty.");
            throw new RuntimeException("Load HAJNDIContext failed, configure file is empty.");
        }
        initEnvironment(confFilePath);
    }

    /**
     * 初始化Context
     *
     * @param confFilePath
     * @return
     */
    private void initEnvironment(String confFilePath) {
        Properties config = new Properties();
        try (InputStream is = HAJNDIContext.class.getResourceAsStream(confFilePath)) {
            config.load(is);
        } catch (IOException e) {
            log.error("read HAJNDIConf failed. configure file:" + confFilePath, e);
            throw new RuntimeException(e);
        }

        String propVal;
        this.environment = new Hashtable<>();
        this.environment.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        this.environment.put("java.naming.factory.url.pkgs", "jboss.naming:org.jnp.interfaces");
        // 查找的分区，不允许为空
        propVal = readProperty(config, "PARTITION_NAME");
        if (propVal.isEmpty()) {
            log.error("PARTITION_NAME not configured in FILE[" + confFilePath + "]");
            throw new RuntimeException("PARTITION_NAME not configured in FILE["
                    + confFilePath + "]");
        }
        this.environment.put("jnp.partitionName", propVal);
        // 查找的组播地址，不允许为空
        propVal = readProperty(config, "DISCOVERY_GROUP");
        if (propVal.isEmpty()) {
            log.error("DISCOVERY_GROUP not configured in PROP_FILE[" + confFilePath + "]");
            throw new RuntimeException("DISCOVERY_GROUP not configured in FILE["
                    + confFilePath + "]");
        }
        this.environment.put("jnp.discoveryGroup", propVal);
        // 查找的端口，可以不指定
        propVal = readProperty(config, "DISCOVERY_PORT");
        if (!propVal.isEmpty()) this.environment.put("jnp.discoveryPort", propVal);
        // 查找的TTL，可以不指定
        propVal = readProperty(config, "DISCOVERY_TTL");
        if (!propVal.isEmpty()) this.environment.put("jnp.discoveryTTL", propVal);
        // 查找超时，可以不指定
        propVal = readProperty(config, "DISCOVERY_TIMEOUT");
        if (!propVal.isEmpty()) this.environment.put("jnp.discoveryTimeout", propVal);
    }

    /**
     * 读取配置的属性，去除两端空格
     *
     * @param conf
     * @param propName
     * @return
     */
    private String readProperty(Properties conf, String propName) {
        String propVal = conf.getProperty(propName);
        return propVal == null ? "" : propVal.trim();
    }

    /**
     * 查找实例
     *
     * @param jndi
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T lookup(String jndi) {
        // 如果缓存不存在实例，先生成实例推到缓存
        if (!cachedProxies.containsKey(jndi)) lookupAndCache(jndi);
        // 返回缓存的实例
        return (T) cachedProxies.get(jndi);
    }

    /**
     * 实例化真正的Bean并缓存代理对象
     *
     * @param jndi
     * @return
     */
    Object lookupAndCache(String jndi) {
        if (jndi == null || jndi.trim().isEmpty()) {
            log.info("lookup failed, param[jndi] is empty.");
            throw new RuntimeException("lookup failed, param[jndi] is empty.");
        }
        // 实例化真正的Bean
        log.info("lookuping jndi[" + jndi + "]...");
        Object bean;
        try {
            Context context = new InitialContext(this.environment);
            bean = context.lookup(jndi);
        } catch (NamingException e) {
            log.error("lookup failed, jndi name:" + jndi, e);
            throw new RuntimeException("lookup[" + jndi + "] failed.", e);
        }
        if (bean == null) {
            log.error("lookup failed, maybe NO server alive or configure wrong.");
            throw new RuntimeException("lookup failed, maybe NO server alive or configure wrong.");
        }
        // 生成代理对象
        Object proxy = Proxy.newProxyInstance(bean.getClass().getClassLoader(),
                bean.getClass().getInterfaces(), new ReadyInvocationHandler(this, jndi, bean));
        // 将代理对象推入缓存
        cachedProxies.put(jndi, proxy);
        // 返回真正的bean（用于代理类invoke方法中重试请求）
        return bean;
    }

    /**
     * 清除缓存的代理对象
     */
    void clearCache() {
        cachedProxies.clear();
    }

    /**
     * 查找的分区
     *
     * @return
     */
    public String getPartitionName() {
        return this.environment.get("jnp.partitionName");
    }

    /**
     * 查找的组播地址
     *
     * @return
     */
    public String getDiscoveryGroup() {
        return this.environment.get("jnp.discoveryGroup");
    }

    /**
     * 查找的端口
     *
     * @return
     */
    public String getDiscoveryPort() {
        return this.environment.get("jnp.discoveryPort");
    }

    /**
     * 查找的TTL
     *
     * @return
     */
    public String getDiscoveryTTL() {
        return this.environment.get("jnp.discoveryTTL");
    }

    /**
     * 查找超时
     *
     * @return
     */
    public String getDiscoveryTimeout() {
        return this.environment.get("jnp.discoveryTimeout");
    }

    @Override
    public String toString() {
        return "{"
                + "\"partitionName\":\"" + getPartitionName() + "\","
                + "\"discoveryGroup\":\"" + getDiscoveryGroup() + "\","
                + "\"discoveryPort\":\"" + getDiscoveryPort() + "\","
                + "\"discoveryTTL\":\"" + getDiscoveryTTL() + "\","
                + "\"discoveryTimeout\":\"" + getDiscoveryTimeout() + "\","
                + "}";
    }
}
