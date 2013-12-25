package app.hongs.action;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsError;
import app.hongs.util.Str;

/**
 * <h1>配置信息输出动作</h1>
 *
 * <h2>web.xml配置:</h2>
 * <pre>
 * &lt;!-- Config Servlet --&gt;
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;Auth&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;app.hongs.action.JSConfAction&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;Auth&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;*.jsa&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * <pre>
 *
 * @author Hongs
 */
public class JsAuthAction
  extends HttpServlet
{

  /**
   * 服务方法
   * 判断配置和消息有没有生成, 如果没有则生成; 消息按客户语言存放
   * @param req
   * @param rsp
   * @throws java.io.IOException
   * @throws javax.servlet.ServletException
   */
  @Override
  public void service(HttpServletRequest req, HttpServletResponse rsp)
    throws ServletException, IOException
  {
    Core core = Core.getInstance();
    ActionHelper helper = (ActionHelper)Core.getInstance(app.hongs.action.ActionHelper.class);

    String name = Core.ACTION_PATH.get();
           name = name.substring(1, name.lastIndexOf('.'));
    String type = req.getParameter( "t");
    String m, s;

    /**
     * CONF和SESS
     */
    String[] ns = name.split("/", 2);
    String conf, sess;
    if (ns.length > 1) {
      conf = ns[0];
      sess = ns[1];
    }
    else {
      conf = ns[0];
      sess = "actions";
      name+="/actions";
    }

    try {
      s = JSON.soString(ActionFilter.getActions(conf, sess);
    }
    catch (HongsError ex) {
      helper.print500Code(ex.getMessage());
      return;
    }

    // 输出配置信息
    if ("json".equals(type)) {
      helper.printJSON( s );
    }
    else {
      helper.printJS("if(!window.HsCONF)window.HsCONF={};$.extend(window.HsAUTH,"+s+");");
    }
  }

}
