package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;

import java.io.IOException;
import java.lang.reflect.Method;

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
 * 方法接收来自 Web 的请求, 可使用 helper.back() 返回数据.
 * </p>
 *
 * <h3>web.xml配置:</h3>
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;Action&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;app.hongs.action.Action&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;Action&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;***.act&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * <pre>
 *
 * @author Hongs
 */
public class Action
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
    String act = Core.ACTION_PATH.get( );
    String cls;
    String mtd;
    int    pos;
    
    if (act == null || act.length() ==0) {
        helper.print404("Action name can not be empty.");
        return;
    }

    try {
        // 去掉扩展名
        pos = act.lastIndexOf('.');
        act = act.substring(0,pos);

        // 检查URL中是否存在".", 会造成路径歧义, 不利于控制权限
        if (act.indexOf('.') !=-1) {
            helper.print404("Illegal action '"+Core.ACTION_PATH.get()+"'.");
            return;
        }

        // 提取方法名
        pos = act.lastIndexOf('.');
        mtd = act.substring(pos+1);
        act = act.substring(0,pos);

        // 提取类名
        pos = act.lastIndexOf('.');
        cls = act.substring(pos+1);
        act = act.substring(0,pos);
    }
    catch (StringIndexOutOfBoundsException ex ) {
        helper.print404("Wrong action '"+Core.ACTION_PATH.get()+"'.");
        return;
    }
    if (act.length() == 0 || cls.length() == 0 || mtd.length() == 0 ) {
        helper.print404("Wrong action '"+Core.ACTION_PATH.get()+"'.");
        return;
    }

    doAction("app."+act+".action."+cls, "action"+mtd, helper);
  }

  /**
   * 执行动作
   *
   * @param cls
   * @param mtd
   * @param helper
   * @throws javax.servlet.ServletException
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
      helper.print404("Can not find class '" + cls + "'.");
      return;
    }
    // 动作类必须加上 Action 注解. Add by Hongs, 2014/7/14
    if (! klass.isAnnotationPresent(app.hongs.action.annotation.Action.class))
    {
      helper.print404("Can not exec class '" + cls + "'.");
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
      helper.print404("Can not find method '" + cls + "." + mtd + "'.");
      return;
    }
    catch (SecurityException ex)
    {
      helper.print500("Can not exec method '" + cls + "." + mtd + "'.");
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
      helper.print500("Cannot instantiate class '" + cls + "'.");
      return;
    }
    catch (IllegalAccessException ex)
    {
      helper.print500("Illegal access for class '" + cls + "'.");
      return;
    }

    // 执行方法
    ActionChains chain = new ActionChains(method, object, helper);
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
    String error = ta.getLocalizedMessage();
    if (! (ta instanceof HongsException)
    &&  ! (ta instanceof HongsError  ) )
    {
      CoreLanguage lang = (CoreLanguage)
          Core.getInstance(CoreLanguage.class );
      if (error == null || error.length() == 0)
      {
        error = lang.translate("core.error.unkwn");
      }
        error = lang.translate("core.error.label",
                ta.getClass().getName())
                + ": " + error;
    }

//  throw new ServletException(error, ta);
    CoreLogger.error( ta );
    helper.print500(error);
  }

}
