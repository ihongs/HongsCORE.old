package app.hongs.action;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsError;
import app.hongs.util.JSON;
import app.hongs.util.Tree;

/**
 * <h1>动作助手类</h1>
 * <pre>
 * 通过getRequestMap,getParameter,getSession,getCookie来获取请求/会话/Cookie数据;
 * 通过succeeed,fail来通知前端动作的成功或失败.
 * </pre>
 *
 * @author Hongs
 */
public class ActionHelper
{

  /**
   * HttpServletRequest
   */
  public HttpServletRequest request;

  /**
   * HttpServletResponse
   */
  public HttpServletResponse response;

  /**
   * 请求数据
   */
  protected Map<String, Object> requestData;

  /**
   * 返回数据
   */
  protected Map<String, Object> responseData;

  /**
   * 初始化层级
   */
  protected short INITID;

  /**
   * 初始化标识
   */
  protected String APRSID;

  /**
   * 初始化助手
   *
   * @param req
   * @param rsp
   */
  public void init(HttpServletRequest req, HttpServletResponse rsp)
  {
    this.request = req;
    this.response = rsp;

    this.requestData = null;
    this.responseData = null;

    try
    {
      this.request.setCharacterEncoding("UTF-8");
      this.response.setCharacterEncoding("UTF-8");
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new HongsError(0x31, "Can not set request or response encoding.");
    }
  }

  /**
   * 初始化助手(用于shell)
   *
   * @param req
   * @param rsp
   */
  public void init(Map<String, String[]> req)
  {
    if (req != null)
    {
      this.requestData = parseParams(req);
    }
    else
    {
      this.requestData = new HashMap(   );
    }
  }

  /**
   * 获取请求的文本
   * @return 请求文本
   */
  public String getRequestText()
  {
    try
    {
      BufferedReader reader = this.request.getReader();
      String text = "";
      char[] buf = new char[1024];
      int i = 0, j = 0;

      while ((i =reader.read(buf, j, 1024)) != -1)
      {
        text += new String(buf);
        j += i * 1024;
      }

      return text;
    }
    catch (IOException ex)
    {
      return "";
    }
  }

  /**
   * 获取请求的Map数据
   * <pre>
   * 不同于request.getParameterMap, 该方法会将带"[]"的拆成子List, 将带"[xxx]"拆成子
   * Map, 并逐级递归. 页可以解析"."分割的参数, 如a.b.c与上面的a[b][c]是同样的效果.
   * </pre>
   * @return Map对象
   */
  public Map<String, Object> getRequestData()
  {
    if (this.requestData == null)
    {
        // 增加按Content-Type解析请求数据, 2013/02/22
        String ct = this.request.getContentType();
        if (ct != null) {
            if ("application/json".equals(ct) || "text/json".equals(ct)) {
                this.requestData = (Map<String, Object>)
                    JSON.parse(this.getRequestText());
                return this.requestData;
            }
        }

        this.requestData = ActionHelper.parseParams(
                           this.request.getParameterMap());
    }
    return this.requestData;
  }

  /**
   * 获取返回数据
   * <pre>
   * 注意: 该函数为过滤器提供原始返回数据参考, 只有使用了pass,fail,back函数返回的数据
   * 才会被记录, 其他方式返回的数据均不会记录在此, 使用时要判断是否为null.
   * </pre>
   * @return 返回数据
   */
  public Map<String, Object> getResponseData()
  {
    return this.responseData;
  }

  /**
   * 同request.getParameter
   * @param name
   * @return 当前请求参数
   */
  public String getParameter(String name)
  {
    return this.request.getParameter(name);
  }

  /**
   * 同request.getAtribute
   * @param name
   * @return 请求环境属性
   */
  public Object getAttribute(String name)
  {
    return this.request.getAttribute(name);
  }

  /**
   * 同request.setAttribute
   * @param name
   * @param value
   */
  public void setAttribute(String name, Object value)
  {
    this.request.setAttribute(name, value);
  }

  /**
   * 获取session
   * 没有则返回null
   * @param name
   * @return Session值
   */
  public Object getSession(String name)
  {
      return this.request.getSession( )
                    .getAttribute(name);
  }

  /**
   * 设置session
   * @param name
   * @param value
   */
  public void setSession(String name, Object value)
  {
      this.request.getSession(  true  )
             .setAttribute(name, value);
  }

  /**
   * 获取cookie
   * 没有则返回null
   * @param name
   * @return Cookie
   */
  public Cookie getCookie(String name)
  {
    Cookie[] cookies = this.request.getCookies();

    if (cookies != null)
    {
      for (int i = 0; i < cookies.length; i ++ )
      {
        if ( cookies[i].getName().equals(name) )
        {
          return cookies[i];
        }
      }
    }

    return null;
  }

  /**
   * 设置cookie
   * @param name
   * @param value
   * @return Cookie
   */
  public Cookie setCookie(String name, String value)
  {
    Cookie cookie = this.getCookie(name);

    if (cookie == null)
    {
      cookie = new Cookie(name,value);
      this.response.addCookie(cookie);
    }
    else
    {
      cookie.setValue(value);
    }

    return cookie;
  }

  /**
   * 设置语言标识到SESSION
   * @param lang
   */
  public void setLangToSession(String lang)
  {
    CoreConfig conf = (CoreConfig)Core.getInstance("app.hongs.CoreConfig");
    String name = conf.getProperty("core.language.session", "lang");
    this.setSession(name, lang);
  }

  /**
   * 设置语言标识到COOKIE
   * @param lang
   */
  public void setLangToCookie(String lang)
  {
    CoreConfig conf = (CoreConfig)Core.getInstance("app.hongs.CoreConfig");
    String name = conf.getProperty("core.language.session", "lang");
    this.setCookie(name, lang);
  }

