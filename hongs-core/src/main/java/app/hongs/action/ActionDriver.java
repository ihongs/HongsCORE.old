package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLocale;
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
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 动作驱动器
 *
 * <p>
 * 其他 Servlet,Filter 继承此类即可安全的使用 Core 请求对象;
 * 或者将其作为 Filter 加入到 web.xml, 则其后执行的 Filter,Servlet 均可安全的使用 Core 请求对象.
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
public class ActionDriver extends HttpServlet implements Servlet, Filter {

    /**
     * 初始化标识, 为 true 表示该对象负责的初始化
     */
    private boolean INIT = false;

    /**
     * 初始化 Filter
     * @param conf
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig conf) throws ServletException {
        this.init(conf.getServletContext());
    }

    /**
     * 初始化 Servlet
     * @param conf
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig conf) throws ServletException {
       super.init(conf);
        this.init(conf.getServletContext());
    }

    /**
     * 公共初始化
     * @param cont
     * @throws ServletException
     */
    synchronized private void init(ServletContext cont) throws ServletException {
        /**
         * 如果核心类中基础路径已设置, 则表示已经被实例过了
         * 即无需再重复获取配置信息了
         */
        if (Core.ENVIR == 1 && Core.BASE_HREF != null) {
            return;
        }
        INIT= true;

        System.setProperty( "file.encoding", "UTF-8" );

        /** 静态属性配置 **/

        String str;

        Core.ENVIR = 1;
        Core.DEBUG = 0;
        Core.BASE_HREF = cont.getContextPath();
        Core.CONT_PATH = cont.getRealPath ("");
        Core.BASE_PATH = Core.CONT_PATH + File.separator + "WEB-INF";

        // 调试开关
        str = cont.getInitParameter("debug");
        if (str != null) {
            Core.DEBUG = Byte.parseByte(str);
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
            if (k.startsWith("begin.")) {
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
                .append("\r\n\tWEBS_PATH   : ").append(Core.CONT_PATH)
                .append("\r\n\tBASE_PATH   : ").append(Core.BASE_PATH)
                .append("\r\n\tCONF_PATH   : ").append(Core.CONF_PATH)
                .append("\r\n\tVARS_PATH   : ").append(Core.VARS_PATH)
                .append("\r\n\tLOGS_PATH   : ").append(Core.LOGS_PATH)
                .append("\r\n\tSERS_PATH   : ").append(Core.SERS_PATH)
                .toString());
        }
    }

