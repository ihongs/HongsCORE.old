package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;
import java.io.File;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet初始化
 * <pre>
 * 映射到 *.do *.de *.if *.js-conf *.js-lang *.jsp
 * 必须作为第一个 filter
 * </pre>
 * @author Hong
 */
public class ActionInit
implements Filter {

    @Override
    public void init(FilterConfig config)
    throws ServletException {
        ServletContext context = config.getServletContext();

        System.setProperty("file.encoding", "UTF-8");

        /** 静态属性配置 **/

        String str;

        // 调试开关
        str = context.getInitParameter( "debug" );
        if (str != null && Integer.parseInt(str ) > 0) {
            Core.IN_DEBUG_MODE = true;
        }

        // 当前应用根目录
        Core.BASE_HREF = context.getContextPath();
        Core.BASE_PATH = context.getRealPath("" ) + File.separator + "WEB-INF";

        // 资源目录
        Core.CONF_PATH = Core.BASE_PATH + File.separator + "conf";
        Core.LANG_PATH = Core.BASE_PATH + File.separator + "lang";
        Core.LOGS_PATH = Core.BASE_PATH + File.separator + "logs";
        Core.TMPS_PATH = Core.BASE_PATH + File.separator + "tmps";

        // 资源配置
        CoreConfig conf = (CoreConfig) Core.getInstance(app.hongs.CoreConfig.class);
        Core.LOGS_PATH = conf.getProperty("core.logs.dir", Core.LOGS_PATH);
        Core.TMPS_PATH = conf.getProperty("core.tmps.dir", Core.TMPS_PATH);
        Core.SERVER_ID = conf.getProperty("core.server.id", "0");

        if (Core.IN_DEBUG_MODE) {
            System.out.println(
                "--------------------------------------------------\r\n"
                + "SERVER_ID       : " + Core.SERVER_ID + "\r\n"
                + "BASE_HREF       : " + Core.BASE_HREF + "\r\n"
                + "BASE_PATH       : " + Core.BASE_PATH + "\r\n"
                + "CONF_PATH       : " + Core.CONF_PATH + "\r\n"
                + "LANG_PATH       : " + Core.LANG_PATH + "\r\n"
                + "LOGS_PATH       : " + Core.LOGS_PATH + "\r\n"
                + "TMPS_PATH       : " + Core.TMPS_PATH + "\r\n");
        }
    }

    private void doInit(ServletRequest request, ServletResponse response)
    throws ServletException {
        HttpServletRequest  req = (HttpServletRequest ) request ;
        HttpServletResponse rsp = (HttpServletResponse) response;

        CoreConfig conf = (CoreConfig)
            Core.getInstance(app.hongs.CoreConfig.class);
        ActionHelper helper = (ActionHelper)
            Core.getInstance(app.hongs.action.ActionHelper.class);
        helper.init( req, rsp );

        Core.ACTION_TIME.set(System.currentTimeMillis());
        Core.ACTION_PATH.set(req.getRequestURI().substring(Core.BASE_HREF.length()));
        Core.ACTION_LANG.set( conf.getProperty( "core.language.default", "en-us" ) );

        if (conf.getProperty("core.language.detect", false)) {
            /**
             * 语言可以记录到Session/Cookie里
             */
            String lang;
            String sess = conf.getProperty("core.language.session", "lang");
            lang = (String) helper.getSession(sess);
            if (lang == null || lang.length() == 0) {
                Cookie c = helper.getCookie(sess);
                lang = c == null ? null : c.getValue();
                if (lang == null || lang.length() == 0) {
                    lang = req.getHeader("Accept-Language");
                }
            }

            /**
             * 检查是否是支持的语言
             */
            if (lang != null) {
                lang = CoreLanguage.getAcceptLanguage(lang);
            }
            if (lang != null) {
                Core.ACTION_LANG.set(lang);
            }
        }

        if (Core.IN_DEBUG_MODE) {
            System.out.println(
                "--------------------------------------------------\r\n"
                + "THREAD_ID       : " + Thread.currentThread().getId() + "\r\n"
                + "ACTION_TIME     : " + Core.ACTION_TIME.get() + "\r\n"
                + "ACTION_PATH     : " + Core.ACTION_PATH.get() + "\r\n"
                + "ACTION_LANG     : " + Core.ACTION_LANG.get() + "\r\n"
                + "User Address    : " + req.getRemoteAddr() + " "
                                       + req.getRemotePort() + "\r\n");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws ServletException, IOException {
        this.doInit(request, response);
        try {
            chain.doFilter(request, response);
        } finally {
            this.doDestroy();
        }
    }

    private void doDestroy() {
        if (Core.IN_DEBUG_MODE) {
            Core core = Core.THREAD_CORE.get();
            long time = Core.ACTION_TIME.get();
            float secs = (float) (System.currentTimeMillis() - time) / 1000;
            System.out.println(
                "--------------------------------------------------\r\n"
                + "THREAD_ID       : " + Thread.currentThread().getId() + "\r\n"
                + "Used Seconds    : " + secs + "\r\n"
                + "Used Objects    : " + core.keySet().toString() + "\r\n");
        }

        Core.THREAD_CORE.get().destroy();
    }

    @Override
    public void destroy() {
        if (Core.IN_DEBUG_MODE) {
            Core core = Core.GLOBAL_CORE;
            long time = Core.GLOBAL_TIME;
            float secs = (float) (System.currentTimeMillis() - time) / 1000;
            System.out.println(
                "--------------------------------------------------\r\n"
                + "SERVER_ID       : " + Core.SERVER_ID + "\r\n"
                + "Used Seconds    : " + secs + "\r\n"
                + "Used Objects    : " + core.keySet().toString() + "\r\n");
        }

        Core.GLOBAL_CORE.destroy();
    }

}
