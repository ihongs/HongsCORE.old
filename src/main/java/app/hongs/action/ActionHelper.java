package app.hongs.action;

import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.util.Data;
import app.hongs.util.Tree;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
  private Map<String, Object> requestData;

  /**
   * 容器数据
   */
  private Map<String, Object> contextData;

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
    this.requestData  = req != null ? req : new LinkedHashMap( );
    this.contextData  = att != null ? att : new LinkedHashMap( );
    this.sessionData  = ses != null ? ses : new LinkedHashMap( );
    this.responseWrtr = out != null ? out : new PrintWriter(System.err);
  }

  /**
   * 初始化助手(用于action)
   *
   * @param req
   * @param rsp
   */
  public ActionHelper(HttpServletRequest req, HttpServletResponse rsp)
  {
    this.request      = req ;
    this.response     = rsp ;
    this.requestData  = null;
    this.responseData = null;

    try
    {
      this.request .setCharacterEncoding("UTF-8");
      this.response.setCharacterEncoding("UTF-8");
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new HongsError(0x21, "Can not set encoding.", ex);
    }
  }

  /**
   * 重置请求响应对象
   *
   * 供 ActionWarder 调用, 因 forward 后 response 会变
   *
   * @param req
   * @param rsp
   * @throws IOException
   */
  protected void reinitHelper(HttpServletRequest req, HttpServletResponse rsp)
  throws IOException
  {
    request      = req;
    response     = rsp;
//  requestData  = null;
//  responseData = null;

    if (responseWrtr != null)
    {
        responseWrtr  = rsp.getWriter();
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
      if (null != ct && ("text/json".equals(ct) || "application/json".equals(ct) ))
      {
        this.requestData = (Map<String,Object>)Data.toObject(this.getRequestText());
      }
      else
      {
        this.requestData = ActionHelper.parseParam(this.request.getParameterMap());
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
    Object o = Tree.getValue2(getRequestData(), name);
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
  public Object getSessValue(String name)
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
  public void setSessValue(String name, Object value)
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
   * 返回指定数据
   * 针对 retrieve 等
   * @param map
   */
  public void reply(Map<String, Object> map)
  {
    // 默认为成功
    if(!map.containsKey("ok" )) {
        map.put( "ok" , true );
    }
    if(!map.containsKey("err")) {
        map.put( "err",  ""  );
    }
    if(!map.containsKey("msg")) {
        map.put( "msg",  ""  );
    }
    this.responseData = map;
  }

  /**
   * 返回保操作结果
   * 针对 create,update,remove
   * @param msg
   * @param o
   */
  public void reply(String msg, Object... o)
  {
    if (msg == null) msg = "";
    Map data = new LinkedHashMap();
    data.put("ok" ,true);
    data.put("msg", msg);
    if (o != null && o.length > 0) {
        data.put("back", o);
    }
    reply(data);
  }

  /**
   * 返回操作结果
   * 针对 exists,unique 等
   * @param msg
   * @param ok
   */
  public void reply(String msg, boolean ok)
  {
    if (msg == null) msg = "";
    Map data = new LinkedHashMap();
    data.put("ok" , ok );
    data.put("msg", msg);
    reply(data);
  }

  //** 输出内容 **/

  /**
   * 输出内容
   * @param txt
   * @param ctt Content-Type定义, 如text/html
   */
  public void print(String txt, String ctt)
  {
    if (this.response != null && ! this.response.isCommitted())
    {
      if (!ctt.contains(";")) ctt += "; charset=utf-8";
      this.response.setContentType(ctt);
    }

    this.getResponseWrtr(  ).print(txt);
  }

  /**
   * 输出文本内容
   * @param txt
   */
  public void print(String txt)
  {
    this.print(txt, "text/plain");
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
  public void error500(Throwable ex)
  {
    this.error500(ex.getLocalizedMessage());
  }

  //** 工具方法 **/

  /**
   * 解析参数
   * @param params
   * @return 解析后的Map
   * @throws app.hongs.HongsException
   */
  public static Map parseParam(Map<String, String[]> params)
  throws HongsException
  {
    Map<String, Object> paramz = new HashMap();
    for (Map.Entry et : params.entrySet())
    {
      String key = (String)et.getKey();
      String[] value = (String[])et.getValue();
      for (int i = 0; i < value.length; i ++ )
      {
        Tree.setValue(paramz, value[i], key);
      }
    }
    return paramz;
  }

}
