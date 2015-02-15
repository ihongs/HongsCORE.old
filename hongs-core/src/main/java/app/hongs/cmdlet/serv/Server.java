package app.hongs.cmdlet.serv;

import app.hongs.Core;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.ActionRunner;
import app.hongs.cmdlet.CmdletHelper;
import app.hongs.cmdlet.CmdletRunner;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.util.Synt;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * HTTP 服务器
 * @author Hongs
 */
@Cmdlet("server")
public class Server {

    @Cmdlet("start")
    public static void start(String[] args) throws HongsException {
        Map<String, Object> opts = CmdletHelper.getOpts(args,
            "port:i", "path:s", "help:b");

        if (Synt.declare(opts.get("help"), false)) {
            System.err.println("Usage: --port SERVICE_PORT --path CONTEXT_PATH");
            return;
        }

        WebAppContext webapp = new WebAppContext();
        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(
                               Synt.declare(opts.get("port"),8080));
        webapp.setContextPath (Synt.declare(opts.get("path"),""));
        webapp.setDescriptor  (Core.BASE_PATH+"/WEB-INF/web.xml");
        webapp.setResourceBase(Core.WEBS_PATH);
        webapp.setParentLoaderPriority( true );
        server.setHandler(webapp);


        Runtime.getRuntime().addShutdownHook(new StopServer(server));
        try {
            server.start();
            server.join( );
        }
        catch (Exception ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    @Cmdlet("show-actions")
    public static void showActions(String[] args) {
      Map<String, Method> actions = new TreeMap(new PropComparator());
      actions.putAll(ActionRunner.getActions());
      System.out.println("Actions:");
      for (Map.Entry<String, Method> et : actions.entrySet()) {
        String a = et.getKey(  );
        Method m = et.getValue();
        System.out.println("  "+a+"\t"+m.getDeclaringClass().getName()+"."+m.getName());
      }
    }

    @Cmdlet("show-cmdlets")
    public static void showCmdlets(String[] args) {
      Map<String, Method> actions = new TreeMap(new PropComparator());
      actions.putAll(CmdletRunner.getCmdlets());
      System.out.println("Cmdlets:");
      for (Map.Entry<String, Method> et : actions.entrySet()) {
        String a = et.getKey(  );
        Method m = et.getValue();
        System.out.println("  "+a+"\t"+m.getDeclaringClass().getName()+"."+m.getName());
      }
    }

    @Cmdlet("show-properties")
    public static void showProperties(String[] args) {
      Map<String, String> env = new TreeMap(new PropComparator());
      env.putAll(new HashMap(System.getProperties()));
      System.out.println("Properties:");
      for (Map.Entry<String, String> et : env.entrySet()) {
        String k = et.getKey(  );
        String v = et.getValue();
        System.out.println("  "+k+"\t"+v);
      }
    }

    @Cmdlet("show-env")
    public static void showENV(String[] args) {
      Map<String, String> env = new TreeMap(new PropComparator());
      env.putAll(new HashMap(System.getenv()));
      System.out.println("ENV:");
      for (Map.Entry<String, String> et : env.entrySet()) {
        String k = et.getKey(  );
        String v = et.getValue();
        System.out.println("  "+k+"\t"+v);
      }
    }

    private static class StopServer extends Thread {

        private final/**/ org.eclipse.jetty.server.Server server;

        public StopServer(org.eclipse.jetty.server.Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            if (server.isStopped( )) {
                System.err.println("Server is stopped !!!");
                return;
            }
            if (server.isStopping()) {
                System.err.println("Server is stopping...");
                return;
            }
            try {
                server.stop();
            } catch (Exception ex) {
                throw new HongsError(HongsError.COMMON, ex);
            }
        }

    }

    private static class PropComparator implements Comparator<String> {

        public int compare(String s1, String s2) {
            return s1.compareTo(  s2  );
        }

    }

}
