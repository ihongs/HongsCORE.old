package app.hongs.action;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.HongsThrowable;
import app.hongs.action.annotation.ActionChain;

/**
 * <h1>动作启动器</h2>
 * <pre>
 * 在 app.xxx.action 中建立一个类, 指定多个
 * public void actionXxx(app.hongs.action.ActionHelper helper)
 * 方法接收来自Web的请求动作.
 * </pre>
 *
 * <h2>web.xml配置:</h2>
 * <pre>
 * &lt;!-- Action Servlet --&gt;
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;Action&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;app.hongs.action.Action&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;Action&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;*.do&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * <pre>
 *
 * @author Hongs
 */
public class Action
  extends AbstractServlet
{

  /**
   * 服务方法
   *
   * Servlet Mapping: *.do
   * 注意: 不支持请求URI的路径中含有"."(句点), 且必须区分大小写;
   * 其目的是为了防止产生多种形式的请求路径, 影响动作过滤, 产生安全隐患.
   *
   * @param req
   * @param rsp
   * @throws java.io.IOException
   * @throws javax.servlet.ServletException
   */
  @Override
  public void _actService(HttpServletRequest req, HttpServletResponse rsp)
    throws IOException, ServletException
  {
    Core         core   = Core.getInstance();
    String       action = Core.ACTION.get ();
    ActionHelper helper = (ActionHelper)
                          Core.getInstance(app.hongs.action.ActionHelper.class);

    if (action.length() == 0) {
        helper.print404Code("Can not find action name.");
        return;
    }

    if (action.indexOf('.') != -1 || action.startsWith("hongs/action")) {
        helper.print404Code("Illegal action '"+Core.ACTION.get()+"'.");
        return;
    }

    /** 提取action路径里的"包.类.方法" **/

    int pos;
    String cls, mtd;
    action = action.substring(1).replace('/', '.');

    pos = action.lastIndexOf('.');
    if (pos == -1) {
        helper.print404Code("Wrong action '"+Core.ACTION.get()+"'.");
        return;
    }
    mtd    = action.substring(pos+1);
    action = action.substring(0,pos);

    pos = action.lastIndexOf('.');
    if (pos == -1) {
        helper.print404Code("Wrong action '"+Core.ACTION.get()+"'.");
        return;
    }
    cls    = action.substring(pos+1);
    action = action.substring(0,pos);

    // app.包.action.类, action方法
    doAction("app."+action+".action."+cls, "action"+mtd, helper);
  }

  /**
   * 执行动作
   *
   * @param cls
   * @param mtd
   * @param helper
   */
  protected void doAction(String cls, String mtd, ActionHelper helper)
    throws ServletException
  {
    /**
     * 获取相应动作类及方法对象, 并执行该方法;
     * 如果找不到或不可执行, 则输出404代码;
     * 如果执行失败, 则输出500代码.
     */

    // 获取类
    Class klass;
    try
    {
      klass = Class.forName(cls);
    }
    catch (ClassNotFoundException ex)
    {
      helper.print404Code("Can not find class '" + cls + "'.");
      return;
    }

    // 获取方法
    Method method;
    try
    {
      method = klass.getMethod(mtd, new Class[] {ActionHelper.class});
    }
    catch (NoSuchMethodException ex)
    {
      helper.print404Code("Can not find method '" + cls + "." + mtd + "'.");
      return;
    }
    catch (SecurityException ex)
    {
      helper.print500Code("Can not exec method '" + cls + "." + mtd + "'.");
      return;
    }

    // 获取对象
    Object object;
    try
    {
      /**
       * 采用核心对象来存储动作对象
       * 下次执行动作则无需重新构建
       * 更多信息请参考Core.get()方法
       */
      Core core = Core.getInstance();
      if (!core.containsKey(cls))
      {
        object = klass.newInstance();
        core.put(cls , object);
      }
      else
      {
        object = core.get(cls);
      }
    }
    catch (InstantiationException ex)
    {
      helper.print500Code("Cannot instantiate class '" + cls + "'.");
      return;
    }
    catch (IllegalAccessException ex)
    {
      helper.print500Code("Illegal access for class '" + cls + "'.");
      return;
    }

    // 执行方法
    ActionChain chain = new ActionChain(method, object, helper);
    try
    {
      chain.doAction();
    }
    catch (HongsException ex )
    {
      // 0x1100为方法内部错误
      if (ex.getCode() == 0x1100)
      {
        doThrown(ex.getCause(), helper);
      }
      else
      {
        doThrown(ex , helper );
      }
    }
  }

  private void doThrown(Throwable ta, ActionHelper helper)
    throws ServletException
  {
    /**
     * 构建错误消息
     */
    String error = ta.getMessage();
    if (!(ta instanceof HongsThrowable))
    {
      CoreLanguage lang = (CoreLanguage)
        Core.getInstance(app.hongs.CoreLanguage.class);
      if (error == null || error.length() == 0)
      {
        error = lang.translate("core.error.unkwn");
      }
        error = lang.translate("core.error.label",
                ta.getClass().getName())
                + ": " + error;
    }

    /**
     * 记录跟踪信息
     */
    if (Core.IN_DEBUG_MODE)
    {
      ta.printStackTrace(System.out);
    }
    else
    if (!(ta instanceof HongsThrowable))
    {
      ta.printStackTrace(System.err);
    }

//  throw new ServletException(error,ta);
    helper.print500Code ( error );
  }

}
