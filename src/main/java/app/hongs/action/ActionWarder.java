package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;
import app.hongs.CoreLogger;
import app.hongs.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 动作承载器
 *
 * <p>
 映射到 *.csp *.api *.jsp /common/auth/* /common/conf/* /common/lang/*<br/>
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
public class ActionWarder
implements Filter {

    /**
     * Request Attribute: 已经启动
     */
    public static final String STARTED = "app.hongs.action.started";

    /**
     * Request Attribute: 已经输出
     */
    public static final String PRINTED = "app.hongs.action.printed";

    /**
     * Request Attribute: 应答数据
     */
    public static final String REPLIED = "app.hongs.action.replted";

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

        // 调一下 ActionRunner 来加载 ACTIONS
        ActionRunner.ACTIONS.size();

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

            CoreLogger.debug(new StringBuilder("...")
                .append("\r\n\tSERVER_ID   : ").append(Core.SERVER_ID)
                .append("\r\n\tBASE_HREF   : ").append(Core.BASE_HREF)
                .append("\r\n\tBASE_PATH   : ").append(Core.BASE_PATH)
                .append("\r\n\tCONF_PATH   : ").append(Core.CONF_PATH)
                .append("\r\n\tVARS_PATH   : ").append(Core.VARS_PATH)
                .append("\r\n\tLOGS_PATH   : ").append(Core.LOGS_PATH)
                .append("\r\n\tSERS_PATH   : ").append(Core.SERS_PATH)
                .toString());
        }
    }

    @Override
    public void destroy() {
        if (1 == (1 & Core.DEBUG)) {
            Core core = Core.GLOBAL_CORE;
            long time = System.currentTimeMillis() - Core.STARTS_TIME;
            CoreLogger.debug(new StringBuilder("...")
                .append("\r\n\tSERVER_ID   : ").append(Core.SERVER_ID)
                .append("\r\n\tRuntime     : ").append(Util.humanTime(  time  ))
                .append("\r\n\tObjects     : ").append(core.keySet().toString())
                .toString());
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
        HttpServletRequest  req = (HttpServletRequest ) request ;
        HttpServletResponse rsp = (HttpServletResponse) response;

        if (!Core.getInstance().containsKey(ActionHelper.class.getName())
        ||  req.getAttribute(STARTED) == null) {
            req.setAttribute(STARTED  ,  true);
            doIniter(req, rsp, chain);
        } else {
            doReinit(req, rsp, chain);
        }
    }

    private void doReinit(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain)
    throws ServletException, IOException {
        ActionHelper helper = (ActionHelper) Core.getInstance(ActionHelper.class);
        HttpServletResponse rzp = helper.getResponse();
        try {
            helper.setInstance(req, rsp);
            chain.doFilter((ServletRequest)req, (ServletResponse)rsp);
        } finally {
            helper.setInstance(req, rzp);
        }
    }

    private void doIniter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain)
    throws ServletException, IOException {
        ActionHelper helper = new ActionHelper( req , rsp );
        Core.getInstance().put(ActionHelper.class.getName(), helper );
        try {
            this .doIniter( req, helper);
            chain.doFilter((ServletRequest)req, (ServletResponse)rsp);
        } finally {
            this .doFinish();
        }
    }

    private void doIniter(HttpServletRequest req, ActionHelper helper)
    throws ServletException {
        Core.ACTION_TIME.set(System.currentTimeMillis());

        Core.ACTION_NAME.set(getRealityActionPath(req ));

        CoreConfig  conf  = (CoreConfig) Core.getInstance(  CoreConfig.class  );
        Core.ACTION_LANG.set(conf.getProperty("core.language.default","zh-cn"));
        if (conf.getProperty("core.language.probing", false)) {
            /**
             * 语言可以记录到Session/Cookies里
             */
            String lang;
            String sess = conf.getProperty("core.language.session", "lang");
//          lang = (String) req.getSession().getAttribute(sess);
            lang = (String) helper.getAttribute(sess);
            if (lang == null || lang.length() == 0) {
                // 从 Cookie 里提取语言
                Cookie[] cookies = req.getCookies();
                if  (cookies != null)
                for (Cookie cookie : cookies)
                if  (cookie.getName().equals(sess)) {
                    lang = cookie.getValue( );
                    break;
                }
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
            CoreLogger.debug(new StringBuilder().append("...")
                .append("\r\n\tTHREAD_ID   : ").append(Thread.currentThread().getId())
                .append("\r\n\tACTION_TIME : ").append(Core.ACTION_TIME.get())
                .append("\r\n\tACTION_LANG : ").append(Core.ACTION_LANG.get())
                .append("\r\n\tACTION_PATH : ").append(Core.ACTION_NAME.get())
                .append("\r\n\tMethod      : ").append(req.getMethod())
                .append("\r\n\tClient      : ").append(req.getRemoteAddr())
                                  .append("\t").append(req.getRemotePort())
                .append("\r\n\tUser-Agent  : ").append(req.getHeader( "User-Agent" ) )
                .toString());
        }
    }

    private void doFinish() {
        if (0 < Core.DEBUG) {
            Core core = Core.THREAD_CORE.get();
            long time = System.currentTimeMillis() - Core.ACTION_TIME.get();
            CoreLogger.debug(new StringBuilder("...")
                .append("\r\n\tTHREAD_ID   : ").append(Thread.currentThread().getId())
                .append("\r\n\tRuntime     : ").append(Util.humanTime(  time  ))
                .append("\r\n\tObjects     : ").append(core.keySet().toString())
                .toString());
        }

        try {
            Core.THREAD_CORE.get( ).destroy( );
        } catch ( Throwable  e) {
            CoreLogger.error(e);
        }
    }

    /**
     * 获得当前的ServletPath
     * @param req
     * @return
     */
    public static String getCurrentActionPath(HttpServletRequest req) {
        String uri = (String) req.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI );
        String ctx = (String) req.getAttribute(RequestDispatcher.INCLUDE_CONTEXT_PATH);
        if (uri == null) {
            uri =  req.getRequestURI( );
            ctx =  req.getContextPath();
        }
        return uri.substring(ctx.length() + 1);
    }

    /**
     * 获得真实的ServletPath
     * @param req
     * @return
     */
    public static String getRealityActionPath(HttpServletRequest req) {
        String uri = (String) req.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI );
        String ctx = (String) req.getAttribute(RequestDispatcher.FORWARD_CONTEXT_PATH);
        if (uri == null) {
            uri =  req.getRequestURI( );
            ctx =  req.getContextPath();
        }
        return uri.substring(ctx.length() + 1);
    }

}
