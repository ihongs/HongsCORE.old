package app.hongs.action;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.util.Tree;
import app.hongs.action.annotation.DatumsWrapper;

/**
 * <h1>请求数据过滤器</h1>
 *
 * @author Hongs
 */
public class DatumsFilter
  implements Filter
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
    /**
     * 获取配置名
     */
    String cn = config.getInitParameter("config-name");
    if (cn == null)
    {
      cn = "default";
    }
    this.configName = cn;

    /**
     * 获取请求键
     */
    String sk = config.getInitParameter("request-key");
    if (sk == null)
    {
      sk = "datkeys";
    }
    this.requestKey = sk;
  }

  @Override
  public void destroy()
  {
    configName = null;
    requestKey = null;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
    throws ServletException, IOException
  {
    ActionHelper helper = (ActionHelper)
      Core.getInstance(app.hongs.action.ActionHelper.class);

    DatumsConfig ac;
    try
    {
      ac = DatumsConfig.getInstance(this.configName);
    }
    catch (HongsException ex)
    {
      helper.print500Code(ex.getMessage());
      return;
    }

    Map map = new HashMap();
    Map dat = new HashMap();

    String[] keys = req.getParameterValues(this.requestKey);
    if (null  !=  keys)
    {
      for (String key : keys)
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
      dat.putAll(ac.getDataByRsp(uri));
    }
    if (ref.startsWith(Core.BASE_HREF))
    {
      ref = ref.substring(Core.BASE_HREF.length());
      map.putAll(ac.getDataByRef(ref));
    }

    if (!map.isEmpty())
    {
      helper.getRequestData( );
      Tree.putAllDeep(helper.requestData, map);
    }

    if ( dat.isEmpty())
    {
      chain.doFilter(req, rsp);
      return;
    }

    /** 输出过滤 **/

    HttpServletResponse rsp2;
    DatumsWrapper rsp3;
    rsp2 = helper.response;
    rsp3 = new DatumsWrapper(rsp2);
    helper.response = rsp3;
    chain.doFilter(req, rsp3);
    helper.response = rsp2;

    Map data = helper.getResponseData( );
    if (data != null
    && (data.get("__success__") == null
    || (boolean) data.get("__success__") == true))
    {
      Tree.putAllDeep (data,dat);
      helper.printJSON(  data  );
    }
    else
    {
      helper.print(rsp3.toString());
    }
  }

}
