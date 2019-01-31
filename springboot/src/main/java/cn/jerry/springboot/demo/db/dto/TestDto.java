package cn.jerry.springboot.demo.db.dto;

import java.io.Serializable;

public class TestDto implements Serializable {
    private static final long serialVersionUID = -1225577725474801162L;

    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "TestDto{" +
                "result=" + result +
                '}';
    }
}

