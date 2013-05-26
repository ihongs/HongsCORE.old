package app.hongs.action;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;

import app.hongs.Core;
import app.hongs.HongsException;

/**
 * <h1>请求数据过滤器</h1>
 *
 * @author Hongs
 */
public class DatumsFilter
  extends AbstractFilter
{

  /**
   * 配置名
   */
  private String configName;

  /**
   * 请求键
   */
  private String requestKey;

  @Override
  public void init(FilterConfig config)
    throws ServletException
  {
    super.init(config);


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
    String sk = config.getInitParameter("request-key");
    if (sk == null)
    {
      sk = "datkeys";
    }
    this.requestKey = sk;
  }

  @Override
  public void _actFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
    throws ServletException, IOException
  {
    ActionHelper helper = (ActionHelper)
      Core.getInstance("app.hongs.action.ActionHelper");

    DatumsConfig ac;
    try
    {
      ac = new DatumsConfig(this.configName);
    }
    catch (HongsException ex)
    {
      helper.print500Code(ex.getMessage());
      return;
    }

    Map map = new HashMap();

    String[] keys = req.getParameterValues(this.requestKey);
    if (null  !=  keys)
    {
      for (String key  :  keys)
      {
        Object obj = ac.getDataByKey(key);
        if (obj instanceof Map)
        {
          map.putAll((Map) obj);
        }
      }
    }

    String uri = helper.request.getRequestURI();
    String ref = helper.request.getHeader("Referer");

    // Referer will start with "http://www.xxx.com" or end with "?var=value"
    ref = ref != null ? ref.replaceAll("(^[^/]+://[^/]+|[\\?#].*$)", "") : "";

    if (uri.startsWith(Core.BASE_HREF))
    {
      uri = uri.substring(Core.BASE_HREF.length());
      map.putAll(ac.getDataByUri(uri));
    }
    if (ref.startsWith(Core.BASE_HREF))
    {
      ref = ref.substring(Core.BASE_HREF.length());
      map.putAll(ac.getDataByRef(ref));
    }

    if (!map.isEmpty())
    {
      helper.getRequestData();
      helper.requestData.putAll(map);
    }

    chain.doFilter(req, rsp);
  }

}
