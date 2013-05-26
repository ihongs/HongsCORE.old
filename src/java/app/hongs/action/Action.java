package app.hongs.action;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsThrowable;
import app.hongs.action.annotation.CommitInvoker;
import app.hongs.action.annotation.CommitSuccess;

/**
 * <h1>动作启动器</h2>
 * <pre>
 * 在app.action中建立一个类,
 * 指定多个"public void actionXxx(app.hongs.action.ActionHelper helper)"方法来接收
 * 来自Web的请求动作.
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
    Core   core   = Core.getInstance();
    String action = core.ACTION.substring( 1 ,
                    core.ACTION.length() - 3);
    /*
    int pos = action.lastIndexOf( '.' );
    if (pos > 0)
    {
      action = action.substring(0, pos);
    }
        pos = action.lastIndexOf( '.' );
    if (pos > 0)
    */
    if (action.indexOf('.') != -1)
    {
      action = action.replace('/', '.');
      this.doAction(action, null, null);
    }
    else
    {
      action = action.replace('/', '.');
      this.doAction(action, "app.action", "action");
    }
  }

  /**
   * 执行动作
   *
   * 注意: 如果classPrefix或methodPrefix为null则返回解析动作名失败
   *
   * @param action
   * @param classPrefix
   * @param methodPrefix
   */
  protected void doAction(String action, String classPrefix, String methodPrefix)
    throws ServletException
  {
    ActionHelper helper = (ActionHelper)Core.getInstance("app.hongs.action.ActionHelper");

    /**
     * 从orderName中分离出对应的动作类及函数名称;
     * 如果找到不到或格式错误, 则输出404代码.
     */

    if (action.length() == 0)
    {
      helper.print404Code("Can not find action name.");
      return;
    }

    if (action.startsWith(".") || action.endsWith(".")
    ||  methodPrefix == null   || classPrefix == null)
    {
      helper.print404Code("Can not parse action name for '" + action + "'.");
      return;
    }

    int pos = action.lastIndexOf(".");
    if (pos == -1)
    {
      helper.print404Code("Can not find class or method for '" + action + "'.");
      return;
    }

    /**
     * 为防止动作名的多种形式影响到权限过滤
     * 故不再对类和方法的首字母自动转为大写
     */
    String cls = action.substring(0, pos);
    if (classPrefix.length() != 0)
    {
      //cls = classPrefix + "." + cls.substring(0, 1).toUpperCase() + cls.substring(1);
      cls = classPrefix + "." + cls;
    }
    String mtd = action.substring(pos + 1);
    if (methodPrefix.length() != 0)
    {
      //mtd = methodPrefix + mtd.substring(0, 1).toUpperCase() + mtd.substring(1);
      mtd = methodPrefix + mtd;
    }

    /*
    pos = 0;
    action = null;
    classPrefix = null;
    methodPrefix = null;
    */

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
      helper.print500Code("Can not instantiate class '" + cls + ".");
      return;
    }
    catch (IllegalAccessException ex)
    {
      helper.print500Code("Illegal access  for class '" + cls + ".");
      return;
    }
    catch (Exception ex)
    {
      catchError(ex, helper);
      return;
    }
    catch (Error ex)
    {
      catchError(ex, helper);
      return;
    }

    // 执行方法
    try
    {
      /**
       * 如果动作函数有绑定 CommitSuccess 注解
       * 则开启事务模式, 仅当操作成功时提交更改
       */
      if (method.isAnnotationPresent(CommitSuccess.class )) {
          CommitInvoker.invoke(method, object, new Object[] {helper});
          return;
      }

      method.invoke(object, new Object[] {helper});
    }
    catch (IllegalAccessException ex)
    {
      helper.print500Code("Illegal access for method '"+cls+"."+mtd+"'.");
    }
    catch (IllegalArgumentException ex)
    {
      helper.print500Code("Illegal argument for method '"+cls+"."+mtd+"'.");
    }
    catch (InvocationTargetException ex)
    {
      Throwable ta = ex.getCause();
      catchError(ta, helper);
    }
    catch (Exception ex)
    {
      catchError(ex, helper);
    }
    catch (Error ex)
    {
      catchError(ex, helper);
    }
  }

  protected void doAction(String action, String classPrefix)
    throws ServletException
  {
    this.doAction(action, classPrefix, "");
  }

  protected void doAction(String action)
    throws ServletException
  {
    this.doAction(action, "", "");
  }

  private void catchError(Throwable ta, ActionHelper helper)
    throws ServletException
  {
    /**
     * 构建错误消息
     */
    String error = ta.getMessage();
    if (!(ta instanceof HongsThrowable))
    {
      CoreLanguage lang = (CoreLanguage)
        Core.getInstance("app.hongs.CoreLanguage");
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
