package app.hongs.action;

import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.util.JSON;
import app.hongs.util.Tree;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 动作助手
 *
 * <p>
 通过 getRequestData,getParameter,getSession,getCookie
 来获取请求/会话/Cookie数据; 通过 back 来通知前端动作的成功或失败.
 </p>
 *
 * @author Hongs
 */
public class ActionHelper
{

  /**
   * HttpServletRequest
   */
  private HttpServletRequest request;

  /**
   * 请求数据
   */
  private Map<String, Object> requestData;

  /**
   * 会话数据
   */
  private Map<String, Object> sessionData;

  /**
   * 客户数据
   */
  private Map<String, String> cookiesData;

  /**
   * HttpServletResponse
   */
  private HttpServletResponse response;

  /**
   * 内部输出
   */
  private PrintWriter responseWrtr;

  /**
   * 返回数据
   */
  private Map<String, Object> responseData;

  /**
   * 初始化助手(用于action)
   *
   * @param req
   * @param rsp
   */
  public ActionHelper(HttpServletRequest req, HttpServletResponse rsp)
  {
    this.request  = req;
    this.response = rsp;

    this.requestData  = null;
    this.responseData = null;

    try
    {
      this.request .setCharacterEncoding("UTF-8");
      this.response.setCharacterEncoding("UTF-8");
      this.responseWrtr  =  rsp.getWriter();
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new HongsError(0x21, "Can not set rs encoding.", ex);
    }
    catch (IOException ex)
    {
      throw new HongsError(0x22, "Can not send to browser.", ex);
    }
  }

  /**
   * 初始化助手(用于cmdlet)
   *
   * @param req
   * @param ses
   * @param cok
   * @param out
   */
  public ActionHelper(
          Map<String, String[]> req,
          Map<String, String[]> ses,
          Map<String, String[]> cok,
          PrintWriter out)
  {
    try
    {
      this.requestData  = req != null ? parseParams(req) : new HashMap( );
      this.sessionData  = req != null ? parseParams(ses) : new HashMap( );
      this.cookiesData  = req != null ? parseParams(cok) : new HashMap( );
      this.responseWrtr = out != null ? out : new PrintWriter(System.err);
    }
    catch (HongsException ex)
    {
      throw new HongsError(0x23, "Can not parse params", ex);
    }
  }

  public HttpServletRequest getRequest()
  {
    return this.request;
  }

  /**
   * 获取请求的文本
   * @return 请求文本
   */
  public String getRequestBody()
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
   *
   * 不同于 request.getParameterMap,
   * 该方法会将带"[]"的拆成子List, 将带"[xxx]"的拆成子Map, 并逐级递归.
   * 也可以解析用"."连接的参数, 如 a.b.c 与上面的 a[b][c] 是同样的效果.
   * 故请务必注意参数中的"."和"[]"符号.
   *
   * @return Map对象
   */
  public Map<String, Object> getRequestData() throws HongsException
  {
    if (this.requestData == null)
    {
      String ct = this.request.getContentType();
      if (null != ct && ("text/json".equals(ct) || "application/json".equals(ct) ))
      {
        this.requestData = (Map<String, Object>) JSON.parse(this.getRequestBody( ));
      }
      else
      {
        this.requestData = ActionHelper.parseParams(this.request.getParameterMap());
      }
    }
    return this.requestData;
  }

  public HttpServletResponse getResponse()
  {
    return this.response;
  }

  public PrintWriter getResponseWrtr()
  {
    return this.responseWrtr;
  }

  /**
   * 获取返回数据
   *
   * 注意:
   * 该函数为过滤器提供原始的返回数据,
   * 只有使用 back 函数返回的数据才会被记录,
   * 其他方式返回的数据均不会记录在此,
   * 使用时务必判断是否为 null.
   *
   * @return 返回数据
   */
  public Map<String, Object> getResponseData()
  {
    return this.responseData;
  }

  /**
   * 获取参数
   * @param name
   * @return 当前请求参数
   * @throws HongsException
   */
  public String getParameter(String name) throws HongsException
  {
    Object o = this.getAttribute(name);
    if (o == null)
    {
      return null;
    }
    if (o instanceof List)
    {
      List l = (List) o;
      int  i = l.size();
      o =  l.get(i + 1);
    }
    return o.toString();
  }

  /**
   * 获取属性
   * @param name
   * @return 当前请求数据
   * @throws HongsException
   */
  public Object getAttribute(String name) throws HongsException
  {
    return Tree.getValue(this.getRequestData(), name);
  }

  /**
   * 设置属性
   * @param name
   * @param value
   * @throws HongsException
   */
  public void setAttribute(String name, Object value) throws HongsException
  {
    Tree.setValue(this.getRequestData(), name, value);
  }

  /**
   * 获取session
   * 没有则返回null
   * 注意; 为防止歧义, 请不要在 name 中使用"[","]"和"."
   * @param name
   * @return Session值
   */
  public Object getSession(String name)
  {
    if (this.sessionData != null)
    {
      return this.sessionData.get(name);
    }
    else
    {
      return this.request.getSession(/**/).getAttribute(name);
    }
  }

