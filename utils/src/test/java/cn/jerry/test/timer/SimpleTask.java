package cn.jerry.test.timer;

public class SimpleTask extends AbstractTask {

    @Override
    public Integer getFrequence() {
        return 1;
    }

    @Override
    public void run() {
        System.out.println("1 running...");
    }

}
