package app.hongs.serv;

import app.hongs.Core;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.util.Synt;
import java.io.File;
import java.io.IOException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;

/**
 * 服务启动命令
 * @author Hongs
 */
@Cmdlet("server")
public class ServerCmdlet {

    @Cmdlet("start")
    public static void start(String[] args) throws HongsException {
        int port = args.length > 0 ? Synt.declare(args[0], 8080) : 8080;

        String conf = Core.CORE_PATH + File.separator + "web.xml";
        if(!(new File(conf)).exists()) {
               conf = Core.CONF_PATH + File.separator + "web.xml";
        }

        WebAppContext webapp = new WebAppContext();
        webapp.setDescriptor  (conf);
        webapp.setContextPath (Core.BASE_HREF);
        webapp.setResourceBase(Core.BASE_PATH);
        webapp.setParentLoaderPriority( true );

        // 设置会话
        try {
            File sd = new File( Core.DATA_PATH + "/sesion" );
            if (!sd.exists()) {
                 sd.mkdirs();
            }
            HashSessionManager sm = new HashSessionManager();
            sm.setStoreDirectory(sd);
//          sm.setSessionCookie("SID");
//          sm.setSessionIdPathParameterName("sid");
            SessionHandler     sh = new SessionHandler( sm );
            webapp.setSessionHandler(sh);
        }
        catch (IOException ex) {
            throw new HongsException.Common(ex);
        }

        Server server = new Server(port);
        server.setHandler(webapp);

        Runtime.getRuntime().addShutdownHook(new StopServer(server));
        try {
            server.start();
            server.join( );
        }
        catch (Exception ex) {
            throw new HongsException.Common(ex);
        }
    }

    private static class StopServer extends Thread {

        private final/**/ org.eclipse.jetty.server.Server server;

        public StopServer(org.eclipse.jetty.server.Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            System.out.println("");
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
                throw new HongsError.Common(ex);
            }
        }

    }

}
