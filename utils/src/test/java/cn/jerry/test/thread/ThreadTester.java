package cn.jerry.test.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadTester {
    private static final Lock LOCK = new ReentrantLock();

    private static final int POOL_SIZE = 10;
    private static int blocking = POOL_SIZE * -1;
    private static Map<Long, Integer> indexes = new HashMap<>();// merchantId, index

    private void updateIndex(Long merId, Integer index) {
        LOCK.lock();
        indexes.put(merId, index);
        // 丢缓存
        LOCK.unlock();
    }

    private void removeIndex(Long merId) {
        LOCK.lock();
        indexes.remove(merId);
        // 丢缓存
        LOCK.unlock();
    }

    public static void main(String[] args) {
        ExecutorService executors = Executors.newFixedThreadPool(POOL_SIZE);
        // 先读缓存，拿到异常终止的商家，new出thread来执行
        // 再启动如下循环查询新商家
        int page = 1; // cache
        while (true) {
            // 取商家id，分页， page每页1个， 取不到，break
            if (blocking > 0) {
                System.out.println("blocking....");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            System.out.println("adding....");
            executors.execute(new TestThread(1L,1));
        }
        //executors.shutdown();
    }

    static class TestThread extends Thread {
        private static final Lock LOCK = new ReentrantLock();

        TestThread(Long merId, Integer index) {
            // 根据商家id取list，执行
            LOCK.lock();
            blocking++;
            LOCK.unlock();
        }

        @Override
        public void run() {
            // cache Map<merhcantId, 合同标记> ，跑完删key
            try {
                Thread.sleep(new Random().nextInt(5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOCK.lock();
            blocking--;
            LOCK.unlock();
        }
    }
}
