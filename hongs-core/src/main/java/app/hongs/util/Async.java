package app.hongs.util;

import app.hongs.Core;
import app.hongs.CoreSerial;
import app.hongs.HongsException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 异步任务
 * @author Hongs
 * @param <T>
 */
public abstract class Async<T> extends CoreSerial implements Core.Destroy {

    private File backFile  =  null;
    public  ExecutorService  servs;
    public  BlockingQueue<T> tasks;

    public Async(String backName, int maxTasks, int maxServs) throws HongsException {
        servs = Executors.newCachedThreadPool(  );
        tasks = new LinkedBlockingQueue(maxTasks);

        if (backName != null) {
            backFile  = new File(Core.SERS_PATH + File.separator + backName + ".async.ser");
            if (backFile.exists()) {
                load ( backFile );
            }
        }

        for (int i = 0; i < maxServs; i ++) {
            servs.execute(new Atask(this) );
        }
    }

    @Override
    public void destroy() throws HongsException {
        if (backFile != null) {
            if (! tasks.isEmpty()) {
                save ( backFile );
            } else
            if (backFile.exists()) {
                backFile.delete();
            }
        }
    }

    @Override
    protected void imports() {
        // Nothing to do.
    }

    public void add(T data) {
        tasks.offer(  data);
    }

    abstract public void run(T data);

    public static class Atask implements Runnable {

        private final Async async;

        public  Atask(Async async) {
            this.async = async;
        }

        @Override
        public  void  run() {
            while (true) {
                async.run(async.tasks.poll());
            }
        }

    }

    /**
     * 测试方法
     * @param args
     */
    public static void main(String[] args) throws IOException, HongsException {
        app.hongs.cmdlet.CmdletRunner.init(args);
        
        Async a = new Async<String>("test", 1000, 1) {

            @Override
            public void run(String data) {
                System.out.println("---"+data);
            }
            
        };
        
        a.add("123123");
        a.add("123aljdsl123");
        a.add("1231d3");
        a.add("123qewr3");
        a.add("sdfsdfs3123");
    }

}
