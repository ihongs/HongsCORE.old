package app.hongs.util;

import app.hongs.Core;
import app.hongs.CoreSerial;
import app.hongs.HongsException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 异步任务
 * 将任务放入后台, 当前线程可以继续去做其它事情
 * 适合那些不需要立即返回的操作, 如邮件发送程序
 * @author Hongs
 * @param <T> 任务的数据类型
 */
public abstract class Async<T> extends CoreSerial implements Core.Destroy {

    private File back = null;
    public  BlockingQueue<T> tasks;
    public transient ExecutorService servs;

    /**
     * @param name      任务集名称, 退出时保存现有任务待下次启动时执行, 为 null 则不保存
     * @param maxTasks  最多容纳的任务数量
     * @param maxServs  最多可用的线程数量
     * @throws HongsException
     */
    protected Async(String name, int maxTasks, int maxServs) throws HongsException {
        servs = Executors.newCachedThreadPool(  );
        tasks = new LinkedBlockingQueue(maxTasks);

        if (name != null) {
            back  = new File(Core.SERS_PATH + File.separator + name + ".async.ser");
            if (back.exists()) {
                load ( back );
            }
        }

        for(int i = 0; i < maxServs; i ++) {
            servs.execute(new Atask(this));
        }
    }

    @Override
    protected void imports() {
        // Nothing to do.
    }

    @Override
    public void destroy() throws HongsException {
        if (!servs.isShutdown( )) {
            servs.shutdownNow( );
        }

        if (back == null) {
            if (!tasks.isEmpty()) {
                throw HongsException.common(
                    "There is "+ tasks.size() +" task(s) not run.");
            }
            return;
        }

        File   file;
        file = back;
        back = null;

        if (!tasks.isEmpty()) {
            save ( file );
        } else
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
           this.destroy( );
        } finally {
          super.finalize();
        }
    }

    /**
     * 添加一个任务
     * @param data 
     */
    public void add(T data) {
        tasks.offer(  data);
    }

    /**
     * 执行一个任务
     * @param list 
     */
    abstract public void run(T data);

    private static class Atask implements Runnable {

        private final Async async;

        public Atask(Async async) {
            this.async = async;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    async.run(async.tasks.take());
                }
            } catch (InterruptedException e) {
                // Nothing to do.
            }
        }

    }

    /**
     * 测试方法
     * @param args
     */
    public static void main(String[] args) throws IOException, HongsException {
        app.hongs.cmdlet.CmdletRunner.init(args);

        Async a = new Async<String>("test", Integer.MAX_VALUE, 2) {

            @Override
            public void run(String x) {
                System.out.println("> "+Thread.currentThread().getId()+"\t"+x);
            }

        };

        java.util.Scanner input = new java.util.Scanner(System.in);
        System.out.println("input:");
        while (true ) {
            String x = input.nextLine().trim( );
            System.out.println("- "+Thread.currentThread().getId()+"\t"+x);
            if ("".equals(x)) {
                a.destroy( );
                break;
            }
            a.add( x);
        }
        int m = a.tasks.size();
        System.out.println("end!!!"+(m>0?m:""));
    }

}
