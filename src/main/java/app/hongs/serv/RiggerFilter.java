package app.hongs.serv;

import app.hongs.Core;
import app.hongs.HongsException;
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
 * 在页面和动作等建成后可以移除;
 * 默认调用通用信息模块(hcim)进行处理.
 * @author Hongs
 */
public class RiggerFilter implements Filter {

    private String action = "hcim"; // 处理动作
    private String config = "hcim"; // 配置名称
    private String prefix;          // 路径前缀
    private int    prelen;          // 前缀长度

    public static final String CONFIG = "app.hongs.serv.common.config";
    public static final String PREFIX = "app.hongs.serv.common.prefix";
    public static final String ENTITY = "app.hongs.serv.common.entity";

    @Override
    public void init(FilterConfig cnf) {
        String x;

        x = cnf.getInitParameter("action");
        if (x != null) {
            action = x;
        }

        x = cnf.getInitParameter("config");
        if (x != null) {
            config = x;
        }

        x = cnf.getInitParameter("prefix");
        if (x != null) {
            prefix = x;
            prelen = x.length() + 2;
        } else {
            prefix = config;
            prelen = config.length() + 2;
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
            throws IOException, ServletException {
        String url, act, ext, ett;
        int    pos;

        url = (String) req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
        if (url == null) {
            url = ((HttpServletRequest)req).getServletPath();
        }
        if (url.length() <  prelen ) {
            url += "/index.jsp";
        } else
        if (url.length() == prelen ) {
            url +=  "index.jsp";
        }

        // 去掉前导'/'
        url = url.substring(1 );

        // 提取扩展名
        pos = url.lastIndexOf('.');
        ext = url.substring(pos+1);

        // 提取动作名
        pos = url.lastIndexOf('/');
        act = url.substring(pos+1);

        // 提取实体名
        if (prelen - 2 < pos ) {
            ett = url.substring(prelen - 1, pos);
        } else {
            ett = "";
        }

        req.setAttribute(CONFIG, config);
        req.setAttribute(PREFIX, prefix);
        req.setAttribute(ENTITY, ett   );

        if ("act".equals(ext)) {
            Map acts;
            try {
                acts = ActionRunner.getActions();
            } catch (HongsException ex) {
                throw new ServletException( ex );
            }
            do {
                if ( acts.containsKey(url)) {
                    break;
                }

                act  = action + "/" + act ;
                if (!acts.containsKey(act)) {
                    break;
                }

                req.getRequestDispatcher("/"+act).forward(req, rsp);
                return;
            } while (false);
        } else
        if ("jsp".equals(ext) || "html".equals(ext)) {
            String dir = new File(Core.BASE_PATH).getParent()+File.separator;
            File   fil;
            do {
                fil  = new File(dir + url);
                if ( fil.exists()) {
                    break;
                }

                act  = act.substring(0, act.length() - ext.length()) + "jsp";
                act  = action + "/" + act ;
                fil  = new File(dir + act);
                if (!fil.exists()) {
                    break;
                }

                req.getRequestDispatcher("/"+act).forward(req, rsp);
                return;
            } while (false);
        }

        chain.doFilter(req, rsp);
    }

    @Override
    public void destroy() {
    }
}
