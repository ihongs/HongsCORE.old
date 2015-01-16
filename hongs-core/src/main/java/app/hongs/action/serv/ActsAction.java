package app.hongs.action.serv;

import app.hongs.action.ActionWarder;
import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.CoreLogger;
import app.hongs.HongsCause;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 动作启动器
 *
 * <h3>处理器编程:</h3>
 * <p>
 * 添加一个包, 将包名加到 default.properties 里的 core.action.pacakges 值中;
 * 添加一个类, 给类加上注解 @Action(action/path), 不添加或提供一个无参构造方法;
 * 添加一个方法, 给方法加上 @Action(action_name), 提供一个 ActionHelper 参数;
 * </p>
 *
 * <h3>web.xml配置:</h3>
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;ActsAction&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;app.hongs.action.ActsAction&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;ActsAction&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;***.act&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 *
 * @author Hongs
 */
public class ActsAction
  extends HttpServlet
{

  /**
   * 服务方法
   * Servlet Mapping: *.act<br/>
   * 注意: 不支持请求URI的路径中含有"."(句点), 且必须区分大小写;
   * 其目的是为了防止产生多种形式的请求路径, 影响动作过滤, 产生安全隐患.
   *
   * @param req
   * @param rsp
   * @throws javax.servlet.ServletException
   */
  @Override
  public void service(HttpServletRequest req, HttpServletResponse rsp)
    throws ServletException
  {
    String act  = ActionWarder.getCurrPath(req);
    Core   core = ActionWarder.getCurrCore(req);
    ActionHelper helper = core.get(ActionHelper.class);
    helper.reinitHelper(req, rsp);

    if (act == null || act.length() == 0)
    {
      senderr(req, helper, "Er404", "Action URI can not be empty.");
      return;
    }

    // 去扩展名
    act = act.substring(1);
    int pos;
        pos = act.lastIndexOf('.');
    if (pos > -1)
        act = act.substring(0,pos);

    ActionRunner runner;

    // 获取动作
    try
    {
      runner = new ActionRunner(act, helper);
    }
    catch (HongsException ex)
    {
      senderr(req, helper, "Er404", ex.getLocalizedMessage());
      return;
    }

    // 执行动作
    try
    {
      runner.doAction( );
    }
    catch (Throwable ex)
    {
      senderr(req, helper, ex);
    }
  }

  private static final Map<Integer, String> ErrMap;
  static {
      ErrMap = new HashMap();
      ErrMap.put(0x10f3, "Er403");
      ErrMap.put(0x10f4, "Er404");
      ErrMap.put(0x10f5, "Er500");
  }
  
  private void senderr(HttpServletRequest req, ActionHelper helper, Throwable ex)
    throws ServletException
  {
    CoreLogger.error(ex);

    String errno;
    String error = ex.getLocalizedMessage();
    if (ex instanceof  HongsCause)
    {
      HongsCause hc = (HongsCause) ex ;
      errno = ErrMap.get(hc.getCode());
      if (errno == null)
      {
        errno = "Ex" + Integer.toHexString(hc.getCode());
      }
    } else
    {
      errno = "Er500";
      CoreLanguage lang = (CoreLanguage)
          Core.getInstance(CoreLanguage.class );
      if (error == null || error.length() == 0)
      {
        error = lang.translate("core.error.unkwn");
      }
        error = lang.translate("core.error.label" ,
        ex.getClass( ).getName()) + ": " + error  ;
    }

    senderr(req, helper, errno, error);
  }

  private void senderr(HttpServletRequest req, ActionHelper helper, String errno, String error)
    throws ServletException
  {
    if ("Er404".equals(errno))
    {
      helper.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else
    {
      helper.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    Map data = new HashMap();
    data.put( "ok" , false );
    data.put( "err", errno );
    data.put( "msg", error );
    helper.reply(data);
  }

}
