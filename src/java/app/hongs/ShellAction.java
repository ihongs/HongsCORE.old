package app.hongs;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.hongs.action.*;

/**
 * <h1>外壳程序动作</h1>
 * <pre>
 * 在 app.shell 中建立一个类, 指定一个
 * public void action(app.hongs.action.ActionHelper helper)
 * 方法接收来自 Web 的请求动作.
 * </pre>
 *
 * <h2>web.xml配置:</h2>
 * <pre>
 * &lt;!-- Shell Servlet --&gt;
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;Shell&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;app.hongs.action.ShellAction&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;Shell&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;*.sh&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;<br/>
 * <pre>
 *
 * @author Hongs
 */
public class ShellAction
  extends Action
{

  /**
   * 服务方法
   *
   * Servlet Mapping: *.sh
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
      this.doAction(action + ".action", "app.shell");
    }
  }

}