    /**
     * 公共销毁
     */
    @Override
    public void destroy() {
        if (! INIT) {
            return;
        }

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
    public void service (ServletRequest rep, ServletResponse rsp)
    throws ServletException, IOException {
        HttpServletRequest  req = (HttpServletRequest ) rep;
        HttpServletResponse rsq = (HttpServletResponse) rsp;

        ActionHelper hlpr;
        Core core = (Core) req.getAttribute( CORE );
        if (core != null) {
            Core.THREAD_CORE.set (  core);
            hlpr = core.get(ActionHelper.class);
            hlpr.reinitHelper( req , rsq);
            /**/doAction(core, hlpr /**/);
        } else {
            core = Core.getInstance(/**/);
            req.setAttribute(CORE , core);
            hlpr = new ActionHelper(req , rsq);
            core.put ( ActionHelper.class.getName(), hlpr);

            try {
                doIniter(core, hlpr, req);
                doAction(core, hlpr /**/);
                doOutput(hlpr, req , rsq);
            } finally {
                doFinish(core, hlpr, req);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest rep, ServletResponse rsp, FilterChain chn)
    throws ServletException, IOException {
        HttpServletRequest  req = (HttpServletRequest ) rep;
        HttpServletResponse rsq = (HttpServletResponse) rsp;

        ActionHelper hlpr;
        Core core = (Core) req.getAttribute( CORE );
        if (core != null) {
            Core.THREAD_CORE.set (  core);
            hlpr = core.get(ActionHelper.class);
            hlpr.reinitHelper( req , rsq);
            /**/doFilter(core, hlpr, chn);
        } else {
            core = Core.getInstance(/**/);
            req.setAttribute(CORE , core);
            hlpr = new ActionHelper(req , rsq);
            core.put ( ActionHelper.class.getName(), hlpr);

            try {
                doIniter(core, hlpr, req);
                doFilter(core, hlpr, chn);
                doOutput(hlpr, req , rsq);
            } finally {
                doFinish(core, hlpr, req);
            }
        }
    }

    private void doOutput(ActionHelper hlpr, HttpServletRequest req, HttpServletResponse rsp) {
//      if (!hlpr.getResponse().isCommitted()) {
            Map dat  = hlpr.getResponseData();
            if (dat != null) {
                if (0< Core.DEBUG && !(4 == (4 & Core.DEBUG))) {
                    req.setAttribute("__HONGS_DATA__", dat );
                }

//              HttpServletRequest  raq = hlpr.getRequest( );
//              HttpServletResponse rzp = hlpr.getResponse();
//              hlpr.reinitHelper ( req , rsp );
                hlpr.print();
//              hlpr.reinitHelper ( raq , rzp );
            }
//      }
    }

    private void doIniter(Core core, ActionHelper hlpr, HttpServletRequest req)
    throws ServletException {
        Core.ACTION_NAME.set(getRealPath(req).substring(1));

        Core.ACTION_TIME.set(System.currentTimeMillis());

        CoreConfig conf = core.get(CoreConfig.class);

        Core.ACTION_ZONE.set(conf.getProperty("core.timezone.default","GMT-8"));
        if (conf.getProperty("core.timezone.probing", false)) {
            /**
             * 时区可以记录到Session/Cookies里
             */
            String sess = conf.getProperty("core.timezone.session", "zone");
            String zone = (String) hlpr.getSessvalue(sess);
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
            String lang = (String) hlpr.getSessvalue(sess);
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
                lang = req.getHeader( "Accept-Language" );
            }
            }

            /**
             * 检查是否是支持的语言
             */
            if (lang != null) {
                lang = CoreLocale.getAcceptLanguage(lang);
            if (lang != null) {
                Core.ACTION_LANG.set(lang);
            }
            }
        }
    }

    private void doFinish(Core core, ActionHelper hlpr, HttpServletRequest req) {
        if (0 < Core.DEBUG && !(4 == (4 & Core.DEBUG))) {
            ActionHelper that = Core.getInstance(ActionHelper.class);
            HttpServletRequest R = that.getRequest();
            long time = System.currentTimeMillis() - Core.ACTION_TIME.get();
            StringBuilder sb = new StringBuilder("...");
              sb.append("\r\n\tACTION_NAME : ").append(Core.ACTION_NAME.get())
                .append("\r\n\tACTION_TIME : ").append(Core.ACTION_TIME.get())
                .append("\r\n\tACTION_LANG : ").append(Core.ACTION_LANG.get())
                .append("\r\n\tACTION_ZONE : ").append(Core.ACTION_ZONE.get())
                .append("\r\n\tMethod      : ").append(R.getMethod())
                .append("\r\n\tClient      : ").append(R.getRemoteAddr()).append(" ").append(R.getRemotePort())
                .append("\r\n\tUser-Agent  : ").append(R.getHeader("User-Agent"))
                .append("\r\n\tRuntime     : ").append(Text.humanTime(time))
                .append("\r\n\tObjects     : ").append(core.keySet( ).toString());

            // 输入输出数据, 这对调试程序非常有帮助
            Map rd, xd;
            try {
                rd = that.getRequestData( );
                xd = that.getResponseData();
            } catch (HongsException ex) {
                throw HongsError.common(null, ex);
            }
            if (xd == null) {
                xd = (Map ) req.getAttribute("__HONGS_DATA__");
            }
            if (rd != null && !rd.isEmpty()) {
              sb.append("\r\n\tRequest     : ").append(Text.indent(Data.toString(rd)).substring(1));
            }
            if (xd != null && !xd.isEmpty()) {
              sb.append("\r\n\tResults     : ").append(Text.indent(Data.toString(xd)).substring(1));
            }

            CoreLogger.debug( sb.toString());
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
     * 执行过滤
     * 如需其他操作请覆盖此方法
     * @param core
     * @param hlpr
     * @param chn
     * @throws ServletException
     * @throws IOException
     */
    protected void doFilter(Core core, ActionHelper hlpr, FilterChain chn)
    throws ServletException, IOException {
        chn.doFilter((ServletRequest ) hlpr.getRequest( ),
                     (ServletResponse) hlpr.getResponse());
    }

    /**
     * 执行动作
     * 如需其他操作请覆盖此方法
     * @param core
     * @param hlpr
     * @throws ServletException
     * @throws IOException
     */
    protected void doAction(Core core, ActionHelper hlpr )
    throws ServletException, IOException {
        service( hlpr.getRequest( ), hlpr.getResponse( ) );
    }

    //** 静态工具函数 **/

    /**
     * Request Attribute: 当前核心对象(类型: Core)
     */
    public static final String CORE = "__HONGS_CORE__";

    /**
     * Request Attribute: 当前工作路径(类型: String)
     */
    public static final String PATH = "__HONGS_PATH__";

    /**
     * 获得当前工作的Core
     * @param req
     * @return
     */
    public static Core getWorkCore(HttpServletRequest req) {
        Core core = (Core) req.getAttribute(ActionDriver.CORE);
        if (core == null) {
            core = Core.GLOBAL_CORE;
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
        String uri = (String) req.getAttribute(ActionDriver.PATH);
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
            uri = req.getServletPath();
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
            uri = req.getServletPath();
        }
        return uri;
    }

}
