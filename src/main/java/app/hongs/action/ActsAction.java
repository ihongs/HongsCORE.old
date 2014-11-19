package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
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
    ActionHelper helper = (ActionHelper)
          Core.getInstance(ActionHelper.class);
    String act = Core.ACTION_NAME.get();
    String acp;
    String mtd;
    int    pos;

    if (act == null || act.length() == 0) {
        helper.error404("Action uri can not be empty.");
        return;
    }

    try {
        // 去掉扩展名
        pos = act.lastIndexOf('.');
        acp = act.substring(0,pos);

        // 提取方法名
        pos = acp.lastIndexOf('/');
        mtd = acp.substring(pos+1);
        acp = acp.substring(0,pos);
    }
    catch (StringIndexOutOfBoundsException ex ) {
        helper.error404("Wrong action '" + act + "'.");
        return;
    }
    if (act.length() == 0 || mtd.length() == 0) {
        helper.error404("Wrong action '" + act + "'.");
        return;
    }

    doAction(acp, mtd, helper);
  }

  /**
   * 执行动作
   *
   * 获取相应动作类及方法对象, 并执行该方法;
   * 如果找不到或不可执行, 则输出404代码;
   * 如果执行失败, 则输出500代码.
   * 
   * @param act
   * @param mtd
   * @param helper
   * @throws javax.servlet.ServletException
   */
  private void doAction(String act, String mtd, ActionHelper helper)
    throws ServletException
  {
    // 获取动作
    ActionCaller caller;
    try
    {
      caller = ActionCaller.getInstance(act, mtd, helper);
    }
    catch (HongsException ex )
    {
      doThrown(400,ex, helper);
      return;
    }

    // 执行动作
    try
    {
      caller.doAction();
    }
    catch (HongsException ex )
    {
      // 0x1100为方法内部错误
      if (ex.getCode() != 0x1100)
      {
        doThrown(500, ex.getCause(), helper);
      }
      else
      {
        doThrown(500, ex, helper);
      }
    }
  }

  private void doThrown(int sym, Throwable err, ActionHelper helper)
    throws ServletException
  {
    /**
     * 构建错误消息
     */
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
        error = lang.translate("core.error.label",
                err.getClass().getName( ))
                + ": " + error;
    }

    switch (sym)
    {
      case 404:
        helper.error404(error);
        break;
      default :
        helper.error500(error);
        CoreLogger.error(err );
//      throw new ServletException(error, err);
    }
  }
}
