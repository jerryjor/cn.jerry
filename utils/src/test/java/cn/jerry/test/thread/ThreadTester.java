package cn.jerry.test.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTester {
	private static int index = 0;

	public static int getIndex() {
		return ++index;
	}

	public static int currIndex() {
		return index;
	}

	public static void main(String[] args) {
		// for (int i = 0; i < 3; i++) {
		// new Thread(new A()).start();
		// }
		// Object lock = B.class;
		// for (int i = 0; i < 3; i++) {
		// new Thread(new B(lock)).start();
		// }
		// for (int i = 0; i < 3; i++) {
		// new Thread(C.getInstance()).start();
		// }
		for (int j=0;j<1;j++) {
			new Thread(new B()).start();
		}
	}
}

class A implements Runnable {
	private int index = ThreadTester.getIndex();

	public void run() {
		doSomething();
	}

	private void doSomething() {
		System.out.println("current thread index : " + index + ", prepare to do something....");
		long st = System.currentTimeMillis();
		while(true) {
			if (System.currentTimeMillis() - st > 1000) {
				break;
			}
		}
		System.out.println("current thread index : " + index + ", finished!");
	}
}

class B implements Runnable {
	private int index = ThreadTester.getIndex();

	public void run() {
		ExecutorService exec = Executors.newFixedThreadPool(5);
		for (int i = 0; i < 50000; i++) {
			exec.execute(new A());
		}
		exec.shutdown();
	}

}
