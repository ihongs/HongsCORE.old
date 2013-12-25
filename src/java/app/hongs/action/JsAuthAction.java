package app.hongs.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.hongs.Core;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.util.JSON;

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
    ActionHelper helper = (ActionHelper)Core.getInstance(ActionHelper.class);

    String name = Core.ACTION_PATH.get();
           name = name.substring(1, name.lastIndexOf('.'));
    String type = req.getParameter( "t");
    String data;

    try {
      data = JSON.toString(ActionConfig.getInstance(name).getAuthMap());
    }
    catch (HongsException ex) {
      helper.print500Code(ex.getMessage());
      return;
    }
    catch (HongsError ex) {
      helper.print500Code(ex.getMessage());
      return;
    }

    // 输出配置信息
    if ( "json".equals(type)) {
      helper.printJSON(data);
    }
    else {
      helper.printJS("if(!window.HsAUTH)window.HsAUTH={};$.extend(window.HsAUTH,"+data+");");
    }
  }

}
