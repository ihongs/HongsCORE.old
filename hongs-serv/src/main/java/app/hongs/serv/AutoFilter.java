package app.hongs.serv;

import app.hongs.Core;
import app.hongs.action.ActionRunner;
import app.hongs.action.ActionWarder;
import static app.hongs.action.ActionWarder.PATH;
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
    private Set<String  > actset = null;

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
        String act, url, ext; int pos;

        // 当前路径
        url = ActionWarder.getCurrPath((HttpServletRequest) req);

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
                for(String uri : actSet()) {
                    if(!act.endsWith(uri)) {
                        continue;
                    }
                    // 虚拟路径
                    req.setAttribute(PATH, url);
                    // 转发请求
                    req.getRequestDispatcher("/"+action+uri+ext).include(req, rsp);
                    return;
                }
            }
        } else {
            File file = new File(Core.WEBS_PATH + url);
            if (!file.exists()) {
                String uri = url.substring(url.lastIndexOf('/'));
                file = new File(Core.WEBS_PATH + "/"+render+uri);
                if (file.exists()) {
                    // 虚拟路径
                    req.setAttribute(PATH, url);
                    // 转发请求
                    req.getRequestDispatcher(/**/"/"+render+uri).forward(req, rsp);
                    return;
                }
            }
        }

        chain.doFilter(req, rsp);
    }

    @Override
    public void destroy() {
    }

    private Set<String> actSet() {
        if (null != actset) {
            return  actset;
        }

        actset = new TreeSet(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.length() < o2.length() ? 1 : -1;
            }
        });

        Class cls = ActionRunner.getActions().get(action+"/retrieve")
                                             .getDeclaringClass(/**/);
        for(Method mtd : cls.getMethods()) {
            Action ann = mtd.getAnnotation(Action.class);
            if (ann != null ) {
               actset.add("/"+ann.value());
            }
        }

        return actset;
    }

}
