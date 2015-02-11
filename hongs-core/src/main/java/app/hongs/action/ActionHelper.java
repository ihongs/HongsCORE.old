package app.hongs.action;

import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.util.Data;
import app.hongs.util.Dict;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 动作助手
 *
 * <p>
 * 通过 getRequestData,getParameter,getAttribute,getSessValue
 * 来获取请求/容器/会话; 通过 reply 来通知前端动作的成功或失败
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
  private Map<String, Object> requestData = null;

  /**
   * 容器数据
   */
  private Map<String, Object> contextData = null;

  /**
   * 会话数据
   */
  private Map<String, Object> sessionData = null;

  /**
   * HttpServletResponse
   */
  private HttpServletResponse response;

  /**
   * 响应数据
   */
  private Map<String, Object> responseData = null;

  /**
   * 响应输出
   */
  private PrintWriter /* * */ responseWrtr = null;

  public ActionHelper()
  {
    throw new HongsError(HongsError.COMMON, "Please use the ActionHelper in the coverage of the ActionWarder or CmdletRunner inside");
  }

  /**
   * 初始化助手(用于cmdlet)
   *
   * @param req
   * @param att
   * @param ses
   * @param out
   */
  public ActionHelper(Map req, Map att, Map ses, PrintWriter out)
  {
    this.request      = null;
    this.requestData  = req != null ? req : new HashMap();
    this.contextData  = att != null ? att : new HashMap();
    this.sessionData  = ses != null ? ses : new HashMap();
    this.response     = null;
    this.responseWrtr = out != null ? out : new PrintWriter(System.out);
  }

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

    try
    {
      this.request .setCharacterEncoding("UTF-8");
      this.response.setCharacterEncoding("UTF-8");
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new HongsError(0x31, "Can not set encoding.", ex);
    }
  }

  public void reinitHelper(HttpServletRequest req, HttpServletResponse rsp)
  {
    this.request  = req;
    this.response = rsp;

    try
    {
      this.request .setCharacterEncoding("UTF-8");
      this.response.setCharacterEncoding("UTF-8");
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new HongsError(0x31, "Can not set encoding.", ex);
    }
  }

  public HttpServletRequest getRequest()
  {
    return this.request;
  }

  /**
   * 获取请求数据
   *
   * 不同于 request.getParameterMap,
   * 该方法会将带"[]"的拆成子List, 将带"[xxx]"的拆成子Map, 并逐级递归.
   * 也可以解析用"."连接的参数, 如 a.b.c 与上面的 a[b][c] 是同样的效果.
   * 故请务必注意参数中的"."和"[]"符号.
   *
   * @return 请求数据
   * @throws app.hongs.HongsException
   */
  public Map<String, Object> getRequestData() throws HongsException
  {
    if (this.requestData == null)
    {
      String ct = this.request.getContentType();
      if (null != ct && ("text/json".equals(ct) || "application/json".equals(ct)))
      {
        this.requestData = (Map<String, Object>) Data.toObject(getRequestText( ) );
      }
      else
      {
        this.requestData = parseParam(request.getParameterMap());
      }
    }
    return this.requestData;
  }

  /**
   * 获取请求文本
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

  public HttpServletResponse getResponse()
  {
    return this.response;
  }

  /**
   * 获取响应数据

 注意:
 该函数为过滤器提供原始的返回数据,
 只有使用 reply 函数返回的数据才会被记录,
 其他方式返回的数据均不会记录在此,
 使用时务必判断是否为 null.
   *
   * @return 响应数据
   */
  public Map<String, Object> getResponseData()
  {
    return this.responseData;
  }

  /**
   * 获取响应输出
   * @return 响应输出
   */
  public PrintWriter getResponseWrtr()
  {
    if (this.responseWrtr != null)
    {
        return this.responseWrtr;
    }
    else
    {
      try
      {
        return this.response.getWriter();
      }
      catch (IOException ex)
      {
        throw new HongsError(0x32, "Can not send to browser.", ex);
      }
    }
  }

  /**
   * 获取请求参数
   * @param name
   * @return 当前请求参数
   * @throws HongsException
   */
  public String getParameter(String name) throws HongsException
  {
    Object o = Dict.getParam(getRequestData(), name);
    if (o == null)
    {
      return null;
    }
    if (o instanceof Map )
    {
      o = new ArrayList(((Map) o).values());
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
   * 获取容器属性
   * 注意; 为防止歧义, 请不要在 name 中使用 "[","]"和"."
   * @param name
   * @return 当前属性, 没有则为null
   */
  public Object getAttribute(String name)
  {
    if (this.contextData != null) {
      return this.contextData.get(name);
    } else {
      return this.getRequest().getAttribute(name);
    }
  }

  /**
   * 设置容器属性
   * 注意; 为防止歧义, 请不要在 name 中使用 "[","]"和"."
   *       当 value 为 null 时 name 对应的会话属性将删除
   * @param name
   * @param value
   */
  public void setAttribute(String name, Object value)
  {
    if (this.contextData != null) {
      if (value == null) {
        this.contextData.remove(name);
      } else {
        this.contextData.put(name, value);
      }
    } else {
      if (value == null) {
        this.getRequest().removeAttribute(name);
      } else {
        this.getRequest().setAttribute(name, value);
      }
    }
  }

  /**
   * 获取会话取值
   * 注意; 为防止歧义, 请不要在 name 中使用 "[","]"和"."
   * @param name
   * @return 当前取值, 没有则为null
   */
  public Object getSessvalue(String name)
  {
    if (this.sessionData != null) {
      return this.sessionData.get(name);
    } else {
      HttpSession sess = this.getRequest().getSession();
      if (null == sess) return null ;
      return sess.getAttribute(name);
    }
  }

  /**
   * 设置会话取值
   * 注意; 为防止歧义, 请不要在 name 中使用 "[","]"和"."
   *       当 value 为 null 时 name 对应的会话属性将删除
   * @param name
   * @param value
   */
  public void setSessvalue(String name, Object value)
  {
    if (this.sessionData != null) {
      if (value == null) {
        this.sessionData.remove(name);
      } else {
        this.sessionData.put(name, value);
      }
    } else {
      if (value == null) {
        HttpSession sess = this.getRequest().getSession();
        if (null != sess) sess.removeAttribute(name);
      } else {
        HttpSession sess = this.getRequest().getSession(true);
        if (null != sess) sess.setAttribute(name, value);
      }
    }
  }

  //** 返回数据 **/

  /**
   * 返回响应数据
   * 针对 retrieve 等
   * @param map
   */
  public void reply(Map<String, Object> map)
  {
    if (null != map) {
        if(!map.containsKey("ok" )) {
            map.put( "ok" , true );
        }
        if(!map.containsKey("err")) {
            map.put( "err",  ""  );
        }
        if(!map.containsKey("msg")) {
            map.put( "msg",  ""  );
        }
    }
    this.responseData = map;
  }

  /**
   * 返回操作结果
   * 针对 create,update,remove 等
   * @param msg
   * @param o
   */
  public void reply(String msg, Object... o)
  {
    Map map = new HashMap();
    map.put("ok", true);
    if (null !=  msg  ) {
        map.put("msg", msg);
    }
    if (null != o && o.length > 0) {
        map.put("back", o );
    }
    reply(map);
  }

  /**
   * 返回检查结果
   * 针对 exists,unique 等
   * @param ok
   */
  public void reply(boolean ok)
  {
    Map map = new HashMap();
    map.put("ok", ok);
    reply(map);
  }

  //** 输出内容 **/

  /**
   * 输出内容
   * @param txt
   * @param cst Content-Type 编码, 如 utf-8
   * @param ctt Content-Type 定义, 如 text/html
   */
  public void print(String txt, String ctt, String cst)
  {
    if (this.response != null && !this.response.isCommitted())
    {
      if (cst != null) {
          this.response.setCharacterEncoding(cst);
      }
      if (ctt != null) {
          this.response.setContentType(ctt);
      }
    }
    this.getResponseWrtr().print(txt);
  }

  /**
   * 输出文本内容
   * @param txt
   * @param ctt
   */
  public void print(String txt, String ctt)
  {
    this.print(txt, ctt, "utf-8");
  }

  /**
   * 输出文本内容
   * @param htm
   */
  public void print(String htm)
  {
    this.print(htm, "text/html");
  }

  /**
   * 输出JSON格式
   *
   * @param dat
   */
  public void print(Object dat)
  {
    this.print(Data.toString(dat), "application/json");
  }

  /**
   * 直接输出数据
   */
  public void print()
  {
    this.print(responseData);
    this.responseData = null;
  }

  //** 跳转及错误 **/

  /**
   * 302重定向
   * @param url
   */
  public void redirect(String url)
  {
    this.response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
    this.response.setHeader("Location", url);
    this.responseData = null;
  }

  /**
   * 403禁止访问
   * @param msg
   */
  public void error403(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    this.responseData = null;
    this.print(msg);
  }

  /**
   * 404缺少页面
   * @param msg
   */
  public void error404(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    this.responseData = null;
    this.print(msg);
  }

  /**
   * 500内部错误
   * @param msg
   */
  public void error500(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    this.responseData = null;
    this.print(msg);
  }

  /**
   * 500 系统异常
   * @param ex
   */
  public void error500(Throwable ex)
  {
    this.error500(ex.getLocalizedMessage());
  }

  //** 工具方法 **/

  /**
   * 解析参数
   * @param params
   * @return 解析后的Map
   */
  public static Map parseParam(Map<String, String[]> params)
  {
    Map<String, Object> paramz = new HashMap();
    for (Map.Entry et : params.entrySet())
    {
      String key = (String)et.getKey();
      String[] value = (String[])et.getValue();
      for (int i = 0; i < value.length; i ++ )
      {
        Dict.setParam( paramz, value[i], key );
      }
    }
    return paramz;
  }

}
