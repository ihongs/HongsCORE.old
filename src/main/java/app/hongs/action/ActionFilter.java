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
 * <h3>配置选项:</h3>
 * <pre>
 * core.server.id         服务ID
 * core.language.probing  自动识别语言
 * core.language.default  默认语言类型
 * </pre>
 *
 * @author Hong
 */
public class ActionFilter
implements Filter {

    @Override
    public void init(FilterConfig config)
    throws ServletException {
        ServletContext context = config.getServletContext();

        System.setProperty("file.encoding", "UTF-8");

        /** 静态属性配置 **/

        String str;

        Core.ENVIR = 1;
        Core.DEBUG = 0;
        Core.BASE_HREF = context.getContextPath();
        Core.BASE_PATH = context.getRealPath("" ) + File.separator + "WEB-INF";

        // 调试开关
        str = context.getInitParameter ("debug");
        if (str != null) {
            Core.DEBUG = Byte.parseByte(  str  );
        }

        // 资源目录
        Core.CONF_PATH = Core.BASE_PATH + File.separator + "etc";
        Core.VARS_PATH = Core.BASE_PATH + File.separator + "var";

        // 资源配置
        CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
        Core.VARS_PATH = conf.getProperty("core.vars.path", Core.VARS_PATH);
        Core.LOGS_PATH = Core.VARS_PATH + File.separator + "log";
        Core.SERS_PATH = Core.VARS_PATH + File.separator + "ser";
        Core.LOGS_PATH = conf.getProperty("core.logs.path", Core.LOGS_PATH);
        Core.SERS_PATH = conf.getProperty("core.tmps.path", Core.SERS_PATH);
        Core.SERVER_ID = conf.getProperty("core.server.id", "" );

            // 启动系统属性
            for (Map.Entry et : conf.entrySet()) {
                String k = (String)et.getKey  ();
                String v = (String)et.getValue();
                if (k.startsWith("start.")) {
                    System.setProperty(k.substring(6), v);
                }
            }

        if (0 < Core.DEBUG) {
            // 调试系统属性
            for (Map.Entry et : conf.entrySet()) {
                String k = (String)et.getKey  ();
                String v = (String)et.getValue();
                if (k.startsWith("debug.")) {
                    System.setProperty(k.substring(6), v);
                }
            }

            CoreLogger.debug("...\r\n"
                + "SERVER_ID       : " + Core.SERVER_ID + "\r\n"
                + "BASE_HREF       : " + Core.BASE_HREF + "\r\n"
                + "BASE_PATH       : " + Core.BASE_PATH + "\r\n"
                + "CONF_PATH       : " + Core.CONF_PATH + "\r\n"
                + "VARS_PATH       : " + Core.VARS_PATH + "\r\n"
                + "LOGS_PATH       : " + Core.LOGS_PATH + "\r\n"
                + "SERS_PATH       : " + Core.SERS_PATH + "\r\n");
        }
    }

    @Override
    public void destroy() {
        if (1 == (1 & Core.DEBUG)) {
            Core core = Core.GLOBAL_CORE;
            long time = System.currentTimeMillis() - Core.STARTS_TIME;
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

        Core.ACTION_TIME.set(System.currentTimeMillis());
        Core.ACTION_LANG.set(conf.getProperty("core.language.default", "zh-cn"));
        Core.ACTION_PATH.set(req.getRequestURI().substring(req.getContextPath().length()));

        if (conf.getProperty("core.language.probing", false)) {
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
                + "ACTION_LANG     : " + Core.ACTION_LANG.get() + "\r\n"
                + "ACTION_PATH     : " + Core.ACTION_PATH.get() + "\r\n"
                + "Method          : " + req.getMethod() + "\r\n"
                + "Client          : " + req.getRemoteAddr() + " "
                                       + req.getRemotePort() + "\r\n"
                + "User-Agent      : " + req.getHeader("User-Agent") + "\r\n");
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
