package app.hongs.action;

import java.util.Date;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import app.hongs.*;

/**
 * <h1>抽象Servlet类</h1>
 *
 * <h2>配置选项:</h2>
 * <pre>
 * core.language.detect   自动识别语言
 * core.language.default  默认语言类型
 * core.language.session  语言会话参数
 * </pre>
 *
 * @author Hongs
 */
public abstract class AbstractServlet
  extends HttpServlet
{

  @Override
  public void init(ServletConfig config)
    throws ServletException
  {
    super.init(config);

    // 根据ServerContext进行初始化
    this.init(config.getServletContext());
  }

  public void init(ServletContext context)
    throws ServletException
  {
    /**
     * 初始化判断, 无需重复初始化
     */
    synchronized (Core.GLOBAL_CORE)
    {
      if (Core.GLOBAL_CORE != null)
          return;
      Core.GLOBAL_CORE = new Core();
      Core.GLOBAL_TIME = System.currentTimeMillis();
    }

    System.setProperty("file.encoding", "UTF-8");

    /** 静态属性配置 **/

    String str;

    // 调试开关
    str = context.getInitParameter("debug");
    if (str!=null&&Integer.parseInt(str)>0)
    {
      Core.IN_DEBUG_MODE = true;
    }

    // 当前应用根目录
    Core.BASE_HREF = context.getContextPath( );
    Core.BASE_PATH = context.getRealPath( "" ) + File.separator + "WEB-INF";

    // 其他信息文件存放目录
    Core.CONF_PATH = Core.BASE_PATH + File.separator + "conf";
    Core.LANG_PATH = Core.BASE_PATH + File.separator + "lang";
    Core.LOGS_PATH = Core.BASE_PATH + File.separator + "logs";
    Core.TMPS_PATH = Core.BASE_PATH + File.separator + "tmps";

    // 默认开启字符串模式
    CoreConfig conf = (CoreConfig)Core.getInstance(app.hongs.CoreConfig.class);
    Core.LOGS_PATH  = conf.getProperty("core.logs.dir", Core.LOGS_PATH);
    Core.TMPS_PATH  = conf.getProperty("core.tmps.dir", Core.TMPS_PATH);
    Core.SERVER_ID  = conf.getProperty("core.server.id", "0");

    if (Core.IN_DEBUG_MODE)
    {
      System.out.println(
        "--------------------------------------------------\r\n"
        + "SERVER_ID       : " + Core.SERVER_ID + "\r\n"
        + "BASE_HREF       : " + Core.BASE_HREF + "\r\n"
        + "BASE_PATH       : " + Core.BASE_PATH + "\r\n"
        + "CONF_PATH       : " + Core.CONF_PATH + "\r\n"
        + "LANG_PATH       : " + Core.LANG_PATH + "\r\n"
        + "LOGS_PATH       : " + Core.LOGS_PATH + "\r\n"
        + "TMPS_PATH       : " + Core.TMPS_PATH + "\r\n"
      );
    }
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse rsp)
    throws ServletException, IOException
  {
    try
    {
      this._actPrepare(req, rsp);
      this._actService(req, rsp);
    }
    finally
    {
      this._actDestroy();
    }
  }

  protected void _actPrepare(HttpServletRequest req, HttpServletResponse rsp)
    throws ServletException
  {
    /**
     * 获取当前动作路径/客户标识/会话编号
     * 并依次比对其与之前的是否一致
     * 如一致即不用再初始化了
     */

    String action = req.getRequestURI()
           .substring(Core.BASE_HREF.length( ));
    String aprsid = req.getRemoteAddr()
            + "|" + req.getRemotePort()
            + "|" + req.getRequestedSessionId();

    Core core = Core.getInstance();
    ActionHelper helper = (ActionHelper)
         core.get(app.hongs.action.ActionHelper.class);

    if (action.equals(Core.ACTION.get())
    &&  aprsid.equals(helper.APRSID)
    &&  0     <       helper.INITID)
    {
      helper.INITID ++;
      return; // 已初始则退出
    }
    else
    {
      helper.INITID ++;
      helper.APRSID = aprsid;
    }

    /**
     * 设置新的核心时间/动作路径
     * 设置新的客户标识/会话编号
     * 同时初始化助手对象
     */

    Core.ACTION_TIME.set(System.currentTimeMillis());
    Core.ACTION.set(action);
    helper.init( req, rsp );

    /** 实例属性配置 **/

    /**
     * 如果开启语言识别, 则根据客户端的Accept-Language来识别支持的语言;
     * 未开启识别或没有对应的语言配置, 则采用默认语言.
     */

    CoreConfig conf = (CoreConfig)
      Core.getInstance(app.hongs.CoreConfig.class);

    if (conf.getProperty("core.language.detect", false))
    {
      /**
       * 语言可以记录到Session/Cookie里
       */
      String lang;
      String sess = conf.getProperty("core.language.session", "lang");
      lang = (String) helper.getSession(sess);
      if (lang == null || lang.length() == 0)
      {
        Cookie c = helper.getCookie(sess);
        lang = c == null ?  null : c.getValue();
        if (lang == null || lang.length() == 0)
        {
          lang = req.getHeader("Accept-Language");
        }
      }

      /**
       * 检查是否是支持的语言
       */
      if (lang != null)
      {
        lang = CoreLanguage.getAcceptLanguage(lang);
      }
      if (lang != null)
      {
        Core.ACTION_LANG.set(lang);
      }
      else
      {
        Core.ACTION_LANG.set(conf.getProperty("core.language.default"));
      }
    }
    else
    {
        Core.ACTION_LANG.set(conf.getProperty("core.language.default"));
    }

    if (Core.IN_DEBUG_MODE)
    {
      System.out.println(
        "--------------------------------------------------\r\n"
        + "THREAD_ID       : " + Thread.currentThread().getId() + "\r\n"
        + "ACTION          : " + Core.ACTION + "\r\n"
        + "LANG            : " + Core.ACTION_LANG.get() + "\r\n"
        + "TIME            : " + Core.ACTION_TIME.get() + "\r\n"
        + "User Address    : " + req.getRemoteAddr() + " "
                               + req.getRemotePort() + "\r\n"
      );
    }
  }

  /**
   * 执行服务
   * 注意: 需要建立自己的service请覆盖该方法, 而不是service方法
   * @param req
   * @param rsp
   * @throws ServletException
   * @throws IOException
   */
  public abstract void _actService(HttpServletRequest req, HttpServletResponse rsp)
    throws ServletException, IOException;

  protected void _actDestroy()
  {
    /**
     * 判断当前退出的层级
     * 如果到了最底层
     * 则销毁核心
     */

    ActionHelper helper = (ActionHelper)
      Core.getInstance(app.hongs.action.ActionHelper.class);

    if (helper.INITID > 0)
    {
      helper.INITID --;
    }
    else
    {
      return;
    }

    Core core = Core.getInstance();

    if (Core.IN_DEBUG_MODE)
    {
      long  time = Core.ACTION_TIME.get();
      float secs = (float)(System.currentTimeMillis() - time) / 1000;
      System.out.println(
        "--------------------------------------------------\r\n"
        + "THREAD_ID       : " + Thread.currentThread().getId() + "\r\n"
        + "Used Seconds    : " + secs    + "\r\n"
        + "Used Objects    : " + core.keySet().toString() + "\r\n"
      );
    }

    core.destroy();
  }

  @Override
  public void destroy()
  {
    super.destroy();

    Core core;
    long time;
    synchronized (Core.GLOBAL_CORE)
    {
      if (Core.GLOBAL_CORE == null)
          return;
      core = Core.GLOBAL_CORE;
      time = Core.GLOBAL_TIME;
      Core.GLOBAL_CORE = null;
      Core.GLOBAL_TIME = null;
    }

    if (Core.IN_DEBUG_MODE)
    {
      float secs = (float)(System.currentTimeMillis() - time) / 1000;
      System.out.println(
        "--------------------------------------------------\r\n"
        + "SERVER_ID       : " + Core.SERVER_ID + "\r\n"
        + "Used Seconds    : " + secs           + "\r\n"
        + "Used Objects    : " + core.keySet().toString() + "\r\n"
      );
    }
 
    core.destroy();
  }

}
