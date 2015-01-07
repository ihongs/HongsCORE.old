package app.hongs.serv;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
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

/**
 * 动作过滤器
 *
 * <h3>初始化参数(init-param):</h3>
 * <pre>
 * auth-conf    动作配置(默认为default)
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
  private String authConf;

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
    xp = config.getInitParameter("auth-conf");
    if (xp == null)
    {
      xp = "default";
    }
    this.authConf = xp;

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

    /**
     * 从配置中提取首页
     */
    AuthConfig conf;
    try {
        conf = AuthConfig.getInstance(authConf);
    }
    catch (HongsException ex) {
        throw new ServletException(ex);
    }
    catch (HongsError ex) {
        throw new ServletException(ex);
    }
    if (conf.pages.size() == 1) {
        this.indexPage = conf.pages.keySet().toArray(new String[0])[0];
    }
  }

  @Override
  public void destroy()
  {
    authConf    = null;
    indexPage   = null;
    loginPage   = null;
    excludeUris = null;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
    throws IOException, ServletException
  {
    DO:do {

    String act = Core.ACTION_NAME.get(  );

    /**
     * 依次校验是否是需要排除的URL
     */
    if (excludeUris != null) {
        for (String url : excludeUris[0]) {
            if (act.equals(url)) {
                break DO;
            }
        }
        for (String url : excludeUris[1]) {
            if (act.startsWith(url)) {
                break DO;
            }
        }
        for (String url : excludeUris[2]) {
            if (act.  endsWith(url)) {
                break DO;
            }
        }
    }

    AuthConfig conf;
    try {
        conf = AuthConfig.getInstance(authConf);
    }
    catch (HongsException ex) {
        throw new ServletException(ex);
    }
    catch (HongsError ex) {
        throw new ServletException(ex);
    }

    Boolean perm  =  conf.chkAuth(act);
    if (perm == null ) {
        sayError(0);
        return;
    }
    if (perm == false) {
        sayError(1);
        return;
    }

    } while (false);

    chain.doFilter(req, rsp);
  }

  private void sayError(int type)
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
            rsp.put("oh", "403");
            rsp.put("ah",  msg );
        if (uri != null && uri.length() != 0) {
            rsp.put("to",  uri );
        }
        helper.reply(rsp);
    }
    else {
        if (uri != null && uri.length() != 0) {
            helper.redirect(uri);
        }
        else {
            helper.error403(msg);
        }
    }
  }

}
