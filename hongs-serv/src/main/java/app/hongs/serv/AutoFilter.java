package app.hongs.serv;

import app.hongs.Core;
import app.hongs.action.ActionDriver;
import app.hongs.action.ActionRunner;
import app.hongs.action.anno.Action;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
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
public class AutoFilter implements Filter {

    private String action;
    private String render;
    private Set<String> actset = null;

    @Override
    public void init(FilterConfig cnf) {
        action = cnf.getInitParameter("action");
        render = cnf.getInitParameter("render");
        if (action == null) {
            action = "/common";
        }
        if (render == null) {
            render =   action ;
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
            throws IOException, ServletException {
        String act, url, ext; int pos;

        // 当前路径
        url = ActionDriver.getCurrPath((HttpServletRequest) req);

        if (url.endsWith(".api")) {
            // 接口无需处理
        } else
        if (url.endsWith(".act")) {
            pos  =  url.lastIndexOf( '.' );
            try {
                ext = url.substring(  pos);
                act = url.substring(1,pos);
            } catch (IndexOutOfBoundsException ex) {
                // 如果无法拆分则直接跳过
                chain.doFilter ( req, rsp);
                return;
            }

            if(!ActionRunner.getActions().containsKey(act)) {
                for(String uri : getacts()) {
                    if(!act.endsWith(uri)) {
                        continue;
                    }
                    // 虚拟路径
                    req.setAttribute(ActionDriver.PATH, url);
                    // 转发请求
                    // 由于 include 内部无法设置 Header, 而 api 又需要在外层输出, 故采用 include 方式
                    if (ActionDriver.getRealPath((HttpServletRequest) req).endsWith(".api")) {
                        req.getRequestDispatcher(action+uri+ext).include(req, rsp);
                    } else {
                        req.getRequestDispatcher(action+uri+ext).forward(req, rsp);
                    }
                    return;
                }
            }
        } else {
            File file = new File(Core.BASE_PATH + url);
            if (!file.exists()) {
                String uri;

                uri = url.substring(url.lastIndexOf('/'));
                if (doForwar(req, rsp, url, uri)) {
                    return;
                }

                // .html 转发给 .jsp
                if (uri.endsWith(".html")) {
                    uri = uri.substring(0, uri.length() - 5) + ".jsp";
                    url = url.substring(0, url.length() - 5) + ".jsp";
                    if (doForwar(req, rsp, url, uri)) {
                        return;
                    }
                }
            }
        }

        chain.doFilter(req, rsp);
    }

    private boolean doForwar(ServletRequest req, ServletResponse rsp, String url, String uri)
            throws ServletException, IOException {
        File file = new File(Core.BASE_PATH +"/"+ render+uri);
        if (file.exists()) {
            // 虚拟路径
            req.setAttribute(ActionDriver.PATH, url);
            // 转发请求
            req.getRequestDispatcher ( render + uri).forward(req, rsp);
            return true ;
        } else {
            return false;
        }
    }

    private Set<String> getacts() {
        if (null != actset) {
            return  actset;
        }

        actset = new TreeSet(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.length() < o2.length() ? 1 : -1;
            }
        });

        // 使用 retrieve 动作获取 class
        // 也就是说, 即使不对外提供 retrieve 也要存在 retrieve 方法才行
        Class cls = ActionRunner.getActions()
        .get(action.substring(1)+"/retrieve")
        .getClassx();

        for(Method mtd : cls.getMethods( ) ) {
            Action ann = mtd.getAnnotation(Action.class);
            if (ann != null) {
                if (!"".equals(ann.value())) {
                    actset.add("/"+ann.value(  ));
                } else {
                    actset.add("/"+mtd.getName());
                }
            }
        }

        return actset;
    }

    @Override
    public void destroy() {
    }

}
