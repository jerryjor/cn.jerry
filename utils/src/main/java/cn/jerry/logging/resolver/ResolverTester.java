package cn.jerry.logging.resolver;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public abstract class ResolverTester {

    public boolean matchesAnnotation(Class<?> type) {
        if (type == null) return false;
        if (getAnnotationClass() == null) return true;
        return type.getAnnotation(getAnnotationClass()) != null;
    }

    public abstract Class<? extends Annotation> getAnnotationClass();

    public abstract String[] getBasePackages();

    public boolean matchesPkg(String className) {
        if (className == null) return false;
        if (getBasePackages() == null || getBasePackages().length == 0) return true;
        int end = className.lastIndexOf(".");
        if (end <= 0) return false;
        for (String pkg : getBasePackages()) {
            if (pkg != null && className.substring(0, end).startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder(this.getClass().getName()).append("{")
                .append("annotation:").append(getAnnotationClass().getName()).append(",")
                .append("basePackage:").append(Arrays.toString(getBasePackages()))
                .append("}").toString();
    }
}
