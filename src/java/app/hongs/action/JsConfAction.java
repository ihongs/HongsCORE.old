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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsError;
import app.hongs.util.Text;

/**
 * <h1>配置信息输出动作</h1>
 *
 * <h2>web.xml配置:</h2>
 * <pre>
 * &lt;!-- Config Servlet --&gt;
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;Config&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;app.hongs.action.JSConfAction&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;Config&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;*.jsc&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * <pre>
 *
 * @author Hongs
 */
public class JsConfAction
  extends AbstractServlet
{
  private static Map<String, String> config = new HashMap<String, String>();
  private static Map<String, String> lastModified = new HashMap<String, String>();

  /**
   * 服务方法
   * 判断配置和消息有没有生成, 如果没有则生成; 消息按客户语言存放
   * @param req
   * @param rsp
   * @throws java.io.IOException
   * @throws javax.servlet.ServletException
   */
  @Override
  public void _actService(HttpServletRequest req, HttpServletResponse rsp)
    throws IOException, ServletException
  {
    Core core = Core.getInstance();
    ActionHelper helper = (ActionHelper)Core.getInstance("app.hongs.action.ActionHelper");

    String name = core.ACTION.substring(1, core.ACTION.lastIndexOf('.'));
    String type = req.getParameter("t");
    String m, s;

    /**
     * 如果指定配置的数据并没有改变
     * 则直接返回 304 Not modified
     */
    m = helper.request.getHeader("If-Modified-Since");
    if (m != null && m.equals(JsConfAction.lastModified.get(name)) )
    {
      helper.response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }

    /**
     * 如果没有配置
     * 则调用工厂方法构造 JS 代码
     */
    if (!JsConfAction.config.containsKey(name))
    {
      try {
        s = this.makeConfig(name);
      }
      catch (HongsError ex) {
        helper.print500Code(ex.getMessage());
        return;
      }

      SimpleDateFormat
          sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",
                                      Locale.ENGLISH );
          sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      m = sdf.format(new Date());

      JsConfAction.config.put(name , s);
      JsConfAction.lastModified.put(name , m);
    }
    else
    {
      s = JsConfAction.config.get(name);
      m = JsConfAction.lastModified.get(name);
    }

    // 标明修改时间
    helper.response.setHeader("Last-Modified", m);

    // 输出配置信息
    if ("json".equals(type)) {
      helper.printJSON(s);
    }
    else {
      helper.printJS("if(typeof(HsCONF)==\"undefined\")HsCONF={};$.extend(HsCONF,"+s+");");
    }
  }

  /**
   * 销毁方法
   * 清空配置信息和消息数据
   */
  @Override
  public void destroy()
  {
    super.destroy();

    // 销毁配置信息
    JsConfAction.config.clear();
  }

  /**
   * 构造配置信息
   * 配置类型按后缀划分为:
   * .N 数字
   * .B 布尔
   * .C 代码
   * 无后缀及其他为字符串
   */
  private String makeConfig(String confName)
  {
    Maker         mk = new Maker(confName);
    StringBuilder sb = new StringBuilder();

    /** 配置代码 **/

    sb.append("{\r\n");

    // 公共配置
    if (confName.equals("default"))
    {
      sb.append("DEBUG:")
        .append(String.valueOf(Core.IN_DEBUG_MODE))
        .append(",\n")
        .append("SERVER_ID:\"")
        .append(Core.SERVER_ID)
        .append("\",\n")
        .append("BASE_HREF:\"")
        .append(Core.BASE_HREF)
        .append("\",\n");
    }

    // 查找扩展配置信息
    Iterator it = mk.conf.keySet().iterator();
    while (it.hasNext())
    {
      sb.append(mk.make((String) it.next( )));
    }

    sb.append("\"\":\"\"\r\n}");
    return sb.toString();
  }

  /** 辅助工具类 **/

  /**
   * 配置信息辅助构造类
   */
  private static class Maker
  {
    private CoreConfig conf;

    public Maker(String name)
    {
      this.conf = new CoreConfig(name+".js");
    }

    public String make(String key)
    {
      /**
       * 后缀 意义
       * [无] 字符串
       * .B   布尔
       * .N   数字
       * .C   代码
       * .L   链接
       */
      String name = key;
      name = name.replaceFirst("^(core|user)\\.js\\.", "");
      name = name.replaceFirst("\\.[B|N|C|L]$", "");
      if (key.endsWith(".L"))
      {
        return this.makeLink(name, key);
      }
      else if(key.endsWith(".C"))
      {
        return this.makeCode(name, key);
      }
      else if (key.endsWith(".B"))
      {
        return this.makeConf(name, key, false);
      }
      else if (key.endsWith(".N"))
      {
        return this.makeConf(name, key, 0 );
      }
      else
      {
        return this.makeConf(name, key, "");
      }
    }

    private String makeConf(String name, String key, String def)
    {
      String value = this.conf.getProperty(key, def);
      value = Text.escape(value);
      return "\"" + name + "\":\"" + value + "\",\r\n";
    }

    private String makeConf(String name, String key, double def)
    {
      String value = String.valueOf(this.conf.getProperty(key, def));
      return "\"" + name + "\":" + value + ",\r\n";
    }

    private String makeConf(String name, String key, boolean def)
    {
      String value = String.valueOf(this.conf.getProperty(key, def));
      return "\"" + name + "\":" + value + ",\r\n";
    }

    private String makeCode(String name, String key)
    {
      String value = this.conf.getProperty(key, "null");
      return "\"" + name + "\":" + value + ",\n";
    }

    private String makeLink(String name, String key)
    {
      String[] arr = this.conf.getProperty(key, "").split(":", 2);
      if (1 == arr.length)
      {
        name = "default";
        key  = arr[0];
      }
      else
      {
        name = arr[0];
        key  = arr[1];
      }
        this.conf.load(name);
      return this.make(key );
    }
  }

}