  /**
   * 设置session
   * 注意; 为防止歧义, 请不要在 name 中使用"[","]"和"."
   * @param name
   * @param value
   */
  public void setSession(String name, Object value)
  {
    if (this.sessionData != null)
    {
      this.sessionData.put(name, value);
    }
    else
    {
      this.request.getSession(true).setAttribute(name, value);
    }
  }

  /**
   * 获取cookie
   * 没有则返回null
   * 注意; 为防止歧义, 请不要在 name 中使用"[","]"和"."
   * @param name
   * @return Cookie值
   */
  public String getCookie(String name)
  {
    if (this.cookiesData != null)
    {
      return this.cookiesData.get(name);
    }

    Cookie cook  = this.getCookia(name);
    return cook == null ? null : cook.getValue();
  }

  /**
   * 设置cookie
   * 注意; 为防止歧义, 请不要在 name 中使用"[","]"和"."
   * @param name
   * @param value
   * @return Cookie
   */
  public Cookie setCookie(String name, String value)
  {
    if (this.cookiesData != null)
    {
      this.cookiesData.put(name, value);
      return   new  Cookie(name, value);
    }

    return  this.setCookia(name, value);
  }

  private Cookie getCookia(String name)
  {
    Cookie[] cookiez = this.request.getCookies();

    if (cookiez != null)
    {
      for (int i = 0; i < cookiez.length; i ++ )
      {
        if ( cookiez[i].getName().equals(name) )
        {
          return cookiez[i];
        }
      }
    }

    return null;
  }

  private Cookie setCookia(String name, String value)
  {
    Cookie cookie = this.getCookia(name);

    if (cookie != null)
    {
      cookie.setValue(value);
    }
    else
    {
      cookie = new Cookie(name,value);
    }
    this.response.addCookie( cookie );

    return cookie;
  }

  //** 快捷动作 **/

  /**
   * 返回指定数据
   * 针对model的getList,getInfo方法
   * @param rst
   */
  public void back(Map<String, Object> rst)
  {
    // 默认为成功
    if(!rst.containsKey("__success__"))
        rst.put( "__success__", true );
    this.responseData = rst;
  }

  /**
   * 返回保操作结果
   * 针对model的save,remove方法
   * @param msg
   * @param rst
   */
  public void back(String msg, Object... rst)
  {
    Map data = new HashMap();
    data.put("__success__",true);
    data.put("__message__", msg);
    if (rst != null && rst.length > 0) {
        data.put( "back"  , rst);
    }
    back(data);
  }

  /**
   * 返回操作结果
   * @param rst
   * @param msg
   */
  public void back(String msg, boolean rst)
  {
    Map data = new HashMap();
    data.put("__success__", rst);
    data.put("__message__", msg);
    back(data);
  }

  /**
   * 返回操作消息
   * @param msg 
   */
  public void back(String msg)
  {
    back(msg, true);
  }

  /**
   * 返回检验结果
   * @param rst
   */
  public void back(Boolean rst)
  {
    back(null, rst);
  }

  //** 输出内容 **/

  /**
   * 输出内容
   * @param text
   * @param type Content-Type定义, 如text/html
   */
  public void print(String text, String type)
  {
    if (this.response != null && ! this.response.isCommitted())
    {
      if (!type.contains(";")) type += "; charset=utf-8";
      this.response.setContentType(type);
    }

    this.getResponseWrtr(  ).print(text);
  }

  /**
   * 输出文本内容
   * @param text
   */
  public void print(String text)
  {
    this.print(text, "text/plain");
  }

  /**
   * 输出JSON格式
   *
   * @param list
   */
  public void print(Object data)
  {
    this.print(JSON.toString(data), "application/json");
  }

  //** 常用状态 **/

  /**
   * 301重定向
   * @param url
   */
  public void print301(String url)
  {
    this.response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    this.response.setHeader("Location", url);
  }

  /**
   * 302重定向
   * @param url
   */
  public void print302(String url)
  {
    this.response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
    this.response.setHeader("Location", url);
  }

  /**
   * 403禁止访问
   * @param msg
   */
  public void print403(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    this.print(msg);
  }

  /**
   * 404缺少页面
   * @param msg
   */
  public void print404(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    this.print(msg);
  }

  /**
   * 500内部错误
   * @param msg
   */
  public void print500(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    this.print(msg);
  }

  /**
   * 500 系统异常
   * @param ex
   */
  public void print500(Exception ex)
  {
    ActionHelper.this.print500(ex.getMessage());
  }

  //** 工具方法 **/

  /**
   * 解析参数
   * @param params
   * @return 解析后的Map
   */
  protected static Map parseParams(Map<String, String[]> params)
  throws HongsException
  {
    Map<String, Object> paramz = new HashMap();
    for (Map.Entry et : params.entrySet())
    {
      String key = (String)et.getKey();
      String[] value = (String[])et.getValue();
      for (int i = 0; i < value.length; i ++ )
      {
        Tree.setValue(paramz, key, value[i]);
      }
    }
    return paramz;
  }

}
