package app.hongs.action;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

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
import app.hongs.HongsError;
import app.hongs.HongsException;

/**
 * <h1>标准动作过滤器</h1>
 *
 * <h2>初始化参数(init-param):</h2>
 * <pre>
 * index-path   首页地址(为空则不跳转)
 * login-path   登录地址(为空则不跳转)
 * config-name  动作配置(默认为default)
 * exclude-urls 不包含的URL
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
  private String indexPage;

  /**
   * 登录路径
   */
  private String loginPage;

  /**
   * 动作配置
   */
  private String configName;

  /**
   * 不包含的URL
   */
  private String excludeUrls[][];

  @Override
  public void init(FilterConfig config)
    throws ServletException
  {
    /**
     * 获取首页URL
     */
    String ip = config.getInitParameter("index-page");
    if (ip != null)
    {
      this.indexPage = Core.BASE_HREF + ip;
    }

    /**
     * 获取登录URL
     */
    String lp = config.getInitParameter("login-page");
    if (lp != null)
    {
      this.loginPage = Core.BASE_HREF + lp;
    }

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
     * 获取不包含的URL
     */
    String xu = config.getInitParameter("exclude-urls");
    if (xu != null)
    {
      Set<String> cu = new HashSet();
      Set<String> su = new HashSet();
      Set<String> eu = new HashSet();
      for (String u : xu.split(","))
      {
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

    /**
     * 从配置中提取首页
     */
    ActionConfig conf;
    try {
        conf = ActionConfig.getInstance(configName);
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
    indexPage   = null;
    loginPage   = null;
    configName  = null;
    excludeUrls = null;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
    throws IOException, ServletException
  {
    DO:do {

    String act = Core.ACTION_PATH.get(  );

    /**
     * 依次校验是否是需要排除的URL
     */
    if (excludeUrls != null) {
        for (String url : excludeUrls[0]) {
            if (act.equals(url)) {
                break DO;
            }
        }
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
    }

    ActionConfig conf;
    try {
        conf = ActionConfig.getInstance(configName);
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
    ActionHelper helper = (ActionHelper)Core.getInstance(ActionHelper.class);
    CoreLanguage lang   = (CoreLanguage)Core.getInstance(CoreLanguage.class);

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

}
