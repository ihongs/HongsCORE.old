package app.hongs.action;

import app.hongs.HongsError;
import app.hongs.util.JSON;
import app.hongs.util.Tree;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 动作助手
 *
 * <p>
 通过 getRequestData,getParam,getSession,getCookie
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
   * 会话数据
   */
  protected Map<String, Object> session;

  /**
   * 客户数据
   */
  protected Map<String, Object> cookies;

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
   * @param ses
   * @param cok
   */
  public void init(Map<String, String[]> req, Map<String, String[]> ses, Map<String, String[]> cok)
  {
    if (req != null)
    {
      this.requestData = parseParams(req);
    }
    else
    {
      this.requestData = new HashMap();
    }
    if (ses != null)
    {
      this.session = parseParams(ses);
    }
    else
    {
      this.session = new HashMap();
    }
    if (ses != null)
    {
      this.cookies = parseParams(cok);
    }
    else
    {
      this.cookies = new HashMap();
    }
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
  public Map<String, Object> getRequestData()
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
   * 获取数据
   * @param name
   * @return 当前请求数据
   */
  public Object getValue(String name)
  {
    return Tree.getValue(this.getRequestData(), name);
  }

  /**
   * 获取参数
   * @param name
   * @return 当前请求参数
   */
  public String getParam(String name)
  {
    Object o = this.getValue(name);
    if (o instanceof List)
    {
      List l = (List) o;
      int  i = l.size();
      o =  l.get(i + 1);
    }
    return o.toString();
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
    if (this.session != null)
    {
      return this.session.get(name);
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
    if (this.session != null)
    {
      this.session.put(name, value);
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
    if (this.cookies != null)
    {
        return this.cookies.get(name).toString();
    }

    Cookie ck  = this.getCookia(name);
    return ck == null  ?  null  :  ck.getValue();
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
    if (this.cookies != null)
    {
        this.cookies.put (name, value);
        return new Cookie(name, value);
    }

    return this.setCookia(name, value);
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
   * 返回保存结果
   * 针对model的save方法
   * @param msg
   * @param rst
   */
  public void back(String msg, Object... rst)
  {
    Map data = new HashMap();
    data.put("__success__",true);
    data.put("__message__", msg);
    data.put("back", rst);
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
   * 针对model的delete方法
   * @param rst
   */
  public void back(String msg)
  {
    back(msg, true);
  }

  /**
   * 返回检验结果
   * 针对model的exists方法
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
    if (!this.response.isCommitted())
    {
      if (type.indexOf(";") == -1) type += "; charset=utf-8";
      this.response.setContentType(type);
    }

    try
    {
      this.response.getWriter().print(text);
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

  //** 发送数据 **/

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

  //** 工具方法 **/

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
        Tree.setValue(paramz, key, value[i]);
      }
    }
    return paramz;
  }

}
