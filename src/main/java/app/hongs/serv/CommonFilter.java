package app.hongs.serv;

import app.hongs.Core;
import app.hongs.action.ActionRunner;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 脚手架过滤器
 * 用于在构建应用前搭建打底处理,
 * 在页面和动作等建成后可以移除.
 * @author Hongs
 */
public class CommonFilter implements Filter {

    public static final String MODULE = "app.hongs.serv.common.module";
    public static final String ENTITY = "app.hongs.serv.common.entity";

    private String action;
    private String render;

    @Override
    public void init(FilterConfig cnf) {
        action = cnf.getInitParameter("action");
        render = cnf.getInitParameter("render");
        if (action == null) {
            action = "common";
        }
        if (render == null) {
            render =  action ;
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
            throws IOException, ServletException {
        String url, mod, ent, act, ext;
        int    pos;

        url = (String) req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
        if (url == null) {
            url = ((HttpServletRequest)req).getServletPath();
        }
        url = url.substring(1); // 去掉前导'/'

        String[] a = url.split("/");
        if (a.length > 0) {
            mod = a[0];
        } else {
            throw new ServletException("URL parse failed!");
        }
        if (a.length > 1) {
            ent = a[1];
        } else {
            ent = "";
        }
        if (a.length > 2) {
            act = a[2];
        } else {
            act = "default.html";
        }

        // 提取扩展名
        pos = act.lastIndexOf('.');
        ext = act.substring(pos+1);

        req.setAttribute(MODULE, mod);
        req.setAttribute(ENTITY, ent);

        if ("act".equals(ext)) {
            Map acts = ActionRunner.getActions();
            do {
                if (acts.containsKey(url.substring(0, url.length() - ext.length() - 1))) {
                    break;
                }

                act = action + "/" + act;
                if (acts.containsKey(act.substring(0, act.length() - ext.length() - 1))) {
                    req.getRequestDispatcher("/"+act).forward(req, rsp);
                    return;
                }
            } while (false);
        } else
        if ("jsp".equals(ext) || "html".equals(ext)) {
            String dir = new File(Core.BASE_PATH).getParent()+File.separator;
            File   fil;
            do {
                fil = new File(dir + url);
                if (fil.exists()) {
                    break;
                }

                url = act.substring(0, act.length() - ext.length()) + "jsp" ;
                url = render + "/" + url ;
                fil = new File(dir + url);
                if (fil.exists()) {
                    req.getRequestDispatcher("/"+url).forward(req, rsp);
                    return;
                }

                url = act.substring(0, act.length() - ext.length()) + "html";
                url = render + "/" + url ;
                fil = new File(dir + url);
                if (fil.exists()) {
                    req.getRequestDispatcher("/"+url).forward(req, rsp);
                    return;
                }
            } while (false);
        }

        chain.doFilter(req, rsp);
    }

    @Override
    public void destroy() {
    }
}
