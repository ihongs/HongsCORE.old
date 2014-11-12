package app.hongs.cmdlet;

import app.hongs.Core;
import app.hongs.action.Action;
import app.hongs.action.ActionHelper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 外壳程序动作
 *
 * <p>
 * 在 app.xxx.shell 中建立一个类, 指定一个
 * <code>
 * public void action(app.hongs.action.ActionHelper helper)
 * </code>
 * 方法接收来自 Web 的请求, 可使用 helper.back() 返回数据.
 * </p>
 *
 * <h3>web.xml配置:</h3>
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;Cmdlet&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;app.hongs.cmdlet.CmdletAction&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;Cmdlet&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;*.api&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
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
   * Servlet Mapping: *.api<br/>
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

        // 提取类名
        pos = act.lastIndexOf('.');
        cls = act.substring(pos+1);
        act = act.substring(0,pos);
    }
    catch (StringIndexOutOfBoundsException ex ) {
        helper.print404("Wrong action '"+Core.ACTION_PATH.get()+"'.");
        return;
    }
    if (act.length() == 0 || cls.length() == 0) {
        helper.print404("Wrong action '"+Core.ACTION_PATH.get()+"'.");
        return;
    }

    doAction("app."+act+".action."+cls, "action", helper);
  }

}
