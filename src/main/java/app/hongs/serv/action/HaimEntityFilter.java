package app.hongs.serv.action;

import app.hongs.Core;
import app.hongs.action.ActionRunner;
import app.hongs.action.ActionWarder;
import java.io.File;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 自动请求过滤
 * @author Hongs
 */
public class HaimEntityFilter implements Filter {

    private String config = "haim";         // 配置名称
    private String prefix = "haim/";        // 过滤前缀
    private String action = "haim/auto";  // 处理动作
    private int    prelen = prefix.length();

    public static final String CONFIG = "app.hongs.serv.haim.config";
    public static final String ENTITY = "app.hongs.serv.haim.entity";
    
    @Override
    public void init(FilterConfig cnf) {
        String x;

        x = cnf.getInitParameter("config");
        if (x != null) {
            config = x;
        }

        x = cnf.getInitParameter("prefix");
        if (x != null) {
            prefix = x;
        } else {
            prefix = config + "/";
        }

        x = cnf.getInitParameter("action");
        if (x != null) {
            action = x;
        } else {
            action = prefix + "common";
        }

        prelen = prefix.length();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
            throws IOException, ServletException {
        String uri, act, ext;
        int pos;
        uri = ActionWarder.getCurrentServletPath((HttpServletRequest)req);
        act = uri.substring(prelen);
        pos = act.lastIndexOf('.');
        ext = act.substring(pos+1);
        act = act.substring(0,pos);

        if ("jsp".equals(ext)) {
            String  web = new File(Core.BASE_PATH).getParent();
            File    jsp = new File(web + File.separator + uri);
            if (!jsp.exists()) {
                doAction( req, rsp, act, ext );
                return;
            }
        } else
        if ("act".equals(ext)) {
            if (ActionRunner.ACTIONS.containsKey(act)) {
                doAction( req, rsp, act, ext );
                return;
            }
        }

        chain.doFilter(req, rsp);
    }

    private void doAction(ServletRequest req, ServletResponse rsp, String act, String ext)
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
