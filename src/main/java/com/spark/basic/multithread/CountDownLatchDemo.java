package com.spark.basic.multithread;

    /**
     CountDownLatch类是一个同步计数器,构造时传入int参数,该参数就是计数器的初始值，每调用一次countDown()方法，计数器减1,计数器大于0 时，await()方法会阻塞程序继续执行
     CountDownLatch如其所写，是一个倒计数的锁存器，当计数减至0时触发特定的事件。利用这种特性，可以让主线程等待子线程的结束。下面以一个模拟运动员比赛的例子加以说明。
     */
  import java.util.concurrent.CountDownLatch;
  import java.util.concurrent.ExecutorService;
  import java.util.concurrent.Executors;

    public class CountDownLatchDemo {
        private static final int WORKER_AMOUNT = 6;
        public CountDownLatchDemo() {
        }
        /**以程序员写项目敲代码为例，6个人同时敲代码，所有人都敲完代码，项目才算完成。
         * @param args
         */
        public static void main(String[] args) {
            //对于Worker，CountDownLatch减1后即结束
            CountDownLatch begin = new CountDownLatch(1);
            //对于整个项目，所有Worker结束后才算结束
            CountDownLatch end = new CountDownLatch(WORKER_AMOUNT);
            Worker[] workers = new Worker[WORKER_AMOUNT];

            for(int i=0;i<WORKER_AMOUNT;i++)
                workers[i] = new Worker(i+1,begin,end);

            //设置特定的线程池，大小为6
            ExecutorService exe = Executors.newFixedThreadPool(WORKER_AMOUNT);
            for(Worker w:workers) {
                exe.execute(w);            //分配线程
            }
            System.out.println("start working!");
            begin.countDown();            //bgein的计数器减一，变成0，6个Worker同时敲。
            try{
                end.await();            //等待end状态变为0，即为项目完成
            }catch (InterruptedException e) {
                // TODO: handle exception
                e.printStackTrace();
            }finally{
                System.out.println("Project complete!");
            }
            exe.shutdown();
        }
    }


 class Worker implements Runnable {

    private int id;
    private CountDownLatch begin;
    private CountDownLatch end;
    public Worker(int i, CountDownLatch begin, CountDownLatch end) {
        // TODO Auto-generated constructor stub
        super();
        this.id = i;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        long runtime;
        try{
            begin.await();        //等待begin的状态为0
            runtime = (long)(Math.random()*1000);
            Thread.sleep(runtime);    //随机分配时间，即worker完成时间
            System.out.println("use " + runtime + ". Play"+id+" arrived.");
        }catch (InterruptedException e) {
            // TODO: handle exception
            e.printStackTrace();
        }finally{
            end.countDown();    //Worker完成一个，使end状态减1，6个都完成，最终减至0
        }
    }
}
