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
import app.hongs.CoreLanguage;
import app.hongs.HongsError;
import app.hongs.util.Text;

/**
 * <h1>语言信息输出动作</h1>
 *
 * <h2>web.xml配置:</h2>
 * <pre>
 * &lt;!-- Config Servlet --&gt;
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;Language&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;app.hongs.action.JSLangAction&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;Language&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;*.jsl&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;<br/>
 * <pre>
 *
 * @author Hongs
 */
public class JsLangAction
  extends AbstractServlet
{
  private static Map<String, String> language = new HashMap<String, String>();
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

    String conf = core.ACTION.substring(1, core.ACTION.length() - 4);
    String type = helper.getParameter("t");
    String lang = helper.getParameter("l");
    if (lang == null || lang.length() == 0)
           lang = core.LANGUAGE;
    String name = conf+"."+lang;
    String m, s;

    /**
     * 如果指定语言的数据并没有改变
     * 则直接返回 304 Not modified
     */
    m = helper.request.getHeader("If-Modified-Since");
    if (m != null  &&  m.equals(JsLangAction.lastModified.get(name)))
    {
      helper.response.setStatus(HttpServletResponse.SC_NOT_MODIFIED );
      return;
    }

    /**
     * 如果没有语言
     * 则调用工厂方法构造 JS 代码
     */
    if (!JsLangAction.language.containsKey(name))
    {
      try {
        s = this.makeLanguage(conf, lang);
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

      JsLangAction.language.put(name , s);
      JsLangAction.lastModified.put(name , m);
    }
    else
    {
      s = JsLangAction.language.get(name);
      m = JsLangAction.lastModified.get(name);
    }

    // 标明修改时间
    helper.response.setHeader("Last-Modified", m);

    // 输出语言信息
    if ("json".equals(type)) {
      helper.printJSON(s);
    }
    else {
      helper.printJS("if(typeof(HsLANG)==\"undefined\")HsLANG={};$.extend(HsLANG,"+s+");");
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
    JsLangAction.language.clear();
  }

  /**
   * 按语言构造消息信息
   * 配置类型按后缀划分为:
   * .C 代码
   * 无后缀及其他为字符串
   * @param lang
   */
  private String makeLanguage(String confName, String langName)
  {
    Maker mk = new Maker(confName, langName);
    StringBuilder sb = new StringBuilder(  );
    sb.append("{\r\n");

    /** 配置代码 **/

    // 公共语言
    if ("default".equals(confName))
    {
      CoreLanguage conf = (CoreLanguage)Core.getInstance("app.hongs.CoreLanguage");
      sb.append("\"error.err\":\"" + conf.getProperty("core.error.label", "ERROR") + "\",\r\n");
      sb.append("\"error.ukw\":\"" + conf.getProperty("core.error.unkwn", "UNKWN") + "\",\r\n");
      sb.append("\"time.format\":\"" + conf.getProperty("core.default.time.format", "HH:mm:ss") + "\",\r\n");
      sb.append("\"date.format\":\"" + conf.getProperty("core.default.date.format", "yyyy/MM/dd") + "\",\r\n");
      sb.append("\"datetime.format\":\"" + conf.getProperty("core.default.datetime.format", "yyyy/MM/dd HH:mm:ss") + "\",\r\n");
    }

    // 查找扩展语言信息
    Iterator it = mk.lang.keySet().iterator();
    while (it.hasNext())
    {
      sb.append(mk.make((String) it.next( )));
    }

    sb.append("\"\":\"\"\r\n}");
    return sb.toString();
  }

  /** 辅助工具类 **/

  /**
   * 语言信息辅助构造类
   */
  private static class Maker
  {
    private CoreLanguage lang;

    public Maker(String name, String lang)
    {
      this.lang = new CoreLanguage(name+".js", lang);
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
      String name = key.substring(8);
      name = name.replaceFirst("\\.[B|N|C|L]$", "");
      if (key.endsWith(".L"))
      {
        return this.makeLink(name, key);
      }
      else if (key.endsWith(".C"))
      {
        return this.makeCode(name, key);
      }
      else if (key.endsWith(".B"))
      {
        return this.makeLang(name, key, false);
      }
      else if (key.endsWith(".N"))
      {
        return this.makeLang(name, key, 0 );
      }
      else
      {
        return this.makeLang(name, key, "");
      }
    }

    private String makeLang(String name, String key, String def)
    {
      String value = this.lang.getProperty(key, def);
      value = Text.escape(value);
      return "\"" + name + "\":\"" + value + "\",\r\n";
    }

    private String makeLang(String name, String key, double def)
    {
      String value = String.valueOf(this.lang.getProperty(key, def));
      return "\"" + name + "\":" + value + ",\r\n";
    }

    private String makeLang(String name, String key, boolean def)
    {
      String value = String.valueOf(this.lang.getProperty(key, def));
      return "\"" + name + "\":" + value + ",\r\n";
    }

    private String makeCode(String name, String key)
    {
      String value = this.lang.getProperty(key, "null");
      return "\"" + name + "\":" + value + ",\r\n";
    }

    private String makeLink(String name, String key)
    {
      String[] arr = this.lang.getProperty(key, "").split(":", 2);
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
        this.lang.load(name);
      return this.make(key );
    }
  }
}
