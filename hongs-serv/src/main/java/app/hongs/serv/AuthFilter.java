package app.hongs.serv;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.action.ActionHelper;
import app.hongs.action.serv.ServWarder;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
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
 * conf-name    配置名称
 * sess-name    会话名称(默认为roles)
 * index-uri    首页地址(为空则不跳转)
 * login-uri    登录地址(为空则不跳转)
 * exclude-uris 不包含的URL, 可用","分割多个, 可用"*"为前后缀
 * </pre>
 *
 * @author Hongs
 */
public class AuthFilter
  implements Filter
{

  /**
   * 动作配置
   */
  private Auth auth;

  /**
   * 角色会话
   */
  private String sessName;

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
  private String[][] excludeUris;

  @Override
  public void init(FilterConfig config)
    throws ServletException
  {
    String xp;

    /**
     * 获取权限配置名
     */
    xp = config.getInitParameter("conf-name");
    this.auth = xp != null ? Auth.getInstance() : Auth.getInstance(xp);

    /**
     * 获取首页URL
     */
    xp = config.getInitParameter("sess-name");
    if (xp != null)
    {
      this.sessName = "roles";
    }

    /**
     * 获取首页URL
     */
    xp = config.getInitParameter("index-uri");
    if (xp != null)
    {
      this.indexPage = Core.BASE_HREF +"/"+ xp;
    }

    /**
     * 获取登录URL
     */
    xp = config.getInitParameter("login-uri");
    if (xp != null)
    {
      this.loginPage = Core.BASE_HREF +"/"+ xp;
    }

    /**
     * 获取不包含的URL
     */
    xp = config.getInitParameter("exclude-uris");
    if (xp != null)
    {
      Set<String> cu = new HashSet();
      Set<String> su = new HashSet();
      Set<String> eu = new HashSet();
      for (String u : xp.split(","))
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
      this.excludeUris = u3;
    }
  }

  @Override
  public void destroy()
  {
    indexPage   = null;
    loginPage   = null;
    excludeUris = null;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
    throws IOException, ServletException
  {
    DO:do {

    String act = ServWarder.getCurrPath((HttpServletRequest) req);

    /**
     * 依次校验是否是需要排除的URL
     */
    if (excludeUris != null) {
        for (String url : excludeUris[1]) {
            if (act.startsWith(url)) {
                break DO;
            }
        }
        for (String url : excludeUris[2]) {
            if (act.endsWith(url)) {
                break DO;
            }
        }
        for (String url : excludeUris[0]) {
            if (act.equals(url)) {
                break DO;
            }
        }
    }

    ActionHelper hlpr = Core.getInstance(ActionHelper.class);
    Set sess  = Synt.declare(hlpr.getSessValue(sessName), Set.class);
    if (sess == null) {
        doFailed(0);
        return;
    }
    Boolean perm = auth.checkAuth(act, sess);
    if (perm != true) {
        doFailed(1);
        return;
    }

    } while (false);

    chain.doFilter(req, rsp);
  }

  private void doFailed(int type)
  {
    ActionHelper helper = Core.getInstance(ActionHelper.class);
    CoreLanguage lang   = Core.getInstance(CoreLanguage.class);

    /**
     * 根据错误类型获取错误消息及跳转URI
     */
    String msg;
    String uri;
    if (type == 0)
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
    String src = helper.getRequest().getRequestURI( );
    String qry = helper.getRequest().getQueryString();
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
    if (helper.getRequest().getRequestURI().endsWith(".act")) {
        Map rsp = new HashMap( );
            rsp.put("ok", false);
            rsp.put("err", "Er403");
            rsp.put("msg",  msg);
        if (uri != null  && uri.length() != 0) {
            rsp.put("goto", uri);
        }
        helper.reply(rsp);
    }
    else {
        if (uri != null  && uri.length() != 0) {
            helper.redirect(uri);
        }
        else {
            helper.error403(msg);
        }
    }
  }

}
