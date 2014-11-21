package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
import static app.hongs.action.ActionWarder.PRINTED;
import static app.hongs.action.ActionWarder.REPLIED;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 动作启动器
 *
 * <p>
 * 在 app.xxx.action 中建立一个类, 指定多个
 * <code>
 * public void actionXxx(app.hongs.action.ActionHelper helper)
 * </core>
 方法接收来自 Web 的请求, 可使用 helper.reply() 返回数据
 </p>
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
 * <pre>
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
   * @throws java.io.IOException
   * @throws javax.servlet.ServletException
   */
  @Override
  public void service(HttpServletRequest req, HttpServletResponse rsp)
    throws IOException, ServletException
  {
    ActionHelper helper = (ActionHelper) Core.getInstance(ActionHelper.class);
    String act = ActionWarder.getCurrentActionPath(req );

    if (act == null || act.length() == 0) {
        helper.error404("Action path can not be empty.");
        return;
    }

    // 去扩展名
    try {
        int pos;
        pos = act.lastIndexOf('.');
        act = act.substring(0,pos);
    }
    catch (StringIndexOutOfBoundsException ex) {
        helper.error404("Action path '"+act +"' error.");
        return;
    }

    ActionRunner caller;

    // 获取动作
    try
    {
      caller = new ActionRunner(act, helper);
    }
    catch (HongsException ex )
    {
      senderr(404, ex, helper);
      return;
    }

    // 执行动作
    try
    {
      caller.doAction();sendout(req, helper);
    }
    catch (HongsException ex )
    {
      senderr(500, ex, helper);
      return;
    }
  }

  private void senderr(int sym, Throwable err, ActionHelper helper)
    throws ServletException
  {
    String error = err.getLocalizedMessage();
    if (! (err instanceof HongsException)
    &&  ! (err instanceof HongsError  ) )
    {
      CoreLanguage lang = (CoreLanguage)
          Core.getInstance(CoreLanguage.class );
      if (error == null || error.length() == 0)
      {
        error = lang.translate("core.error.unkwn");
      }
        error = lang.translate("core.error.label" ,
        err.getClass().getName()) + ": " + error  ;
    }

    switch (sym)
    {
      case 404:
        helper.error404 (error);
        break;
      default :
        helper.error500 (error);
        CoreLogger.error( err );
//      throw new ServletException(error, err);
    }
  }

  private void sendout(HttpServletRequest req, ActionHelper helper) {
      if (req.getAttribute(PRINTED) == null ) {
          req.setAttribute(PRINTED  ,  true );
          Map data = helper.getResponseData();
          if (data!= null)helper.print(data );
      } else
      if (req.getAttribute(REPLIED) == null ) {
          Map data = helper.getResponseData();
          req.setAttribute(REPLIED  ,  data );
      }
  }

}
