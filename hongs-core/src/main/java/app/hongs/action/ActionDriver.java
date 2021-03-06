package app.hongs.action;

import app.hongs.Cnst;
import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLocale;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.util.Data;
import app.hongs.util.Synt;
import app.hongs.util.Tool;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
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
     * 首位标识, 为 true 表示第一个执行，负责系统初始化
     */
    private boolean INIT = false;

    /**
     * 初始化 Filter
     * @param conf
     * @throws ServletException
     */
    @Override
    public void init( FilterConfig conf) throws ServletException {
        this.init(conf.getServletContext());
    }

    /**
     * 初始化 Servlet
     * @param conf
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig conf) throws ServletException {
       super.init(conf /*call super init*/);
        this.init(conf.getServletContext());
    }

    /**
     * 公共初始化
     * @param cont
     * @throws ServletException
     */
    synchronized private void init(ServletContext cont) throws ServletException {
        if (Core.ENVIR != 1) {
            Core.ENVIR  = 1;
        } else {
            return;
        }
        INIT= true;

        if (Core.BASE_HREF == null) {
            System.setProperty("file.encoding", "UTF-8");

            /** 核心属性配置 **/

            Core.DEBUG = Synt.declare(cont.getInitParameter("debug"), (byte) 0);

            Core.BASE_HREF = cont.getContextPath();
            Core.BASE_PATH = cont.getRealPath("" );

            Core.BASE_PATH = Core.BASE_PATH.replaceFirst("[/\\\\]$", "");
            Core.CORE_PATH = Core.BASE_PATH + File.separator + "WEB-INF";

            File cp = new File(Core.CORE_PATH );
            if (!cp.exists()) {
                Core.CORE_PATH = cp.getParent();
            }

            Core.CONF_PATH = Core.CORE_PATH + File.separator + "etc";
            Core.DATA_PATH = Core.CORE_PATH + File.separator + "var";

            //** 系统属性配置 **/

            CoreConfig cnf = CoreConfig.getInstance("_begin_");
            Core.SERVER_ID = cnf.getProperty("core.server.id" , "1" );

            Map m = new HashMap();
            m.put("BASE_PATH", Core.BASE_PATH);
            m.put("CORE_PATH", Core.CORE_PATH);
            m.put("CONF_PATH", Core.CONF_PATH);
            m.put("DATA_PATH", Core.DATA_PATH);

            // 启动系统属性
            for (Map.Entry et : cnf.entrySet( )) {
                String k = (String)et.getKey(  );
                String v = (String)et.getValue();
                if (k.startsWith("begin.")) {
                    k = k.substring(6  );
                    v = Tool.inject(v,m);
                    System.setProperty(k,v);
                }
            }

            if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG)) {
            // 调试系统属性
            for (Map.Entry et : cnf.entrySet()) {
                String k = (String)et.getKey ( );
                String v = (String)et.getValue();
                if (k.startsWith("debug.")) {
                    k = k.substring(6  );
                    v = Tool.inject(v,m);
                    System.setProperty(k,v);
                }
            }
            }
        }

        if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG)) {
            CoreLogger.debug(new StringBuilder("...")
                .append("\r\n\tDEBUG       : ").append(Core.DEBUG)
                .append("\r\n\tSERVER_ID   : ").append(Core.SERVER_ID)
                .append("\r\n\tBASE_HREF   : ").append(Core.BASE_HREF)
                .append("\r\n\tBASE_PATH   : ").append(Core.BASE_PATH)
                .append("\r\n\tCORE_PATH   : ").append(Core.CORE_PATH)
                .append("\r\n\tCONF_PATH   : ").append(Core.CONF_PATH)
                .append("\r\n\tDATA_PATH   : ").append(Core.DATA_PATH)
                .toString());
        }

        // 调一下 ActionRunner 来加载动作
        ActionRunner.getActions();
    }

    /**
     * 公共销毁
     */
    @Override
    public void destroy() {
        if (! INIT) {
            return;
        }

        if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG)) {
            Core core = Core.GLOBAL_CORE;
            long time = System.currentTimeMillis() - Core.STARTS_TIME;
            CoreLogger.debug(new StringBuilder("...")
                .append("\r\n\tSERVER_ID   : ").append(Core.SERVER_ID)
                .append("\r\n\tRuntime     : ").append(Tool.humanTime(time))
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
        Core core = (Core) req.getAttribute(Cnst.CORE_ATTR);
        if (core != null) {
            Core.THREAD_CORE.set (  core);
            hlpr = core.get(ActionHelper.class);
            hlpr.reinitHelper( req , rsq);
            /**/doAction(core, hlpr /**/);
        } else {
            core = Core.getInstance(/**/);
            req.setAttribute(Cnst.CORE_ATTR , core);
            hlpr = new ActionHelper( req , rsq);
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
        Core core = (Core) req.getAttribute(Cnst.CORE_ATTR);
        if (core != null) {
            Core.THREAD_CORE.set (  core);
            hlpr = core.get(ActionHelper.class);
            hlpr.reinitHelper( req , rsq);
            /**/doFilter(core, hlpr, chn);
        } else {
            core = Core.getInstance(/**/);
            req.setAttribute(Cnst.CORE_ATTR , core);
            hlpr = new ActionHelper( req , rsq);
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

    private void doOutput(ActionHelper hlpr, HttpServletRequest req, HttpServletResponse rsp)
            throws ServletException {
//        if (hlpr.getResponse().isCommitted()) {
//            ServletException se = new ServletException("Response is committed!");
//            CoreLogger.error(se);
//            throw se;
//        }

        Map dat  = hlpr.getResponseData();
        if (dat != null) {
            req .setAttribute(Cnst.RESP_ATTR, dat);
            hlpr.reinitHelper( req, rsp );
            hlpr.responed();
        }
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
            String zone = (String)hlpr.getSessibute(sess);
            if (zone == null || zone.length() == 0) {
                   zone = /*str*/ hlpr.getCookibute(sess);
            }

            if (zone != null) {
                Core.ACTION_ZONE.set(zone);
            }
        }

        Core.ACTION_LANG.set(conf.getProperty("core.language.default","zh_CN"));
        if (conf.getProperty("core.language.probing", false)) {
            /**
             * 语言可以记录到Session/Cookies里
             */
            String sess = conf.getProperty("core.language.session", "lang");
            String lang = (String)hlpr.getSessibute(sess);
            if (lang == null || lang.length() == 0) {
                   lang = /*str*/ hlpr.getCookibute(sess);
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
        if (0 < Core.DEBUG && 4 != (4 & Core.DEBUG)) {
            ActionHelper that = Core.getInstance(ActionHelper.class);
            HttpServletRequest R = that.getRequest();
            long time = System.currentTimeMillis(  ) - Core.ACTION_TIME.get();
            StringBuilder sb = new StringBuilder("...");
              sb.append("\r\n\tACTION_NAME : ").append(Core.ACTION_NAME.get())
                .append("\r\n\tACTION_TIME : ").append(Core.ACTION_TIME.get())
                .append("\r\n\tACTION_LANG : ").append(Core.ACTION_LANG.get())
                .append("\r\n\tACTION_ZONE : ").append(Core.ACTION_ZONE.get())
                .append("\r\n\tMethod      : ").append(R.getMethod())
                .append("\r\n\tRemote      : ").append(R.getRemoteAddr())
                                   .append(" ").append(R.getRemotePort())
                .append("\r\n\tQuest-With  : ").append(R.getHeader("X-Requested-With"))
                .append("\r\n\tUser-Agent  : ").append(R.getHeader("User-Agent"))
                .append("\r\n\tObjects     : ").append(core.keySet( ).toString())
                .append("\r\n\tRuntime     : ").append(Tool.humanTime(time));

            // 输入输出数据, 这对调试程序非常有帮助
            Map rd, xd; CoreConfig cf = CoreConfig.getInstance();
            try {
                rd = that.getRequestData( );
                xd = that.getResponseData();
            } catch (HongsException ex) {
                throw new HongsError.Common(ex);
            }
            if (xd == null) {
                xd = (Map) req.getAttribute(Cnst.RESP_ATTR);
            }
            if (cf.getProperty("core.trace.action.request", false)
            &&  rd != null && !rd.isEmpty()) {
              sb.append("\r\n\tRequest     : ")
                .append(Tool.indent(Data.toString(rd)).substring(1));
            }
            if (cf.getProperty("core.trace.action.results", false)
            &&  xd != null && !xd.isEmpty()) {
              sb.append("\r\n\tResults     : ")
                .append(Tool.indent(Data.toString(xd)).substring(1));
            }

            if (cf.getProperty("core.trace.action.context", false)) {
              Map         map = new HashMap();
              Enumeration<String> nms = req.getAttributeNames();
              while (nms.hasMoreElements()) {
                  String  nme = nms.nextElement();
                  map.put(nme , req.getAttribute(nme));
              }
              if (!map.isEmpty()) {
                sb.append("\r\n\tcontext     : ")
                  .append(Tool.indent(Data.toString(map)).substring(1));
              }
            }

            if (cf.getProperty("core.trace.action.session", false)) {
              Map         map = new HashMap();
              HttpSession ses = hlpr.getRequest().getSession( );
              Enumeration<String> nms = ses.getAttributeNames();
              while (nms.hasMoreElements()) {
                  String  nme = nms.nextElement();
                  map.put(nme , ses.getAttribute(nme));
              }
              if (!map.isEmpty()) {
                sb.append("\r\n\tSession     : ")
                  .append(Tool.indent(Data.toString(map)).substring(1));
              }
            }

            if (cf.getProperty("core.trace.action.cookies", false)) {
              Map         map = new HashMap();
              Cookie[]    cks = hlpr.getRequest().getCookies( );
              for (Cookie cke : cks) {
                  map.put(cke.getName() , cke.getValue());
              }
              if (!map.isEmpty()) {
                sb.append("\r\n\tCookies     : ")
                  .append(Tool.indent(Data.toString(map)).substring(1));
              }
            }

            CoreLogger.trace(sb.toString());
        }

        // 销毁此周期内的对象
        core.destroy();
        req.removeAttribute(Cnst.CORE_ATTR);
        Core.THREAD_CORE.remove();
        Core.ACTION_TIME.remove();
        Core.ACTION_ZONE.remove();
        Core.ACTION_LANG.remove();
        Core.ACTION_NAME.remove();
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

    //** 静态工具函数 **/

    /**
     * 获得当前工作的Core
     * @param req
     * @return
     */
    public static Core getWorkCore(HttpServletRequest req) {
        Core core = (Core) req.getAttribute(Cnst.CORE_ATTR);
        if (core ==  null) {
            core  =  Core.GLOBAL_CORE ;
        } else {
            Core.THREAD_CORE.set(core);
        }
        return core;
    }

    /**
     * 获得当前工作的Path
     * 当使用某些通用过滤器时会设置虚拟请求路径,
     * 后方动作需按照此给出的路径来执行特定任务.
     * @param req
     * @return
     */
    public static String getWorkPath(HttpServletRequest req) {
        String uri = (String) req.getAttribute(Cnst.PATH_ATTR);
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
