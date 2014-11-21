package app.hongs.serv.action;

import app.hongs.Core;
import app.hongs.action.ActionRunner;
import app.hongs.action.ActionWarder;
import java.io.File;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 自动请求过滤
 * @author Hongs
 */
public class HaimAccessFilter implements Filter {

    private String config = "haim";         // 配置名称
    private String action = "haim/bottom";  // 处理动作

    public static final String CONFIG = "app.hongs.serv.haim.config";
    public static final String ENTITY = "app.hongs.serv.haim.entity";

    @Override
    public void init(FilterConfig cnf) {
        String x;

        x = cnf.getInitParameter("config");
        if (x != null) {
            config = x;
        }

        x = cnf.getInitParameter("action");
        if (x != null) {
            action = x;
        } else {
            action = config + "/bottom";
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
            throws IOException, ServletException {
        String pre, act, ext;
        int    pos;
        
        pre = (String) req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
        act = (String) req.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
        if (pre == null) {
            pre = ((HttpServletRequest)req).getServletPath();
            act = ((HttpServletRequest)req).getPathInfo();
        }
        
        pos = act.lastIndexOf('.');
        ext = act.substring(pos+1);
        act = act.substring(0,pos);

        if ("act".equals(ext)) {
            if (!ActionRunner.ACTIONS.containsKey(pre + act)) {
                doBottom(req, rsp, act, ext);
                return;
            }
        } else
        if ("htm".equals(ext)) {
            String  web = new File(Core.BASE_PATH).getParent()+File.separator+pre+File.separator+act;
            File    jsp;
            jsp = new File(web+".jsp");
            if ( jsp.exists()) {
                act  =  Core.BASE_HREF + pre + act + "." + ext ;
                req.getRequestDispatcher(act).forward(req, rsp);
                return;
            }
            jsp = new File(web+".htm");
            if (!jsp.exists()) {
                doBottom(req, rsp, act, ext);
                return;
            }
        }

        chain.doFilter(req, rsp);
    }

    private void doBottom(ServletRequest req, ServletResponse rsp, String act, String ext)
            throws ServletException, IOException {
        String uri;
        int    pos;
        pos = act.lastIndexOf('/');
        uri = act.substring(0,pos);
        act = act.substring(pos+1);
        act = Core.BASE_HREF+"/"+action+"/"+act+"."+ext;
        req.setAttribute(CONFIG, config);
        req.setAttribute(ENTITY, uri   );
        req.getRequestDispatcher(act).forward(req, rsp);
    }

    @Override
    public void destroy() {
    }
}