  /** 发送内容 **/

  /**
   * 输出内容
   * @param text
   * @param type Content-Type定义, 如text/html
   */
  public void print(String text, String type)
  {
    if (!this.response.isCommitted())
    {
      this.setContentType(type);
    }

    try
    {
      /*
      String ae = this.request.getHeader("Accept-Encoding");
      if (ae != null && ae.indexOf("compress") != -1 && ae.indexOf("gzip") != -1)
      {
        // TODO: 将数据进行压缩处理
        return;
      }
      */
      PrintWriter out = this.response.getWriter();
      out.print(text);
    }
    catch (IOException ex)
    {
      throw new HongsError(0x33, "Can not print to browser");
    }
  }

  /**
   * 输出纯文本内容
   * @param text
   */
  public void print(String text)
  {
    this.print(text, "text/plain");
  }

  /**
   * 输出HTML内容
   * @param text
   */
  public void printHTML(String text)
  {
    this.print(text, "text/html");
  }

  /**
   * 输出XML内容
   * @param text
   */
  public void printXML(String text)
  {
    this.print(text, "text/xml");
  }

  /**
   * 输出CSS内容
   * @param text
   */
  public void printCSS(String text)
  {
    this.print(text, "text/css");
  }

  /**
   * 输出JS内容
   * @param text
   */
  public void printJS(String text)
  {
    this.print(text, "application/javascript");
  }

  /** 发送数据 **/

  /**
   * 发送JSON格式的数据
   *
   * @param text
   */
  public void printJSON(String text)
  {
    this.print(text, "application/json");
  }

  /**
   * 发送JSON格式的数据
   *
   * @param list
   */
  public void printJSON(List list)
  {
    this.printJSON(JSON.toString(list));
  }

  /**
   * 发送JSON格式的数据
   *
   * @param data
   */
  public void printJSON(Map data)
  {
    this.printJSON(JSON.toString(data));
  }

  /**
   * 发送一个成功信号
   *
   * @param rst 通讯数据
   */
  public void pass(Map<String, Object> rst)
  {
    rst.put("__success__", true);
    this.responseData = rst;
    this.printJSON(rst);
  }

  /**
   * 发送一个成功信号
   *
   * @param msg 成功消息
   */
  public void pass(String msg)
  {
    Map arr = new HashMap();
    arr.put("__message__", msg);
    this.pass(arr);
  }

  /**
   * 发送一个成功信号
   *
   */
  public void pass()
  {
    Map arr = new HashMap();
    this.pass(arr);
  }

  /**
   * 发送一个失败信号
   *
   * @param rst 通讯数据
   */
  public void fail(Map<String, Object> rst)
  {
    rst.put("__success__",false);
    this.responseData = rst;
    this.printJSON(rst);
  }

  /**
   * 发送一个失败信号
   *
   * @param msg 失败消息
   */
  public void fail(String msg)
  {
    Map arr = new HashMap();
    arr.put("__message__", msg);
    this.fail(arr);
  }

  /**
   * 发送一个失败信号
   *
   */
  public void fail()
  {
    Map arr = new HashMap();
    this.fail(arr);
  }

  /** 快捷动作 **/

  /**
   * 返回指定数据
   * 针对model的getList,getInfo方法
   * @param data
   */
  public void back(Map<String, Object> rst)
  {
    this.pass(rst);
  }

  /**
   * 返回指定数据
   * 针对model的save方法
   * @param id
   */
  public void back(String id)
  {
    Map data = new HashMap();
    data.put("id", id);
    this.pass(data);
  }

  /**
   * 返回操作数量
   * 针对model的remove方法
   * @param num
   */
  public void back(Number num)
  {
    Map data = new HashMap();
    data.put("num", num);
    this.pass(data);
  }

  /**
   * 返回检验结果
   * 针对model的exists方法
   * @param rst
   */
  public void back(Boolean rst)
  {
    if (rst) this.pass();
    else     this.fail();
  }

  /** HTTP常用状态 **/

  /**
   * 301重定向
   * @param url
   */
  public void print301Code(String url)
  {
    this.response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    this.response.setHeader("Location", url);
  }

  /**
   * 302重定向
   * @param url
   */
  public void print302Code(String url)
  {
    this.response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
    this.response.setHeader("Location", url);
  }

  /**
   * 403禁止访问
   * @param msg
   */
  public void print403Code(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    this.print(msg);
  }

  /**
   * 404缺少页面
   * @param msg
   */
  public void print404Code(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    this.print(msg);
  }

  /**
   * 500内部错误
   * @param msg
   */
  public void print500Code(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    this.print(msg);
  }

  /** HTTP内容类型 **/

  /**
   * 设置Content-Type
   * @param type
   */
  public void setContentType(String type)
  {
    if (type.indexOf(";") == -1) type += "; charset=utf-8";
    this.response.setContentType(type);
  }

  public void setLastModified(Date date)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                     sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    this.response.setHeader("Last-Modified", sdf.format(date));
  }

  /** 静态工具方法 **/

  /**
   * 解析参数
   * @param params
   * @return 解析后的Map
   */
  protected static Map parseParams(Map<String, String[]> params)
  {
    Map<String, Object> paramz = new HashMap();
    for (Map.Entry et : params.entrySet())
    {
      String key = (String)et.getKey();
      String[] value = (String[])et.getValue();
      for (int i = 0; i < value.length; i ++ )
      {
        Tree.setArrayValue(paramz, key, value[i]);
      }
    }
    return paramz;
  }

}
