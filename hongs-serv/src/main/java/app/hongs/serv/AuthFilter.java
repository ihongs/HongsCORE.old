package app.hongs.serv;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.MenuSet;
import app.hongs.action.ActionDriver;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 动作过滤器
 *
 * <h3>初始化参数(init-param):</h3>
 * <pre>
 * config       配置名称
 * index-page   首页地址(为空则不跳转)
 * login-page   登录地址(为空则不跳转)
 * exclude-urls 不包含的URL, 可用","分割多个, 可用"*"为前后缀
 * </pre>
 *
 * @author Hongs
 */
public class AuthFilter
  extends  ActionDriver
{

  /**
   * 动作配置
   */
  private MenuSet siteMap;

  /**
   * 首页路径
   */
  private String indexPage;

  /**
   * 登录路径
   */
  private String loginPage;

  /**
   * 不包含的URL
   */
  private String[][] excludeUrls;

  @Override
  public void init(FilterConfig config)
    throws ServletException
  {
    super.init(config);

    String s;

    /**
     * 获取权限配置名
     */
    s = config.getInitParameter("config");
    try
    {
      this.siteMap = s == null ? MenuSet.getInstance() : MenuSet.getInstance(s);
    }
    catch (HongsException ex)
    {
      throw new ServletException(ex);
    }

    /**
     * 获取首页URL
     */
    s = config.getInitParameter("index-page");
    if (s != null)
    {
      this.indexPage = Core.BASE_HREF + s;
    }

    /**
     * 获取登录URL
     */
    s = config.getInitParameter("login-page");
    if (s != null)
    {
      this.loginPage = Core.BASE_HREF + s;
    }

    /**
     * 获取不包含的URL
     */
    s = config.getInitParameter("exclude-urls");
    if (s != null)
    {
      Set<String> cu = new HashSet();
      Set<String> su = new HashSet();
      Set<String> eu = new HashSet();
      for (String  u : s.split(";"))
      {
        u = u.trim();
        if (u.endsWith("*")) {
            su.add(u.substring( 0, u.length() - 2));
        } else if(u.startsWith("*")) {
            eu.add(u.substring( 1 ));
        } else {
            cu.add(u);
        }
      }
      String u3[][] = {
          cu.toArray(new String[0]),
          su.toArray(new String[0]),
          eu.toArray(new String[0])
      };
      this.excludeUrls = u3;
    }
  }

  @Override
  public void destroy()
  {
    super.destroy();

    siteMap     = null;
    indexPage   = null;
    loginPage   = null;
    excludeUrls = null;
  }

  @Override
  public void doFilter(Core core, ActionHelper helper, FilterChain chain)
    throws IOException, ServletException
  {
    ServletRequest  req = helper.getRequest( );
    ServletResponse rsp = helper.getResponse();
    String act = ActionDriver.getCurrPath((HttpServletRequest) req);

    DO:do {

    /**
     * 依次校验是否是需要排除的URL
     */
    if (excludeUrls != null) {
        for (String url : excludeUrls[1]) {
            if (act.startsWith(url)) {
                break DO;
            }
        }
        for (String url : excludeUrls[2]) {
            if (act.endsWith(url)) {
                break DO;
            }
        }
        for (String url : excludeUrls[0]) {
            if (act.equals(url)) {
                break DO;
            }
        }
    }

    Set<String> authset = siteMap.getAuthSet();
    if (null == authset) {
        if (null != loginPage) {
            doFailed((short)1);
            return;
        }
        if (siteMap.actions.contains(act)) {
            doFailed((short)3);
            return;
        }
    } else {
        if (siteMap.actions.contains(act) && !authset.contains(act)) {
            doFailed((short)3);
            return;
        }
    }

    } while(false);

    chain.doFilter(req, rsp);
  }

  private void doFailed(short type)
  {
    ActionHelper hlpr = Core.getInstance(ActionHelper.class);
    CoreLanguage lang = Core.getInstance(CoreLanguage.class);

    /**
     * 根据错误类型获取错误消息及跳转URI
     */
    String msg;
    String uri;
    if (1== type)
    {
      msg = lang.translate("core.error.no.login");
      uri = this.loginPage;
    }
    else
    {
      msg = lang.translate("core.error.no.power");
      uri = this.indexPage;
    }

    /**
     * 追加来源URI
     */
    String src = hlpr.getRequest().getRequestURI( );
    String qry = hlpr.getRequest().getQueryString();
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

      if (!uri.contains("?"))
      {
        uri += "?r=" + src;
      }
      else
      {
        uri += "&r=" + src;
      }
    }

    /**
     * 修改(2013/02/22):
     * 如果处于AJAX环境, 则是由JSON传递URL和消息
     * 否则使用HTTP错误代码
     */
    if (hlpr.getRequest().getRequestURI().endsWith(".act")) {
        Map rsp = new HashMap( );
            rsp.put("ok", false);
            rsp.put("err", "Er40" + type);
            rsp.put("msg",  msg);
        if (uri != null  && uri.length( ) != 0) {
            rsp.put("goto", uri);
        }
        hlpr.reply(rsp);
    }
    else {
        if (uri != null  && uri.length() != 0) {
            hlpr.redirect(uri);
        }
        else {
            hlpr.error403(msg);
        }
    }
  }

}
