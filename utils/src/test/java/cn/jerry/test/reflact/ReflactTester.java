package cn.jerry.test.reflact;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflactTester {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        test2();
        
    }

    private static void test1() throws InstantiationException, IllegalAccessException {
        Class<ClassA> c = ClassA.class;
        ClassA a = c.newInstance();
        System.out.println(a.getKey());
    }

    private static void test2() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        Class<ClassA> c = ClassA.class;
        for (Method m : c.getMethods()) {
            if ("getKey".equals(m.getName())) {
                Object o = m.invoke(c.newInstance());
                System.out.println(o);
            }
        }
    }
}
