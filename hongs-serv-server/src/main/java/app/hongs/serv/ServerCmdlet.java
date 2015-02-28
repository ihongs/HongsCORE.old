package app.hongs.serv;

import app.hongs.Core;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.cmdlet.CmdletHelper;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.util.Synt;
import java.util.Map;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 服务启动命令
 * @author Hongs
 */
@Cmdlet("server")
public class ServerCmdlet {

    @Cmdlet("start")
    public static void start(String[] args) throws HongsException {
        Map<String, Object> opts = CmdletHelper.getOpts(args,
            "port:i", "path:s", "help:b");

        if (Synt.declare(opts.get("help"), false)) {
            System.err.println("Usage: --port SERVICE_PORT --path CONTEXT_PATH");
            return;
        }

        WebAppContext webapp = new WebAppContext();
        Server server = new Server(Synt.declare(opts.get("port"),8080));
        webapp.setContextPath (Synt.declare(opts.get("path"),""));
        webapp.setDescriptor  (Core.BASE_PATH+"/WEB-INF/web.xml");
        webapp.setResourceBase(Core.CONT_PATH);
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

}
