package app.hongs.action;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.hongs.Core;
import java.io.PrintWriter;

/**
 * <h1>是否可执行动作</h1>
 * <pre>
 * Servlet Mapping: *.de
 * 如: /action/uri.do.de
 * </pre>
 * @author Hongs
 */
public class DetectAction
  extends AbstractServlet
{

    @Override
    public void _actService(HttpServletRequest req, HttpServletResponse rsp)
    throws ServletException, IOException {
        Core core = Core.getInstance();
        PrintWriter out = rsp.getWriter();
        //int pos = core.ACTION.lastIndexOf('.') - 1;
        //String act = core.ACTION.substring(0, pos);
        String act = core.ACTION.substring( 0 ,
                     core.ACTION.length() - 3);
        String conf = req.getParameter("conf");
        String sess = req.getParameter("sess");
        String logn = req.getParameter("logn");
        if (conf == null || "".equals(conf)) conf = "default";
        if (sess == null || "".equals(sess)) sess = "actions";
        if (ActionFilter.checkAction(act, conf, sess, "1".equals(logn))) {
            out.print("OK");
        }
        else {
            out.print("NO");
        }
    }

}
