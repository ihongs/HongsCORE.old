package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;
import app.hongs.CoreLogger;
import app.hongs.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 动作承载器
 *
 * <p>
 * 映射到 *.act *.api *.jsp /common/auth/* /common/conf/* /common/lang/*<br/>
 * 必须作为第一个 filter
 * </p>
 *
 * @author Hong
 */
public class ActionHolder
implements Filter {

    @Override
    public void init(FilterConfig config)
    throws ServletException {
        ServletContext context = config.getServletContext();

        System.setProperty("file.encoding", "UTF-8");

        /** 静态属性配置 **/

        String str;

        // 调试开关
        str = context.getInitParameter ("debug");
        if (str != null) {
            Core.DEBUG = Byte.parseByte(  str  );
        }

        Core.ENVIR = 1; // 标明为 Action 模式

        // 当前应用根目录
        Core.BASE_HREF = context.getContextPath();
        Core.BASE_PATH = context.getRealPath("" ) + File.separator + "WEB-INF";

        // 资源目录
        Core.CONF_PATH = Core.BASE_PATH + File.separator + "conf";
        Core.LANG_PATH = Core.BASE_PATH + File.separator + "lang";
        Core.LOGS_PATH = Core.BASE_PATH + File.separator + "logs";
        Core.TMPS_PATH = Core.BASE_PATH + File.separator + "tmps";

        // 资源配置
        CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
        Core.LOGS_PATH = conf.getProperty("core.logs.dir", Core.LOGS_PATH);
        Core.TMPS_PATH = conf.getProperty("core.tmps.dir", Core.TMPS_PATH);
        Core.SERVER_ID = conf.getProperty("core.server.id", "" );

        if (0 < Core.DEBUG) {
            CoreLogger.debug("...\r\n"
                + "SERVER_ID       : " + Core.SERVER_ID + "\r\n"
                + "BASE_HREF       : " + Core.BASE_HREF + "\r\n"
                + "BASE_PATH       : " + Core.BASE_PATH + "\r\n"
                + "CONF_PATH       : " + Core.CONF_PATH + "\r\n"
                + "LANG_PATH       : " + Core.LANG_PATH + "\r\n"
                + "LOGS_PATH       : " + Core.LOGS_PATH + "\r\n"
                + "TMPS_PATH       : " + Core.TMPS_PATH + "\r\n");
        }
    }

    @Override
    public void destroy() {
        if (1 == (1 & Core.DEBUG)) {
            Core core = Core.GLOBAL_CORE;
            long time = System.currentTimeMillis() - Core.GLOBAL_TIME;
            CoreLogger.debug("...\r\n"
                + "SERVER_ID       : " + Core.SERVER_ID + "\r\n"
                + "Runtime         : " + Text.humanTime(time) + "\r\n"
                + "Objects         : " + core.keySet().toString() + "\r\n");
        }

        try {
            Core.GLOBAL_CORE.destroy( );
        } catch ( Throwable  e) {
            CoreLogger.error(e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws ServletException, IOException {
        try {
            this .doFilter(request, response);
            chain.doFilter(request, response);

            // 将返回数据转换成JSON格式
            if (((HttpServletResponse)response).getStatus() == HttpServletResponse.SC_OK) {
                ActionHelper helper = (ActionHelper)
                      Core.getInstance(ActionHelper.class);
                Map data  = helper.getResponseData();
                if (data != null) {
                    helper.print(data);
                }
            }
        } finally {
            this .doDestroy();
        }
    }

    private void doFilter(ServletRequest request, ServletResponse response)
    throws ServletException {
        HttpServletRequest  req = (HttpServletRequest ) request ;
        HttpServletResponse rsp = (HttpServletResponse) response;

        ActionHelper helper = new ActionHelper(req, rsp);
        Core.getInstance().put(ActionHelper.class.getName(), helper);
        CoreConfig conf = (CoreConfig) Core.getInstance(CoreConfig.class);

        Core.ACTION_PATH.set(req.getServletPath());
        Core.ACTION_TIME.set(System.currentTimeMillis());
        Core.ACTION_LANG.set(conf.getProperty("core.language.default", "en-us"));

        if (conf.getProperty("core.language.detect", false)) {
            /**
             * 语言可以记录到Session/Cookie里
             */
            String lang;
            String sess = conf.getProperty("core.language.session", "lang");
            lang = (String) helper.getSession(sess);
            if (lang == null || lang.length() == 0) {
                lang = helper.getCookie(sess);
            if (lang == null || lang.length() == 0) {
                lang = req.getHeader("Accept-Language");
            }
            }

            /**
             * 检查是否是支持的语言
             */
            if (lang != null) {
                lang = CoreLanguage.getAcceptLanguage(lang);
            if (lang != null) {
                Core.ACTION_LANG.set(lang);
            }
            }
        }

        if (0 < Core.DEBUG) {
            CoreLogger.debug("...\r\n"
                + "THREAD_ID       : " + Thread.currentThread().getId() + "\r\n"
                + "ACTION_TIME     : " + Core.ACTION_TIME.get() + "\r\n"
                + "ACTION_PATH     : " + Core.ACTION_PATH.get() + "\r\n"
                + "ACTION_LANG     : " + Core.ACTION_LANG.get() + "\r\n"
                + "Address         : " + req.getRemoteAddr() + " "
                                       + req.getRemotePort() + "\r\n");
        }
    }

    private void doDestroy() {
        if (0 < Core.DEBUG) {
            Core core = Core.THREAD_CORE.get();
            long time = System.currentTimeMillis() - Core.ACTION_TIME.get();
            CoreLogger.debug("...\r\n"
                + "THREAD_ID       : " + Thread.currentThread().getId() + "\r\n"
                + "Runtime         : " + Text.humanTime(time) + "\r\n"
                + "Objects         : " + core.keySet().toString() + "\r\n");
        }

        try {
            Core.THREAD_CORE.get( ).destroy( );
        } catch ( Throwable  e) {
            CoreLogger.error(e);
        }
    }

}
