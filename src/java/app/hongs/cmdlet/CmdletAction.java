package app.hongs.cmdlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.hongs.Core;
import app.hongs.action.Action;
import app.hongs.action.ActionHelper;

/**
 * <h1>外壳程序动作</h1>
 * <pre>
 * 在 app.xxx.shell 中建立一个类, 指定一个
 * public void action(app.hongs.action.ActionHelper helper)
 * 方法接收来自 Web 的请求动作.
 * </pre>
 *
 * <h2>web.xml配置:</h2>
 * <pre>
 * &lt;!-- Shell Servlet --&gt;
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;Cmdlet&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;app.hongs.cmdlet.CmdletAction&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;Cmdlet&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;*.if&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;<br/>
 * <pre>
 *
 * @author Hongs
 */
public class CmdletAction
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
  public void service(HttpServletRequest req, HttpServletResponse rsp)
    throws IOException, ServletException
  {
    ActionHelper helper = (ActionHelper)
      Core.getInstance(ActionHelper.class);
    String action = Core.ACTION_PATH.get();
    action = action.substring(1, action.lastIndexOf('.')); // 去掉前导"/", 去掉扩展名

    if (action != null && action.length() == 0) {
        helper.print404Code("Can not find action name.");
        return;
    }

    if (action.indexOf('.') != -1 || action.startsWith("hongs/cmdlet")) {
        helper.print404Code("Illegal action '"+Core.ACTION_PATH.get()+"'.");
        return;
    }

    /** 提取action路径里的"包.类" **/

    int pos;
    String pkg, cls;
    action = action.replace('/', '.');

    pos = action.lastIndexOf('.');
    if (pos == -1) {
        helper.print404Code("Wrong action '"+Core.ACTION_PATH.get()+"'.");
        return;
    }
    pkg = action.substring(0,pos);
    cls = action.substring(pos+1);

    // app.包.cmdlet.类, action方法
    doAction("app."+pkg+".cmdlet."+cls, "action", helper);
  }

}
