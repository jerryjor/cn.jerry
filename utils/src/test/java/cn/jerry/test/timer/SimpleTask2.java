package cn.jerry.test.timer;

public class SimpleTask2 extends AbstractTask {

    @Override
    public Integer getFrequence() {
        return 1;
    }

    @Override
    public void run() {
        System.out.println("2 running...");
    }

}
