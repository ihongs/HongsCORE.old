package app.hongs.action;

import app.hongs.Core;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.util.Data;
import app.hongs.util.Tree;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 动作助手
 *
 * <p>
 * 通过 getRequestData,getParameter,getAttribute,getCookie
 * 来获取请求/会话/Cookie数据; 通过 reply 来通知前端动作的成功或失败
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
   * HttpServletResponse
   */
  private HttpServletResponse response;

  /**
   * 响应数据
   */
  private Map<String, Object> responseData;

  /**
   * 响应输出
   */
  private PrintWriter responseWrtr;

  /**
   * 初始化助手(用于cmdlet)
   *
   * @param req
   * @param ses
   * @param out
   */
  public ActionHelper(
          Map<String, String[]> req,
          Map<String, String[]> ses,
          PrintWriter out)
  {
    try
    {
      this.requestData  = req != null ? parseParams(req) : new HashMap( );
      this.sessionData  = ses != null ? parseParams(ses) : new HashMap( );
      this.responseWrtr = out != null ? out : new PrintWriter(System.err);
    }
    catch (HongsException ex)
    {
      throw new HongsError(0x23, "Can not parse params", ex);
    }
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
    this.requestData  = null;
    this.responseData = null;

    try
    {
      this.request .setCharacterEncoding("UTF-8");
      this.response.setCharacterEncoding("UTF-8");
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new HongsError(0x21, "Can not set rs encoding.", ex);
    }
  }

  /**
   * 重置请求响应对象
   *
   * 供 ActionLoader 调用, 因 forward 后 response 会变
   *
   * @param req
   * @param rsp
   */
  protected void setInstance(HttpServletRequest req, HttpServletResponse rsp)
  throws IOException
  {
    request  = req;
    response = rsp;
    if (responseWrtr != null) {
        responseWrtr  = rsp.getWriter();
    }
  }

  public ActionHelper getInstance()
  throws HongsException
  {
    throw new HongsException(0x1100, "Please use the ActionHelper in the coverage of the ActionLoader or CmdletRunner inside");
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
      if (null != ct && ("text/json".equals(ct) || "application/json".equals(ct) ))
      {
        this.requestData = (Map<String,Object>)Data.toObject(this.getRequestText());
      }
      else
      {
        this.requestData = ActionHelper.parseParams(this.request.getParameterMap());
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
        throw new HongsError(0x22, "Can not send to browser.", ex);
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
    Object o = Tree.getValue(getRequestData(), name);
    if (o == null)
    {
      return null;
    }
//    if (o instanceof Map )
//    {
//      o = new ArrayList(((Map) o).values());
//    }
//    if (o instanceof List)
//    {
//      List l = (List) o;
//      int  i = l.size();
//      o =  l.get(i + 1);
//    }
    return o.toString();
  }

  /**
   * 获取会话属性
   * 注意; 为防止歧义, 请不要在 name 中使用 "[","]"和"."
   * @param name
   * @return 当前会话属性, 没有则为null
   */
  public Object getAttribute(String name)
  {
    if (this.sessionData != null)
    {
      return this.sessionData.get(name);
    }
    else
    {
      return this.request.getSession( ).getAttribute(name);
    }
  }

  /**
   * 设置会话属性
   * 注意; 为防止歧义, 请不要在 name 中使用 "[","]"和"."
   *       当 value 为 null 时 name 对应的会话属性将删除
   * @param name
   * @param value
   */
  public void setAttribute(String name, Object value)
  {
    if (this.sessionData != null)
    {
      if (value == null)
      {
        this.sessionData.remove(name);
      }
      else
      {
        this.sessionData.put   (name, value);
      }
    }
    else
    {
      if (value == null)
      {
        this.request.getSession( ).removeAttribute(name);
      }
      else
      {
        this.request.getSession(true).setAttribute(name, value);
      }
    }
  }

  //** 返回数据 **/

  /**
   * 返回指定数据
   * 针对model的getList,getInfo方法
   * @param rst
   */
  public void reply(Map<String, Object> rst)
  {
    // 默认加上 SESSIONID
//    if(!rst.containsKey("__session__")) {
//        String ssid = (String) getAttribute("__session__");
//        if (ssid == null  &&  request != null ) {
//            ssid = request.getSession().getId();
//        }
//        rst.put( "__session__", ssid );
//    }
    // 默认为成功
    if(!rst.containsKey("__success__")) {
        rst.put( "__success__", true );
    }
    if(!rst.containsKey("__message__")) {
        rst.put( "__message__",  ""  );
    }
    this.responseData = rst;
  }

  /**
   * 返回保操作结果
   * 针对model的save,create,modify,remove方法
   * @param msg
   * @param rst
   */
  public void reply(String msg, Object... rst)
  {
    Map data = new HashMap();
    data.put("__success__",true);
    data.put("__message__", msg);
    if (rst != null && rst.length > 0) {
        data.put( "back"  , rst);
    }
    ActionHelper.this.reply(data);
  }

  /**
   * 返回操作结果
   * @param rst
   * @param msg
   */
  public void reply(String msg, boolean rst)
  {
    Map data = new HashMap();
    data.put("__success__", rst);
    data.put("__message__", msg);
    ActionHelper.this.reply(data);
  }

  /**
   * 返回操作消息
   * @param msg
   */
  public void reply(String msg)
  {
    ActionHelper.this.reply(msg, true);
  }

  /**
   * 返回检验结果
   * 针对model的exists,unique方法
   * @param rst
   */
  public void reply(boolean rst)
  {
    ActionHelper.this.reply(null, rst);
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
   * @param data
   */
  public void print(Object data)
  {
    this.print(Data.toString(data), "application/json");
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
  }

  /**
   * 403禁止访问
   * @param msg
   */
  public void error403(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    this.print(msg);
  }

  /**
   * 404缺少页面
   * @param msg
   */
  public void error404(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    this.print(msg);
  }

  /**
   * 500内部错误
   * @param msg
   */
  public void error500(String msg)
  {
    this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    this.print(msg);
  }

  /**
   * 500 系统异常
   * @param ex
   */
  public void error500(Exception ex)
  {
    ActionHelper.this.error500(ex.getMessage());
  }

  //** 工具方法 **/

  /**
   * 解析参数
   * @param params
   * @return 解析后的Map
   * @throws app.hongs.HongsException
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
