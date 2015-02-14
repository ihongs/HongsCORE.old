package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.util.Data;
import app.hongs.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
 * 映射到 *api,*.act,*.jsp,/common/conf/*,/common/lang/*,/common/auth/*<br/>
 * 必须作为第一个 filter
 * </p>
 *
 * <h3>配置选项:</h3>
 * <pre>
 * core.server.id         服务ID
 * core.language.probing  探测语言
 * core.language.default  默认语言
 * core.timezone.probing  探测时区
 * core.timezone.default  默认时区
 * </pre>
 *
 * @author Hong
 */
public class ActionWarder
implements Filter {

    /**
     * Request Attribute: 当前核心对象(类型: Core)
     */
    public static final String CORE = "__HONGS_CORE__";

    /**
     * Request Attribute: 当前工作路径(类型: String)
     */
    public static final String PATH = "__HONGS_PATH__";

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
        Core.WEBS_PATH = context.getRealPath("" );
        Core.BASE_PATH = Core.WEBS_PATH + File.separator + "WEB-INF";

        // 调试开关
        str = context.getInitParameter ("debug");
        if (str != null) {
            Core.DEBUG = Byte.parseByte(  str  );
        }

        // 资源目录
        Core.CONF_PATH = Core.BASE_PATH + File.separator + "etc";
        Core.VARS_PATH = Core.BASE_PATH + File.separator + "var";

        CoreConfig conf = CoreConfig.getInstance("_begin_");

        // 资源配置
        Core.VARS_PATH = conf.getProperty("core.vars.path", Core.VARS_PATH);
        Core.LOGS_PATH = Core.VARS_PATH + File.separator + "log";
        Core.SERS_PATH = Core.VARS_PATH + File.separator + "ser";
        Core.LOGS_PATH = conf.getProperty("core.logs.path", Core.LOGS_PATH);
        Core.SERS_PATH = conf.getProperty("core.tmps.path", Core.SERS_PATH);
        Core.SERVER_ID = conf.getProperty("core.server.id", "" );

        // 调一下 ActionRunner 来加载动作
        ActionRunner.getActions();

        //** 系统属性配置 **/

        Map m = new HashMap();
        m.put("BASE_PATH", Core.BASE_PATH);
        m.put("CONF_PATH", Core.CONF_PATH);
        m.put("VARS_PATH", Core.VARS_PATH);

        // 启动系统属性
        for (Map.Entry et : conf.entrySet()) {
            String k = (String)et.getKey ( );
            String v = (String)et.getValue();
            if (k.startsWith("start.")) {
                k = k.substring(6  );
                v = Text.inject(v,m);
                System.setProperty(k,v);
            }
        }

        if (0 < Core.DEBUG && !(4 == (4 & Core.DEBUG))) {
        // 调试系统属性
        for (Map.Entry et : conf.entrySet()) {
            String k = (String)et.getKey ( );
            String v = (String)et.getValue();
            if (k.startsWith("debug.")) {
                k = k.substring(6  );
                v = Text.inject(v,m);
                System.setProperty(k,v);
            }
        }

            CoreLogger.debug(new StringBuilder("...")
                .append("\r\n\tSERVER_ID   : ").append(Core.SERVER_ID)
                .append("\r\n\tBASE_HREF   : ").append(Core.BASE_HREF)
                .append("\r\n\tWEBS_PATH   : ").append(Core.WEBS_PATH)
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
        if (0 < Core.DEBUG && !(4 == (4 & Core.DEBUG))) {
            Core core = Core.GLOBAL_CORE;
            long time = System.currentTimeMillis() - Core.STARTS_TIME;
            CoreLogger.debug(new StringBuilder("...")
                .append("\r\n\tSERVER_ID   : ").append(Core.SERVER_ID)
                .append("\r\n\tRuntime     : ").append(Text.humanTime(time))
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
    public void doFilter(ServletRequest rep, ServletResponse resp, FilterChain chain)
    throws ServletException, IOException {
        HttpServletRequest  req  = (HttpServletRequest ) rep ;
        HttpServletResponse resq = (HttpServletResponse) resp;

//        ActionHelper that;
//        Core core = (Core)req.getAttribute(CORE);
//        if ( core != null) {
//             Core.THREAD_CORE.set ( core);
//             that = core.get(ActionHelper.class);
//             that.reinitHelper(req, resq);
//                chain.doFilter(rep, resp);
//        } else {
        Core core = Core.getInstance();
        req.setAttribute( CORE, core );
        ActionHelper that = new ActionHelper(req, resq);
        core.put( ActionHelper.class.getName(  ), that);

        try {
            doIniter(req, core, that);
            chain.doFilter(rep, resp);

            // 输出数据
            if (!that.getResponse().isCommitted()) {
                Map rsp  = that.getResponseData();
                if (rsp != null) {
                    that.print();
                }
            }
        } finally {
            doFinish(req, core, that);
        }
//        }
    }

    protected void doIniter(HttpServletRequest req, Core core, ActionHelper helper)
    throws ServletException {
        Core.ACTION_NAME.set(getRealPath(req).substring(1));

        Core.ACTION_TIME.set(System.currentTimeMillis());

        CoreConfig conf = core.get(CoreConfig.class);

        Core.ACTION_ZONE.set(conf.getProperty("core.timezone.default","GMT-8"));
        if (conf.getProperty("core.language.probing", false)) {
            /**
             * 时区可以记录到Session/Cookies里
             */
            String sess = conf.getProperty("core.timezone.session", "zone");
            String zone = (String) helper.getSessvalue(sess);
            if (zone == null || zone.length() == 0) {
                // 从 Cookie 里提取时区
                Cookie[] cookies = req.getCookies();
                if (cookies != null) for (Cookie cookie : cookies) {
                    if (cookie.getName( ).equals(sess)) {
                        zone = cookie.getValue();
                        break;
                    }
                }
            }
        }

        Core.ACTION_LANG.set(conf.getProperty("core.language.default","zh-CN"));
        if (conf.getProperty("core.language.probing", false)) {
            /**
             * 语言可以记录到Session/Cookies里
             */
            String sess = conf.getProperty("core.language.session", "lang");
            String lang = (String) helper.getSessvalue(sess);
            if (lang == null || lang.length() == 0) {
                // 从 Cookie 里提取语言
                Cookie[] cookies = req.getCookies();
                if (cookies != null) for (Cookie cookie : cookies) {
                    if (cookie.getName( ).equals(sess)) {
                        lang = cookie.getValue();
                        break;
                    }
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
    }

    protected void doFinish(HttpServletRequest req, Core core, ActionHelper helper) {
        if (0 < Core.DEBUG && !(4 == (4 & Core.DEBUG))) {
            ActionHelper that = Core.getInstance(ActionHelper.class);
            HttpServletRequest R = that.getRequest();
            Map rd, xd;
            try {
                rd = that.getRequestData( );
                xd = that.getResponseData();
            } catch (HongsException e) {
                throw new HongsError(HongsError.COMMON, e);
            }
            long time = System.currentTimeMillis() - Core.ACTION_TIME.get();
            StringBuilder sb = new StringBuilder("...");
              sb.append("\r\n\tACTION_NAME : ").append(Core.ACTION_NAME.get())
                .append("\r\n\tACTION_TIME : ").append(Core.ACTION_TIME.get())
                .append("\r\n\tACTION_LANG : ").append(Core.ACTION_LANG.get())
                .append("\r\n\tACTION_ZONE : ").append(Core.ACTION_ZONE.get())
                .append("\r\n\tMethod      : ").append(R.getMethod())
                .append("\r\n\tClient      : ").append(R.getRemoteAddr()).append(" ").append(R.getRemotePort())
                .append("\r\n\tUser-Agent  : ").append(R.getHeader("User-Agent"))
                .append("\r\n\tRuntime     : ").append(Text.humanTime( time ))
                .append("\r\n\tObjects     : ").append(core.keySet().toString( ));
            if (rd != null && !rd.isEmpty()) {
              sb.append("\r\n\tRequest     : ").append(Text.indent(Data.toString(rd)).substring(1));
            }
            if (xd != null && !xd.isEmpty()) {
              sb.append("\r\n\tResults     : ").append(Text.indent(Data.toString(xd)).substring(1));
            }
            CoreLogger.debug(sb.toString( ));
        }

        try {
            core.destroy();
            req.removeAttribute(CORE);
            Core.THREAD_CORE.remove();
            Core.ACTION_TIME.remove();
            Core.ACTION_ZONE.remove();
            Core.ACTION_LANG.remove();
            Core.ACTION_NAME.remove();
        } catch ( Throwable  e) {
            CoreLogger.error(e);
        }
    }

    /**
     * 获得当前工作的Core
     * @param req
     * @return
     */
    public static  Core  getWorkCore(HttpServletRequest req) {
        Core core = (Core) req.getAttribute(CORE);
        if ( core == null) {
             core =  Core. GLOBAL_CORE ;
        } else {
             Core.THREAD_CORE.set(core);
        }
        return core;
    }

    /**
     * 获得当前工作的Path
     * @param req
     * @return
     */
    public static String getWorkPath(HttpServletRequest req) {
        String uri = (String) req.getAttribute(PATH);
        if (uri == null) {
            uri = getCurrPath(req);
        }
        return uri;
    }

    /**
     * 获得当前的ServletPath
     * @param req
     * @return
     */
    public static String getCurrPath(HttpServletRequest req) {
        String uri = (String) req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
        if (uri == null) {
            uri =  req.getServletPath();
        }
        return uri;
    }

    /**
     * 获得真实的ServletPath
     * @param req
     * @return
     */
    public static String getRealPath(HttpServletRequest req) {
        String uri = (String) req.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH);
        if (uri == null) {
            uri =  req.getServletPath();
        }
        return uri;
    }

}
