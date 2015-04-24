package app.hongs.action.serv;

import app.hongs.Core;
import app.hongs.CoreLocale;
import app.hongs.CoreLogger;
import app.hongs.HongsCause;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.ActionDriver;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
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
  extends  ActionDriver
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
    String act  = ActionDriver.getCurrPath(req);
    Core   core = ActionDriver.getWorkCore(req);
    ActionHelper helper = core.get(ActionHelper.class);
    Core.THREAD_CORE.set( core );

    if (act == null || act.length() == 0)
    {
      senderr(req, helper, 0x10f4, "Action URI can not be empty.");
      return;
    }

    // 去掉根和扩展名
    act = act.substring(1);
    int pos = act.lastIndexOf('.');
    if (pos != -1)
        act = act.substring(0,pos);

    // 获取并执行动作
    try
    {
      ActionRunner runner = new ActionRunner(act, helper);
      runner.doAction();
    }
    catch (HongsException  ex)
    {
      senderr(req, helper, ex);
    }
  }

  private void senderr(HttpServletRequest req, ActionHelper helper, Throwable ex)
    throws ServletException
  {
    CoreLogger.error(ex);

    int    errno;
    String error;
    if (ex instanceof  HongsCause)
    {
      HongsCause hc = (HongsCause) ex;
      String[] ls = hc.getLocalizedOptions();
      if (ls == null || ls.length == 0 )
      {
        hc.setLocalizedOptions(ActionDriver.getRealPath(req));
      }
        errno = hc.getCode();
        error = hc.getLocalizedMessage();
    } else
    {
        errno = 0x10fa;
        error = ex.getLocalizedMessage();
      CoreLocale lang = Core.getInstance(CoreLocale.class);
      if (error == null || error.length() == 0)
      {
        error = lang.translate("core.error.unkwn", ex.getClass().getName());
      } else
      {
        error = lang.translate("core.error.label", ex.getClass().getName()) + ": " + error;
      }
    }

    senderr(req, helper, errno, error);
  }

  private void senderr(HttpServletRequest req, ActionHelper helper, int errno, String error)
    throws ServletException
  {
    String errso;
    switch (errno)
    {
      case 0x10f1:
        errso = "Er401";
        helper.getResponse().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        break;
      case 0x10f3:
        errso = "Er403";
        helper.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
        break;
      case 0x10f4:
        errso = "Er404";
        helper.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
        break;
      case 0x10f5:
        errso = "Er405";
        helper.getResponse().setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        break;
      case 0x10fa:
        errso = "Er500";
        helper.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        break;
      default:
        errso = "Ex" + Integer.toHexString(errno);
        helper.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    Map data = new HashMap();
    data.put( "ok" , false );
    data.put( "err", errso );
    data.put( "msg", error );
    helper.reply(data);
  }

}
