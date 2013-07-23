package app.hongs.db;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsException;

/**
 * <h1>分页查询</h1>
 * <pre>
 * 注意: 必须先调 getList 然后调 getPage
 * </pre
 *
 * <h2>配置选项:</h2>
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

  private FetchBean fs;

  private int page;

  private int rows;

  private Map info;

  public FetchPage(DB db, FetchBean fs)
  {
    this.db    = db;
    this.fs    = fs;
    this.table = null;

    Object page2 = fs.getOption("page");
    if (page2 != null && page2.equals(""))
    {
      this.page = Integer.parseInt(page2.toString());
    }

    Object rows2 = fs.getOption("rows");
    if (rows2 != null && page2.equals(""))
    {
      this.rows = Integer.parseInt(rows2.toString());
    }
  }

  public FetchPage(Table table, FetchBean fs)
  {
    this(table.db, fs);
    this.table = table;

    this.fs.from(table.tableName, table.name);
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
      CoreConfig conf = (CoreConfig)Core.getInstance("app.hongs.CoreConfig");
      this.page = (int)conf.getProperty("core.first.of.page", 1);
    }
    if (this.rows == 0)
    {
      CoreConfig conf = (CoreConfig)Core.getInstance("app.hongs.CoreConfig");
      this.rows = (int)conf.getProperty("core.rows.per.page", 1);
    }

    /**
     * 如果页码等于0, 则将其设为1
     * 如果页码小于0, 则逆向计算其真实页码
     */
    if (this.page == 0)
    {
      this.page = 1;
    }
    else
    if (this.page < 0)
    {
      this.getInfo();
      int tp = (Integer)this.info.get("total_pages");
      int ap = this.page + 1;
      while (ap < 0)
      {
        ap += tp;
      }
    }

    this.info.put("page", this.page);
    this.info.put("rows", this.rows);

    // 查询列表
    fs.limit(rows * (page - 1), rows);
    List list;
    if (null != this.table)
    {
      list = this.table.fetchMore(fs);
    }
    else
    {
      list = this.db.fetchMore(fs);
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
      this.info.put("real_rows", 0);
    }
    else
    {
      this.info.put("errno", 0);
      this.info.put("real_rows", list.size());
    }

    return list;
  }

  public Map getInfo()
    throws HongsException
  {
    if (this.info.containsKey("errno") && (Integer)this.info.get("errno") == 1)
    {
      this.info.put("total_pages", 0);
      this.info.put("total_rows", 0);
      return this.info;
    }

    // 查询总行数
    String   sql;
    Object[] params;
    FetchBean      fs2 = this.fs.clone();
    for (FetchBean fs3 : fs2.joinList)
    {
      fs3.setSelect("");
    }
    fs2.limit(0);
    fs2.setOrderBy("");
    if (fs2.hasGroupBy( ))
    {
      sql    = "SELECT COUNT(*) AS __count__ FROM ("
             + fs2.getSQL( )+") AS __table__";
      params = fs2.getParams( );
    }
    else
    {
      fs2.setSelect(  "COUNT(*) AS __count__"  );
      sql    = fs2.getSQL(    );
      params = fs2.getParams( );
    }

    // 计算总行数及总页数
    Map row = this.db.fetchOne(sql, params);
    if (row.isEmpty() == false)
    {
      int tr = Integer.parseInt(row.get("__count__").toString());
      int tp = (int)Math.ceil((float)tr / this.rows);
      this.info.put("total_pages", tp);
      this.info.put("total_rows", tr);
    }

    return this.info;
  }

}
