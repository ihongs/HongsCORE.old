package app.hongs.action;

import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.hongs.Core;

/**
 * <h1>是否可执行动作</h1>
 * <pre>
 * Servlet Mapping: *.de
 * 如: /action/uri.do.de
 * </pre>
 * @author Hongs
 */
public class DetectAction
  extends HttpServlet
{

    @Override
    public void service(HttpServletRequest req, HttpServletResponse rsp)
    throws IOException, ServletException {
        PrintWriter out = rsp.getWriter();
        String act  = Core.ACTION_PATH.get();
               act  = act.substring(0, act.lastIndexOf('.') - 1); // 去掉扩展名
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
