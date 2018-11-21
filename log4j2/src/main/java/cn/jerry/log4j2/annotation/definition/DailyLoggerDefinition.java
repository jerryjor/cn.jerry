package cn.jerry.log4j2.annotation.definition;

import org.apache.logging.log4j.spi.StandardLevel;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface DailyLoggerDefinition {
    String basePackage();
    StandardLevel level() default StandardLevel.INFO;
    String fileName();
    String fileDailyPattern() default "yyyy-MM-dd";
    // String contentPattern() default "%d{yyyy-MM-dd HH:mm:ss.SSS} %p [%t] %c{1.} %m %ex%n";
    String contentTimePattern() default "yyyy-MM-dd HH:mm:ss.SSS";
    boolean contentWithLevel() default true;
    boolean contentWithThread() default true;
    boolean contentWithShortPackage() default true;
    boolean copy2Console() default false;
}
