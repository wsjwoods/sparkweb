package com.spark.basic.multithread;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Semaphore也是一个线程同步的辅助类，可以维护当前访问自身的线程个数，并提供了同步机制。使用Semaphore可以控制同时访问资源的线程个数，例如，实现一个文件允许的并发访问数。
 * <p>
 * Semaphore的主要方法摘要：
 * <p>
 * 　　void acquire():从此信号量获取一个许可，在提供一个许可前一直将线程阻塞，否则线程被中断。
 * <p>
 * 　　void release():释放一个许可，将其返回给信号量。
 * <p>
 * 　　int availablePermits():返回此信号量中当前可用的许可数。
 * <p>
 * 　　boolean hasQueuedThreads():查询是否有线程正在等待获取。
 */
public class SemaphoreTest {
    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool();
        final Semaphore sp = new Semaphore(3);//创建Semaphore信号量，初始化许可大小为3
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        sp.acquire();//请求获得许可，如果有可获得的许可则继续往下执行，许可数减1。否则进入阻塞状态
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println("线程" + Thread.currentThread().getName() +
                            "进入，当前已有" + (3 - sp.availablePermits()) + "个并发");
                    try {
                        Thread.sleep((long) (Math.random() * 10000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("线程" + Thread.currentThread().getName() +
                            "即将离开");
                    sp.release();//释放许可，许可数加1
                    //下面代码有时候执行不准确，因为其没有和上面的代码合成原子单元
                    System.out.println("线程" + Thread.currentThread().getName() +
                            "已离开，当前已有" + (3 - sp.availablePermits()) + "个并发");
                }
            };
            service.execute(runnable);
        }
    }

}

/**
 * 单个信号量的Semaphore对象可以实现互斥锁的功能，并且可以是由一个线程获得了“锁”，再由另一个线程释放“锁”，这可应用于死锁恢复的一些场合。
 */
class LockTest {
    public static void main(String[] args) {
        final Business business = new Business();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            executor.execute(
                    new Runnable() {
                        public void run() {
                            business.service();
                        }
                    }

            );
        }
        executor.shutdown();
    }

    private static class Business {
        private int count;
        Lock lock = new ReentrantLock();
        Semaphore sp = new Semaphore(1);

        public void service() {
            //lock.lock();
            try {
                System.out.println(sp.availablePermits());
                sp.acquire(); //当前线程使用count变量的时候将其锁住，不允许其他线程访问
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            try {
                count++;
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(new Date().getSeconds() + ":" + count);
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                //lock.unlock();
                sp.release();  //释放锁
            }
        }
    }
}

