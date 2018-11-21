package cn.jerry.test.generic;

import java.util.ArrayList;
import java.util.List;

public class GenericTester {
    public static void main(String[] args) {
        getList1();
    }

    private static void getList1() {
        List<Number> a = new ArrayList<Number>();
        for (int i = 0; i < 10; i++) {
            a.add(new Long(i));
        }
        System.out.println(getList2(a));
    }

    private static List<? super Number> getList2(List<? super Number> list) {
        List<? super Number> a = new ArrayList<Number>();
        for (Object o : list) {
            a.add((Number) o);
        }
        return a;
    }

}
