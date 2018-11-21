package cn.jerry.test.timer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 定时器
 * 
 * @author zhaojiarui
 * 
 */
public abstract class AbstractTask extends TimerTask {
    protected static final Timer TIMER = new Timer();
    protected Integer frequence;

    /**
     * 获取定时参数, 单位：秒
     * 
     * @return
     */
    public Integer getFrequence() {
        if (frequence == null) {
            frequence = 5 * 60;
        }
        return frequence;
    }

    public void start() {
        System.out.println("timer is starting... frequence: " + this.getFrequence() + " min.");
        TIMER.schedule(this, 1000, this.getFrequence() * 1000);
    }
}
