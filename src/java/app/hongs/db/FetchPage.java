package app.hongs.db;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 分页查询
 * 
 * <p>
 * 注意: 必须先调 getList 然后调 getPage
 * </p>
 *
 * <h3>配置选项:</h3>
 * <pre>
 * core.first.of.page 首页序号, 0或1
 * core.rows.per.page 每页多少条记录
 * </pre>
 *
 * @author Hongs
 */
public class FetchPage
{

  private DB db;

  private Table table;

  private FetchCase caze;

  private int page;

  private int rows;

  private Map info;

  public FetchPage(DB db, FetchCase caze)
  {
    this.db    = db;
    this.caze  = caze;
    this.table = null;

    Object page2 = caze.getOption("page");
    if (page2 != null && page2.equals(""))
    {
      this.page = Integer.parseInt(page2.toString());
    }

    Object rows2 = caze.getOption("rows");
    if (rows2 != null && page2.equals(""))
    {
      this.rows = Integer.parseInt(rows2.toString());
    }
  }

  public FetchPage(Table table, FetchCase caze)
  {
    this(table.db, caze);
    this.table  =  table;

    this.caze.from(table.tableName, table.name);
  }

  public void setPage(int page)
  {
    this.page = page;
  }

  public void setRows(int rows)
  {
    this.rows = rows;
  }

  public List getList()
    throws HongsException
  {
    this.info = new HashMap();

    if (this.page == 0)
    {
      CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
      this.page = conf.getProperty("core.first.of.page", 1 );
    }
    if (this.rows == 0)
    {
      CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
      this.rows = conf.getProperty("core.rows.per.page", 10);
    }

    /**
     * 如果页码等于0, 则将其设为1
     * 如果页码小于0, 则逆向计算其真实页码
     */
    if (this.page == 0)
    {
      this.page = 1 ;
    }
    else
    if (this.page <  0)
    {
      this.getInfo();
      int pc = (Integer)this.info.get("pagecount");
      int pn = this.page + 1;
      while (pn < 0)
      {
        pn = pn + pc;
      }
      this.page = pn;
    }

    this.info.put("page", this.page);
    this.info.put("rows", this.rows);

    // 查询列表
    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    int pn = page - conf.getProperty("core.first.of.page", 1);
    caze.limit(pn * rows, rows);
    List list;
    if (null != this.table)
    {
      list = this.table.fetchMore(caze);
    }
    else
    {
      list = this.db.fetchMore(caze);
    }

    // 获取真实行数
    if (list.isEmpty())
    {
      if (this.page == 1)
      {
        this.info.put("errno", 1); // 列表为空
      }
      else
      {
        this.info.put("errno", 2); // 页码超出
      }
    }
    else
    {
      this.info.put("errno", 0);
    }

    return list;
  }

  public Map getInfo()
    throws HongsException
  {
    if (this.info.containsKey("errno") && (Integer)this.info.get("errno") == 1)
    {
      this.info.put("rowscount", 0);
      this.info.put("pagecount", 0);
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
      this.info.put("pageocunt", pc);
    }

    return this.info;
  }

}
