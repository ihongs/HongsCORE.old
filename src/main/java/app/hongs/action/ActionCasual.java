package app.hongs.action;

import app.hongs.Core;
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
 * 临时工/脚手架 过滤器
 * 用于在构建应用前搭建打底处理,
 * 在页面和动作等建成后可以移除.
 * @author Hongs
 */
public class ActionCasual implements Filter {

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
        if (url.endsWith("/")) {
            url += "default.html";
        }

            pos = url.lastIndexOf(".");
            ext = url.substring(pos+1);
        try {
            url = url.substring(0,pos);
        } catch (IndexOutOfBoundsException ex) {
            // 如果无法拆分则直接跳过
            chain.doFilter (req , rsp);
            return;
        }

            pos = url.lastIndexOf("/");
            act = url.substring(pos+1);
        try {
            mod = url.substring(0,pos);
        } catch (IndexOutOfBoundsException ex) {
            // 如果无法拆分则直接跳过
            chain.doFilter (req , rsp);
            return;
        }

            pos = mod.lastIndexOf("/");
            ent = mod.substring(pos+1);
        try {
            mod = mod.substring(0,pos);
        } catch (IndexOutOfBoundsException ex) {
            // 无法拆分则认为没有实体
            mod = ent;
            ent = "" ;
        }

        if ("act".equals(ext)) {
            Map acts = ActionRunner.getActions();
            do {
                if (acts.containsKey(url)) {
                    break;
                }

                req.setAttribute(MODULE , mod);
                req.setAttribute(ENTITY , ent);

                url = action + "/" + act;
                if (acts.containsKey(url)) {
                    url= url + "." + ext;
                    req.getRequestDispatcher("/"+url).include(req, rsp);
                    return;
                }
            } while (false);
        } else
        if ("jsp".equals(ext) || "html".equals(ext)) {
            String dir = new File(Core.BASE_PATH).getParent()+File.separator;
            File   fil;
            do {
                fil = new File(dir + url + "." + ext);
                if (fil.exists()) {
                    break;
                }

                // 默认的 default 页是无实体的
                if ("default".equals(act) && !"".equals(ent)) {
                    mod = mod +"/" + ent;
                    ent = "" ;
                }

                req.setAttribute(MODULE , mod);
                req.setAttribute(ENTITY , ent);
                
                url = render + "/" + act + ".jsp" ;
                fil = new File(dir + url);
                if (fil.exists()) {
                    req.getRequestDispatcher("/"+url).include(req, rsp);
                    return;
                }

                url = render + "/" + act + ".html";
                fil = new File(dir + url);
                if (fil.exists()) {
                    req.getRequestDispatcher("/"+url).include(req, rsp);
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
