package cn.jerry.fabric.api.tools.test;

import java.util.ArrayList;
import java.util.List;

public class PeerHealthyCacheTester {
    public static void main(String[] args) {
        List<DemoHealthy> list = new ArrayList<>();
        Object obj = "sdfdds";
        list.add(new DemoHealthy(obj, 1000L));
        DemoHealthy demo = new DemoHealthy(obj, 1001L);
        list.remove(demo);
        System.out.println(list.size());
    }

    static class DemoHealthy implements Comparable<DemoHealthy> {
        private Object obj;
        private long lastRespMs;

        DemoHealthy(Object obj, long lastRespMs) {
            this.obj = obj;
            this.lastRespMs = lastRespMs;
        }

        @Override
        public int compareTo(DemoHealthy o) {
            return Long.compare(this.lastRespMs, o.lastRespMs);
        }

        @Override
        public int hashCode() {
            return this.obj.toString().hashCode();
        }

        @Override
        public boolean equals(Object target) {
            return target instanceof DemoHealthy && this.obj.toString().equals(((DemoHealthy) target).obj.toString());
        }
    }
}
