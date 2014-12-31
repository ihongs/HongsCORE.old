package app.hongs.db;

import app.hongs.HongsException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 分页查询
 *
 * @author Hongs
 */
public class FetchPage
{

  private DB db;

  private FetchCase caze;

  private int page =  1 ;

  private int rows = 10 ;

  private Map info = new HashMap();

  public FetchPage(DB db, FetchCase caze) throws HongsException
  {
    this.db    = db;
    this.caze  = caze;

    Object page2 = caze.getOption("page");
    if (page2 != null && page2.equals(""))
    {
      this.setPage(Integer.parseInt(page2.toString()));
    }

    Object rows2 = caze.getOption("rows");
    if (rows2 != null && rows2.equals(""))
    {
      this.setRows(Integer.parseInt(rows2.toString()));
    }
  }

  public void setPage(int page) throws HongsException
  {
    if (page <  0)
    {
      this.getPage( );
      int pc = (Integer) this.info.get("pagecount");
      int pn = page + 1;
      while (pn <  0)
      {
        pn = pn + pc;
      }
      page = pn;
    } else
    if (page == 0)
    {
      page = 1 ;
    }
    this.page = page;
  }

  public void setRows(int rows) throws HongsException
  {
    if (rows == 0)
    {
      rows = 10;
    }
    this.rows = rows;
  }

  public List getList()
    throws HongsException
  {
    caze.limit((this.page - 1) * this.rows, this.rows);

    // 查询列表
    List list = this.db.fetchMore(caze);

    // 获取行数
    if (!list.isEmpty())
    {
      this.info.put("err", 0); // 没有异常
    } else
    if (this.page == 1 )
    {
      this.info.put("err", 1); // 列表为空
      this.info.put("pagecount", 0);
      this.info.put("rowscount", 0);
    }
    else
    {
      this.info.put("err", 2); // 页码超出
    }

    return list;
  }

  public Map getPage()
    throws HongsException
  {
    this.info.put("page", this.page);
    this.info.put("rows", this.rows);

    // 列表为空则不用计算了
    if (this.info.containsKey("pagecount"))
    {
      return this.info;
    }
    
    // 查询总行数
    String   sql;
    Object[] params;
    FetchCase      caze2 = this.caze.clone();
    for (FetchCase caze3 : caze2.joinList)
    {
      caze3.setSelect("");
    }
    caze2.limit(0);
    caze2.setOrderBy( "");
    if (caze2.hasGroupBy( ))
    {
      sql    =  "SELECT COUNT(*) AS __count__ FROM ("
             + caze2.getSQL()+") AS __table__" ;
      params = caze2.getParams();
    }
    else
    {
      caze2.setSelect( "COUNT(*) AS __count__");
      sql    = caze2.getSQL();
      params = caze2.getParams();
    }

    // 计算总行数及总页数
    Map row = this.db.fetchOne(sql, params);
    if (row.isEmpty() == false)
    {
      int rc = Integer.parseInt(row.get("__count__").toString());
      int pc = (int)Math.ceil((float)rc / this.rows);
      this.info.put("rowscount", rc);
      this.info.put("pagecount", pc);
    }

    return this.info;
  }

}
