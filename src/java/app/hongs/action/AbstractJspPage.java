package app.hongs.action;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

/**
 * <h1>抽象JSP类</h1>
 * <pre>
 * 如果在JSP中需要使用框架提供的方法和标签, 需要从这继承.
 * </pre>
 *
 * @author Hongs
 * @deprecated 
 */
public class AbstractJspPage
  extends AbstractServlet
  implements HttpJspPage
{

  @Override
  public void init(ServletConfig config)
    throws ServletException
  {
    super.init(config);
  }

  @Override
  public void destroy()
  {
    super.destroy();
  }

  @Override
  public void jspInit()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void jspDestroy()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void _jspService(HttpServletRequest req, HttpServletResponse rsp)
    throws ServletException, IOException
  {
    rsp.getWriter().close();
  }

  @Override
  public void _actService(HttpServletRequest req, HttpServletResponse rsp)
    throws ServletException, IOException
  {
    this._jspService(req, rsp);
  }

}
