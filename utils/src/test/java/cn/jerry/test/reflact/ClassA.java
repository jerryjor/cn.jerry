package cn.jerry.test.reflact;

public class ClassA {
    private String key = "dddd";

    public ClassA() {
        throw new RuntimeException("Default constructor is not avaliable!");
    }

    public ClassA(String key) {
        System.out.println("key:" + key);
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
