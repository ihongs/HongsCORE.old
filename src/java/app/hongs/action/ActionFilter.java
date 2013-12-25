package app.hongs.action;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.net.URLEncoder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;

/**
 * <h1>标准动作过滤器</h1>
 *
 * <h2>初始化参数(init-param):</h2>
 * <pre>
 * index-path   首页地址(为空则不跳转)
 * login-path   登录地址(为空则不跳转)
 * check-login  检查登录(默认为false)
 * config-name  权限配置名(默认为default)
 * session-key  权限会话键(默认为actions)
 * </pre>
 *
 * <h2>注意:</h2>
 * <pre>
 * 如果权限配置名为空串或权限配置文件不存在则仅检查权限会话; 权限会话中可存放
 * Set&lt;String&gt;{动作URI...} 或 Map&lt;String, Boolean&gt;{动作URI : 审核}
 * 结构的数据; 如果不采用权限配置文件, 需用
 * Map&lt;String, Boolean&gt;
 * 类型存储权限开关.
 * </pre>
 *
 * @author Hongs
 */
public class ActionFilter
  implements Filter
{

  /**
   * 首页路径
   */
  private String indexPath;

  /**
   * 登录页路径
   */
  private String loginPath;

  /**
   * 是否检查登录
   */
  private boolean checkLogin;

  /**
   * 权限配置名
   */
  private String configName;

  /**
   * 权限会话键
   */
  private String sessionKey;

  @Override
  public void init(FilterConfig config)
    throws ServletException
  {
    /**
     * 获取首页URL
     */
    String ip = config.getInitParameter("index-path");
    if (ip != null)
    {
      this.indexPath = Core.BASE_HREF + ip;
    }

    /**
     * 获取登录页URL
     */
    String lp = config.getInitParameter("login-path");
    if (lp != null)
    {
      this.loginPath = Core.BASE_HREF + lp;
    }

    /**
     * 获取登录检查标识
     */
    String cl = config.getInitParameter("check-login");
    if (cl == null)
    {
      cl = "false";
    }
    this.checkLogin = Boolean.parseBoolean(cl);

    /**
     * 获取权限配置名
     */
    String cn = config.getInitParameter("config-name");
    if (cn == null)
    {
      cn = "default";
    }
    this.configName = cn;

    /**
     * 获取权限会话键
     */
    String sk = config.getInitParameter("session-key");
    if (sk == null)
    {
      sk = "actions";
    }
    this.sessionKey = sk;
  }

  @Override
  public void destroy()
  {
    indexPath  = null;
    loginPath  = null;
    configName = null;
    sessionKey = null;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
    throws IOException, ServletException
  {
    /*
    ActionHelper helper = (ActionHelper)Core.getInstance(app.hongs.action.ActionHelper.class);
    if (  helper.INITID == 1  )
    {
      ActionFilter.clearCache();
    }
    */

    /** 开始判断动作权限 **/

    Map<String, Boolean> sessionData = getSession(this.sessionKey);

    if (sessionData == null)
    {
      if (this.checkLogin)
      {
        this.sayError(0);
        return;
      }
      
      sessionData = new HashMap();
    }

    String act = Core.ACTION_PATH.get();

    Map<String, Boolean> configData = getConfig(this.configName);

    /**
     * 纯SESSION校验则只判断SESSION里是否有指定ACT
     * 按CONFIG校验则判断CONFIG里有而SESSION里没有指定ACT
     */
    if (configData == null)
    {
      if ( sessionData.containsKey(act) && !sessionData.get(act))
      {
        this.sayError(1);
        return;
      }
    }
    else
    if (configData.containsKey(act))
    {
      if (!sessionData.containsKey(act) || !sessionData.get(act))
      {
        this.sayError(1);
        return;
      }
    }

    chain.doFilter(req, rsp);

    /** 结束处理动作权限 **/

    ActionFilter.clearCache();
  }

  private void sayError(int type)
  {
    CoreLanguage lang   = (CoreLanguage)Core.getInstance(app.hongs.CoreLanguage.class);
    ActionHelper helper = (ActionHelper)Core.getInstance(app.hongs.action.ActionHelper.class);

    ActionFilter.clearCache();

    /**
     * 根据错误类型获取错误消息及跳转URI
     */
    String msg;
    String uri;
    if (type == 0)
    {
      msg = lang.translate("core.error.no.login");
      uri = this.loginPath;
    }
    else
    {
      msg = lang.translate("core.error.no.power");
      uri = this.indexPath;
    }

    /**
     * 追加来源URI
     */
    String src = helper.request.getRequestURI( );
    String qry = helper.request.getQueryString();
    if (src != null && src.length() != -1)
    {
      if (qry != null && qry.length() != -1)
      {
        src = src + "?" + qry;
      }

      try
      {
        src = URLEncoder.encode(src, "UTF-8");
      }
      catch (UnsupportedEncodingException ex)
      {
        src = "";
      }

      if (uri.indexOf("?") == -1)
      {
        uri += "?ref=" + src;
      }
      else
      {
        uri += "&ref=" + src;
      }
    }

    /**
     * 修改(2013/02/22):
     * 如果处于AJAX环境, 则是由JSON传递URL和消息
     * 否则使用HTTP错误代码
     */
    if (helper.request.getRequestURI().endsWith(".act")) {
        Map rsp = new HashMap();
            rsp.put("__success__", false);
            rsp.put("__message__", msg);
        if (uri != null || uri.length() != 0) {
            rsp.put("__refresh__", uri);
        }
        helper.back(rsp);
    }
    else {
        if (uri != null || uri.length() != 0) {
            helper.print302Code(uri);
        }
        else {
            helper.print403Code(msg);
        }
    }
  }

  private static Map<String, Map<String, Boolean>> getCache(int type)
  {
    String key = type == 0
      ? "app.hongs.action.ActionFilter.ConfigList"
      : "app.hongs.action.ActionFilter.SessionList";

    Core core = Core.getInstance();
    Map<String, Map<String, Boolean>> actionData;

    if (core.containsKey(key))
    {
      actionData = (Map<String, Map<String, Boolean>>)core.get(key);
    }
    else
    {
      actionData = new HashMap<String, Map<String, Boolean>>();
      core.put(key, actionData);
    }

    return actionData;
  }

  private static void clearCache()
  {
    Core core = Core.getInstance();
    if (core.containsKey("app.hongs.action.ActionFilter.ConfigList"))
    {
      core.remove("app.hongs.action.ActionFilter.ConfigList");
    }
    if (core.containsKey("app.hongs.action.ActionFilter.SessionList"))
    {
      core.remove("app.hongs.action.ActionFilter.SessionList");
    }
  }

  private static Map<String, Boolean> getConfig(String configName)
    throws IOException
  {
    Map<String, Map<String, Boolean>> configList = ActionFilter.getCache(0);
    if (configList.containsKey(configName))
    {
      return configList.get(configName);
    }

    try
    {
      Map<String, Boolean> configData = new HashMap<String, Boolean>();
      ActionConfig ac = new ActionConfig(configName);
      for (String action : ac.actions)
      {
        configData.put(action, false);
      }

      configList.put(configName, configData);
      return configData;
    }
    catch (HongsException ex)
    {
      if (ex.getCode() == 0x10e0)
      {
        configList.put(configName, null);
        return null;
      }
      else
      {
        throw new IOException(ex);
      }
    }
  }

  private static Map<String, Boolean> getSession(String sessionKey)
  {
    Map<String, Map<String, Boolean>> sessionList = ActionFilter.getCache(1);
    if (sessionList.containsKey(sessionKey))
    {
      return sessionList.get(sessionKey);
    }

    ActionHelper helper = (ActionHelper)Core.getInstance(app.hongs.action.ActionHelper.class);
    String[ ] keys = sessionKey.split("\\.");
    Object subs = helper.getSession(keys[0]);

    for (int i = 1; i < keys.length; i ++)
    {
      if (subs != null && subs instanceof Map)
      {
        subs = ((Map)subs).get(keys[i]);
      }
      else
      {
        subs = null;
        break;
      }
    }

    if (subs instanceof Set)
    {
      Set<String> subs2 = (Set<String>)subs;
      Map<String, Boolean> subs3 = new HashMap<String, Boolean>();

      Iterator it = subs2.iterator();
      while (it.hasNext())
      {
        subs3.put((String)it.next(), true);
      }

      sessionList.put(sessionKey, subs3);

      return subs3;
    }
    else
    if (subs instanceof Map)
    {
      Map<String, Boolean> subs2 = (Map<String, Boolean>)subs;

      sessionList.put(sessionKey, subs2);

      return subs2;
    }
    else
    {
      sessionList.put(sessionKey, null);

      return null;
    }
  }

  public static Map<String, Boolean> getActions(String configName, String sessionKey, boolean checkLogin)
  {
    Map<String, Boolean> sessionData = getSession(sessionKey);
    if (sessionData == null) {
        if (checkLogin) {
            return null;
        }
    }

    Map<String, Boolean> configData;
    try {
      configData = getConfig(configName);
    }
    catch (IOException ex ) {
        configData =  null;
    }
    if (configData == null) {
        configData =  new HashMap();
    }

    Map<String, Boolean> data = new HashMap();
    data.putAll( configData);
    if (sessionData == null) {
        data.putAll(sessionData);
    } else {
        for (String act : data.keySet()) {
            data.put(act, false);
        }
    }

    return  data;
  }

  public static Map<String, Boolean> getActions(String configName, String sessionKey)
  {
    return getActions(configName, sessionKey, false);
  }

  public static Map<String, Boolean> getActions(String configName)
  {
    return getActions(configName, "actions" , false);
  }
  
  public static Map<String, Boolean> getActions()
  {
    return getActions("default" , "actions" , false);
  }
  
  /**
   * 检查是否有指定动作的权限
   * @param act 动作URI
   * @param configName 配置名称
   * @param sessionKey 会话键名
   * @param checkLogin 检查登录
   * @return 通过为true, 反之为false
   */
  public static boolean checkAction(String act, String configName, String sessionKey, boolean checkLogin)
  {
    Map<String, Map<String, Boolean>> sessionList = getCache(1);
    if (!sessionList.containsKey(sessionKey))
    {
      return !checkLogin;
    }

    Map<String, Boolean> sessionData = sessionList.get(sessionKey);
    if (sessionData == null)
    {
      return !checkLogin;
    }

    Map<String, Map<String, Boolean>> configList = getCache(0);
    if (!configList.containsKey(configName))
    {
      return true;
    }

    Map<String, Boolean> configData = configList.get(configName);
    if (configData == null)
    {
      if (sessionData.containsKey(act) && !sessionData.get(act))
      {
        return false;
      }
    }
    else
    {
      if (configData.containsKey(act))
      {
        if (!sessionData.containsKey(act) || !sessionData.get(act))
        {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * 检查是否有指定动作的权限(不检查登录)
   * @param act 动作URI
   * @param configName 配置名称
   * @param sessionKey 检查登录
   * @return 通过为true, 反之为false
   */
  public static boolean checkAction(String act, String configName, String sessionKey)
  {
    return ActionFilter.checkAction(act, configName, sessionKey, false);
  }

  /**
   * 检查是否有指定动作的权限(不检查登录, sessionKey取actions)
   * @param act 动作URI
   * @param configName 配置名称
   * @return 通过为true, 反之为false
   */
  public static boolean checkAction(String act, String configName)
  {
    return ActionFilter.checkAction(act, configName, "actions", false);
  }

  /**
   * 检查是否有指定动作的权限(不检查登录, sessionKey取actions, configName取default)
   * @param act 动作名
   * @return 通过为true, 反之为false
   */
  public static boolean checkAction(String act)
  {
    return ActionFilter.checkAction(act, "default", "actions", false);
  }

}
