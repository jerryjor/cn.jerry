package cn.jerry.springboot.demo.db;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@MapperScan(basePackages = "cn.jerry.springboot.demo.db.dao.oracle.xxx", sqlSessionTemplateRef = "xxxOracleSst")
@PropertySource(value = {"classpath:jdbc2.properties"})
public class XxxOracleDsConfig {
    private static Logger logger = LogManager.getLogger();
    private static final String DRIVER_CLASS_NAME_ORACLE = "oracle.jdbc.driver.OracleDriver";
    // private static final String DRIVER_CLASS_NAME_MYSQL = "com.mysql.jdbc.Driver";
    private static final String PROPERTIE_KEY_ORACLE_XXX = "xxx";

    @Autowired
    private Environment env;

    @Bean(name = "xxxOracleDs")
    public DataSource getDataSource() {
        logger.info("initializing data source... PROPERTIE_KEY:{}", PROPERTIE_KEY_ORACLE_XXX);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DRIVER_CLASS_NAME_ORACLE);
        dataSource.setUrl(env.getProperty(PROPERTIE_KEY_ORACLE_XXX + ".url"));
        dataSource.setUsername(env.getProperty(PROPERTIE_KEY_ORACLE_XXX + ".username"));
        dataSource.setPassword(env.getProperty(PROPERTIE_KEY_ORACLE_XXX + ".password"));
        Properties prop = new Properties();
        prop.setProperty("initial-size", env.getProperty(PROPERTIE_KEY_ORACLE_XXX + ".initialSize"));
        prop.setProperty("max-active", env.getProperty(PROPERTIE_KEY_ORACLE_XXX + ".maxActive"));
        prop.setProperty("max-idle", env.getProperty(PROPERTIE_KEY_ORACLE_XXX + ".maxIdle"));
        prop.setProperty("min-idle", env.getProperty(PROPERTIE_KEY_ORACLE_XXX + ".minIdle"));
        dataSource.setConnectionProperties(prop);
        return dataSource;
    }

    @Bean(name = "xxxOracleTxm")
    public DataSourceTransactionManager getTransactionManager(@Qualifier("xxxOracleDs") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "xxxOracleSsf")
    public SqlSessionFactory getSqlSessionFactory(@Qualifier("xxxOracleDs") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:cn/jerry/springboot/demo/oracle/xxx/**/*Mapper.xml"));
        return bean.getObject();
    }

    @Bean(name = "xxxOracleSst")
    public SqlSessionTemplate getSqlSessionTemplate(@Qualifier("xxxOracleSsf") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
