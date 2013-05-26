package app.hongs.action;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h1>抽象Filter类</h1>
 * <pre>
 * 如果在过滤器中需要使用框架提供的方法, 需要从这继承.
 * </pre>
 *
 * @author Hongs
 */
public abstract class AbstractFilter
  extends AbstractServlet
  implements Filter
{

  @Override
  public void init(FilterConfig config)
    throws ServletException
  {
    super.init(config.getServletContext());
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
    throws ServletException, IOException
  {
    try
    {
      super._actInit((HttpServletRequest)req, (HttpServletResponse)rsp);
      this._actFilter(req, rsp, chain);
    }
    finally
    {
      super._actDestroy();
    }
  }

  /**
   * 执行过滤
   * 注意: 需要建立自己的filter请覆盖该方法, 而不是doFilter方法
   * @param req
   * @param rsp
   * @param chain
   * @throws ServletException
   * @throws IOException
   */
  public abstract void _actFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
    throws ServletException, IOException;

  /**
   * 执行服务
   * 注意: 该方法在此处被取消使用, 覆盖该方法不会被调用
   * @param req
   * @param rsp
   * @throws ServletException
   * @throws IOException
   */
  @Override
  public void _actService(HttpServletRequest req, HttpServletResponse rsp)
    throws ServletException, IOException
  {
    throw new UnsupportedOperationException("Not supported, use _actFilter.");
  }

  @Override
  public void destroy()
  {
    super.destroy();
  }

}
