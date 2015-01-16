package app.hongs.db;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsException;
import app.hongs.util.Text;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基础模型
 *
 * <p>
 * 当要使用 save(add,put),create(add),update(put),delete(del) 时请确保表有配置主键.<br/>
 * 基础动作方法: getList,getInfo,create,update,delete 扩展动作方法: exists,unique
 * 通常它们被动作类直接调用; 基础模型方法: get,add,put,del 一般改写只需覆盖它们即可;
 * filter, permit 分别用于获取和更改数据等常规操作时进行过滤,
 * permit 默认调用 filter 来实现的, 可覆盖它来做资源过滤操作.<br/>
 * 可使用查询参数:
 * <code>
 * ?f1=123&f2.-gt=456&wd=a+b&ob=-f1+f2&pn=1&rs=10&cs=id+f1+f2
 * </code>
 详见 filter 方法说明
 </p>
 *
 * <h3>异常代码:</h3>
 * <pre>
 * 区间: 0x10a0~0x10af
 * 0x10a1 创建时不得含有id
 * 0x10a2 更新时必须含有id
 * 0x10a3 删除时必须含有id
 * 0x10a4 获取时必须含有id
 * 0x10a6 无权更新该资源
 * 0x10a7 无权删除该资源
 * 0x10a8 无权获取该资源
 * 0x10aa 参数n和v不能为空(检查存在)
 * 0x10ab 指定的字段不存在(检查存在)
 * 0x10ac 不支持的运算符: $0
 * </pre>
 *
 * @author Hongs
 */
public class Model
{

  /**
   * 所属库对象
   */
  public DB db;

  /**
   * 所属表对象
   */
  public Table table;

  /**
   * 页码参数名
   * 影响getPage
   */
  protected String pageKey = "pn";

  /**
   * 行数参数名
   * 影响getPage
   */
  protected String rowsKey = "rn";

  /**
   * 字段参数名
   * 影响getPage/getList/filter
   */
  protected String colsKey = "cs";

  /**
   * 排序参数名
   * 影响getPage/getList/filter
   */
  protected String sortKey = "ob";

  /**
   * 搜索参数名
   * 影响getPage/getList/filter
   */
  protected String findKey = "wd";

  /**
   * 被搜索的字段
   * 影响getPage/getList/filter
   */
  public String[] findCols = new String[] {"name"};

  /**
   * 构造方法
   *
   * 需指定该模型对应的表对象.
   * 如传递的page,rows,cols,sort,find等参数名不同,
   * 可在构造时分别指定;
   * 请指定被搜索的字段.
   *
   * @param table
   * @throws app.hongs.HongsException
   */
  public Model(Table table)
    throws HongsException
  {
    this.db = table.db;
    this.table = table;

    // 配置
    CoreConfig conf = Core.getInstance(CoreConfig.class);
    this.pageKey = conf.getProperty("fore.page.key", "pn");
    this.rowsKey = conf.getProperty("fore.rows.key", "rn");
    this.colsKey = conf.getProperty("fore.cols.key", "cs");
    this.sortKey = conf.getProperty("fore.sort.key", "ob");
    this.findKey = conf.getProperty("fore.find.key", "wd");
  }

  //** 标准动作方法 **/

  /**
   * 获取分页
   *
   * 为空则page.errno为1, 页码超出则page.errno为2
   * 页码等于 0 则不要列表数据
   * 行数小于 0 则不要分页信息
   * 行数等于 0 则不要使用分页
   * 页码小于 0 则逆向倒数获取
   *
   * @param rd
   * @param caze
   * @return 单页列表
   * @throws app.hongs.HongsException
   */
  public Map getList(Map rd, FetchCase caze)
    throws HongsException
  {
    if (rd == null)
    {
      rd = new HashMap();
    }
    if (caze == null)
    {
      caze = new FetchCase();
    }

    caze.setOption("MODEL_METHOD", "getList");
    this.filter(caze, rd);

    // 获取页码, 默认为第一页
    int page = 1;
    if (rd.containsKey(this.pageKey))
    {
      page = Integer.parseInt((String)rd.get(this.pageKey));
    }

    // 获取行数, 默认依从配置
    int rows;
    if (rd.containsKey(this.rowsKey))
    {
      rows = Integer.parseInt((String)rd.get(this.rowsKey));
    }
    else
    {
      rows = CoreConfig.getInstance().getProperty("fore.rows.per.page", 10);
    }

    Map data = new HashMap();

    if (rows != 0)
    {
      caze.from (table.tableName , table.name);
      FetchPage fp = new FetchPage(caze, table);
      fp.setPage(page != 0 ? page : 1);
      fp.setRows(rows >  0 ? rows : Math.abs(rows));

      // 页码等于 0 则不要列表数据
      if (page != 0 )
      {
        List list = fp.getList();
        data.put( "list", list );
      }

      // 行数小于 0 则不要分页信息
      if (rows >  0 )
      {
        Map  info = fp.getPage();
        data.put( "page", info );
      }
    }
    else
    {
      // 行数等于 0 则不要使用分页
        List list = table.fetchMore(caze);
        data.put( "list", list );
    }

    return data;
  }

  /**
   * 获取分页(无查询结构)
   *
   * 为空则page.errno为1, 页码超出则page.errno为2
   *
   * 含分页信息
   *
   * @param rd
   * @return 单页列表
   * @throws app.hongs.HongsException
   */
  public Map getList(Map rd)
    throws HongsException
  {
    return this.getList(rd, null);
  }

  /**
   * 获取信息(调用get)
   *
   * @param rd
   * @param caze
   * @return 记录信息
   * @throws app.hongs.HongsException
   */
  public Map getInfo(Map rd, FetchCase caze)
    throws HongsException
  {
    String id = (String)rd.get(this.table.primaryKey);
    Map info = this.get(id, caze);
    Map data = new HashMap();
    data.put( "info", info );
    return data;
  }

  /**
   * 获取信息(无查询结构)
   *
   * @param rd
   * @return 记录信息
   * @throws app.hongs.HongsException
   */
  public Map getInfo(Map rd)
    throws HongsException
  {
    return this.getInfo(rd, null);
  }

  /**
   * 创建记录
   *
   * @param rd
   * @return 记录ID
   * @throws HongsException
   */
  public String create(Map rd)
    throws HongsException
  {
    return this.add(rd);
  }

  /**
   * 更新记录
   *
   * @param rd
   * @param caze
   * @return 更新条数
   * @throws app.hongs.HongsException
   */
  public int update(Map rd, FetchCase caze)
    throws HongsException
  {
    Object idz = rd.get(this.table.primaryKey);
    if (idz == null) {
        return this.put(null, null);
    }

    Set<String> ids = new LinkedHashSet();
    if (idz instanceof Collection ) {
        ids.addAll((Collection)idz);
    } else {
        ids.add   ( idz.toString());
    }

    Map  dat  =  new  HashMap  (   rd  );
    rd.remove(  this.table.primaryKey  );

    for (String id : ids )
    {
      this.put( id , dat );
    }

    return ids.size();
  }

  /**
   * 更新记录
   *
   * @param rd
   * @return 更新条数
   * @throws app.hongs.HongsException
   */
  public int update(Map rd)
    throws HongsException
  {
    return this.update(rd, null);
  }

  /**
   * 删除记录
   *
   * @param rd
   * @param caze
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int delete(Map rd, FetchCase caze)
    throws HongsException
  {
    Object idz = rd.get(this.table.primaryKey);
    if (idz == null) {
        return this.del(null, null);
    }

    Set<String> ids = new LinkedHashSet();
    if (idz instanceof Collection ) {
        ids.addAll((Collection)idz);
    } else {
        ids.add   ( idz.toString());
    }

    for (String id : ids )
    {
      this.del( id  );
    }

    return ids.size();
  }

  /**
   * 删除记录
   *
   * @param rd
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int delete(Map rd)
    throws HongsException
  {
    return this.delete(rd, null);
  }

  //** 扩展动作方法 **/

  /**
   * 检查是否存在
   *
   * @param rd
   * @param caze
   * @return 存在为true, 反之为false
   * @throws app.hongs.HongsException
   */
  public boolean exists(Map rd, FetchCase caze)
    throws HongsException
  {
    if (rd == null)
    {
      rd = new HashMap();
    }
    if (caze == null)
    {
      caze = new FetchCase();
    }

    // 默认不关联
    if (!caze.hasOption("ASSOCS")
    &&  !caze.hasOption("ASSOC_TYPES")
    &&  !caze.hasOption("ASSOC_JOINS"))
    {
      caze.setOption("ASSOCS", new HashSet());
    }

    // 是否缺少n或v参数
    if (!rd.containsKey("n") || !rd.containsKey("v"))
    {
      throw new HongsException(0x10aa, "Param n or v can not be empty");
    }

    String n = (String) rd.get("n");
    String v = (String) rd.get("v");

    Map columns = this.table.getColumns();

    // 是否缺少n对应的字段
    if (!columns.containsKey(n))
    {
      throw new HongsException(0x10ab, "Column " + n + " is not exists");
    }

    caze.where(".`"+n+"` = ?", v);

    Iterator it = rd.entrySet( ).iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry)it.next();
      String field = (String) entry.getKey();
      String value = (String) entry.getValue( );

      if (columns.containsKey(field))
      {
        if (field.equals(this.table.primaryKey))
        {
          caze.where(".`"+ this.table.primaryKey+"` != ?", value);
        }
        else
        {
          caze.where(".`"+field+"` = ?", value);
        }
      }
    }

    Map row = this.table.fetchLess(caze);
    return !row.isEmpty();
  }

  /**
   * 检查是否存在
   *
   * @param rd
   * @return 存在为true, 反之为false
   * @throws app.hongs.HongsException
   */
  public boolean exists(Map rd)
    throws HongsException
  {
    return  exists(rd, null);
  }

  public boolean unique(Map rd, FetchCase caze)
    throws HongsException
  {
    return !exists(rd, caze);
  }

  public boolean unique(Map rd)
    throws HongsException
  {
    return !exists(rd);
  }

  //** 标准模型方法 **/

  /**
   * 添加记录
   *
   * @param data
   * @return 记录ID
   * @throws app.hongs.HongsException
   */
  public String add(Map<String, Object> data)
    throws HongsException
  {
    String id = (String) data.get(this.table.primaryKey);
    if (id != null && id.length() != 0)
    {
      throw new HongsException(0x10a1, "Add can not have a id");
    }

    id = Core.getUniqueId();
    data.put(this.table.primaryKey, id);

    // 存入主数据
    /**  **/ this.table.insert ( data );

    // 存入子数据
    this.table.insertSubValues ( data );

    return id;
  }

  /**
   * 更新记录
   *
   * @param id
   * @param caze
   * @param data
   * @return 更新条数
   * @throws app.hongs.HongsException
   */
  public int put(String id, FetchCase caze, Map<String, Object> data)
    throws HongsException
  {
    if (id == null || id.length() == 0)
    {
      throw new HongsException(0x10a2, "ID can not be empty for put");
    }

    // 调用setFileter进行校验
    caze = caze != null ? caze.clone() : new FetchCase();
    caze.setOption("MODEL_METHOD", "put");
    if (!this.permit(caze, id))
    {
      throw new HongsException(0x10a6, "Can not put the resource for id '"+id+"'");
    }

    // 更新主数据
    data.remove(this.table.primaryKey );
    int an = this.table.update ( data , this.table.primaryKey + " = ?", id);

    // 更新子数据
    data.put(this.table.primaryKey, id);
    this.table.insertSubValues ( data );

    return an;
  }

  /**
   * 更新记录
   *
   * @param id
   * @param data
   * @return 更新条数
   * @throws app.hongs.HongsException
   */
  public int put(String id, Map<String, Object> data)
    throws HongsException
  {
    return this.put(id, null, data);
  }

  /**
   * 删除指定记录
   *
   * 为避免逻辑复杂混乱
   * 如需重写删除方法,
   * 请总是重写该方法.
   *
   * @param id
   * @param caze
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int del(String id, FetchCase caze)
    throws HongsException
  {
    if (id == null || id.length() == 0)
    {
      throw new HongsException(0x10a3, "ID can not be empty for del");
    }

    // 调用setFileter进行校验
    caze = caze != null ? caze.clone() : new FetchCase();
    caze.setOption("MODEL_METHOD", "del");
    if (!this.permit(caze, id))
    {
      throw new HongsException(0x10a7, "Can not del the resource for id '"+id+"'");
    }

    // 删除子数据
    this.table.deleteSubValues(id);

    // 删除主数据
    int an = this.table.delete("`"+this.table.primaryKey+"` = ?", id);

    return an;
  }

  /**
   * 删除指定记录
   *
   * @param id
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int del(String id)
    throws HongsException
  {
    return this.del(id, null);
  }

  public Map get(String id, FetchCase caze)
    throws HongsException
  {
    if (id == null || id.length() == 0)
    {
      throw new HongsException(0x10a3, "ID can not be empty for del");
    }

    // 调用filter进行过滤
    caze = caze != null ? caze.clone() : new FetchCase();
    caze.setOption("MODEL_METHOD", "get");
    Map rd = new HashMap();
        rd.put (table.primaryKey ,  id  );
    this.filter(caze , rd);

    return this.table.fetchLess(caze);
  }

  public Map get(String id)
    throws HongsException
  {
    return this.get(id, null);
  }

  public List getAll(Map rd, FetchCase caze)
    throws HongsException
  {
    // 调用filter进行过滤
    caze = caze != null ? caze.clone() : new FetchCase();
    caze.setOption("MODEL_METHOD", "getAll");
    this.filter(caze , rd);

    return this.table.fetchMore(caze);
  }

  public List getAll(Map rd)
    throws HongsException
  {
    return this.getAll(rd, null);
  }

  //** 辅助过滤方法 **/

  /**
   * "获取"过滤
   *
   * <pre>
   * 作用于getPage,getList上
   *
   * 如需添加过滤条件, 请重写此方法;
   * 注意: 此处需要类似引用参数, 故调用前请务必实例化req和caze;
   * 默认仅关联join类型为LEFT,INNER和link类型为BLS_TO,HAS_ONE的表,
   * 如需指定关联的表请设置FetchCase的option: ASSOCS,
   * 如需指定关联方式请设置FetchCase的option: ASSOC_JOINS, ASSOC_TYEPS
   *
   * 设计目标:
   * 1) 按照cols参数设置查询字段;
   *    限定字段列表: cols=a+b+c或cols=-a+x.b, -表示排除该字段;
   * 2) 按照sort参数设置排序方式,
   *    多个字段排序: sort=a+b+c或sort=-a+b+c, -表示该字段逆序;
   * 3) 按照find参数设置模糊查询,
   *    多关键词搜索: find=x+y+z;
   *    指定字段搜索: find.a=x或find.a.b=y, 同样适用上面的规则,
   *    a.b为搜索关联表, 但需注意: a,a.b必须在findKeys中有指定;
   * 4) 如果有字段名相同的参数则获取与之对应的记录,
   *    可以在字段名后跟.加上gt,lt,ge,le,ne分布表示&gt;,&lt;,&ge;,&le;,&ne;
   * 5) 如果有子表.字段名相同的参数则获取与之对应的记录,
   *    可以在子表.字段名后跟.加上gt,lt,ge,le,ne分布表示&gt;,&lt;,&ge;,&le;,&ne;
   * 注: "+"在URL中表示空格; 以上设计目录均已实现; 以上1/2/3中的参数名可统一设置或单独指定;
   * </pre>
   *
   * @param caze
   * @param rd
   * @throws app.hongs.HongsException
   */
  protected void filter(FetchCase caze, Map rd)
    throws HongsException
  {
    /**
     * 如果不是 getInfo 且没指定关联类型
     * 默认只关联 BLS_TO,HAS_ONE 的表(仅能关联一个)
     * 默认只连接 LEFT  ,INNER   的表(必须满足左表)
     */
    if (!"get".equals(caze.getOption("MODEL_METHOD")) && !"-".equals(rd.get("cx")))
    {
      if (caze.getOption("ASSOC_TYPES") == null)
      {
        Set types = new HashSet();
        types.add("BLS_TO" );
        types.add("HAS_ONE");
        caze.setOption("ASSOC_TYPES", types);
      }

      if (caze.getOption("ASSOC_JOINS") == null)
      {
        Set types = new HashSet();
        types.add( "LEFT"  );
        types.add( "INNER" );
        caze.setOption("ASSOC_JOINS", types);
      }
    }

    if (rd.isEmpty())
    {
      return;
    }

    /**
     * 依据设计规则, 解析请求参数, 转为查询结构
     */
    Iterator it = rd.entrySet().iterator();
    Map columns = this.table.getColumns();
    while (it.hasNext())
    {
      Map.Entry et = (Map.Entry)it.next();
      String key = (String)et.getKey();
      Object value = et.getValue();

      if (key == null || value == null
      ||  key.equals(this.rowsKey)
      ||  key.equals(this.pageKey))
      {
        continue;
      }

      // 字段
      if (key.equals(this.colsKey))
      {
        this.colsFilter(caze, value, columns);
        continue;
      }

      // 排序
      if (key.equals(this.sortKey))
      {
        this.sortFilter(caze, value, columns);
        continue;
      }

      // 搜索
      if (key.equals(this.findKey))
      {
        this.findFilter(caze, value, findCols);
        continue;
      }

      // 当前表字段
      if (columns.containsKey(key))
      {
        this.mkeyFilter(caze, value, key);
        continue;
      }

      // 关联表字段
      if (value instanceof Map)
      {
        this.skeyFilter(caze, value, key);
        continue;
      }
    }
  }

  /**
   * "变更"过滤
   * @param id 主键值
   * @param caze 条件
   * @return
   * @throws HongsException
   */
  protected boolean permit(FetchCase caze, String id)
    throws HongsException
  {
    if (!caze.hasOption("ASSOCS") && caze.joinList.isEmpty())
    {
      caze.setOption("ASSOCS", new HashSet());
    }
    caze.setSelect(".`"+this.table.primaryKey+"`")
            .where(".`"+this.table.primaryKey+"`=?", id);
    this.filter(caze, new HashMap());
    return ! this.table.fetchLess(caze).isEmpty( );
  }

  /**
   * 字段过滤(被caseFileter调用)
   * 根据请求的字段设置查询及判断需要关联的表
   * @param caze
   * @param val
   * @param columns
   * @throws HongsException
   */
  protected void colsFilter(FetchCase caze, Object val, Map columns)
    throws HongsException
  {
    if (caze.hasSelect( ))
    {
      return;
    }

    Set<String> cols;
    if (val instanceof String)
    {
      cols = new HashSet(Arrays.asList(((String)val).split("[ ,\\+]")));
    } else
    if (val instanceof Collection)
    {
      cols = new HashSet((Collection)val);
    } else
    {
      return;
    }
    if (cols.isEmpty())
    {
      return;
    }

    Set<String> tns = (Set<String>)caze.getOption("ASSOCS");
    if (tns == null)
    {
        tns  = new HashSet();
        caze.setOption("ASSOCS", tns);
    }

    Map<String, Set<String>> colsBuf = new HashMap();

    for (String col : cols)
    {
      col  = col.trim();
      String tbl  =  "";

      int pos  = col.indexOf(".");
      if (pos != -1)
      {
        tbl = col.substring(0 , pos);
        col = col.substring(pos + 1);
      }

      Set set  = colsBuf.get(tbl);
      if (set == null)
      {
        set = new HashSet(  );
        colsBuf.put(tbl, set);
      }
      set.add(col);
    }

    for ( Map.Entry  et : colsBuf.entrySet())
    {
      Map          cols2;
      FetchCase    caze2;
      Set<String>  colz = (Set) et.getValue();
      String         tn = (String)et.getKey();

      if (!"".equals(tn))
      {
        Map          tc;
        Table        tb;
        String       tx;
        String[]     ts;

        tc =this.table.getAssoc(tn);
        if (tc == null)
        {
          continue;
        }

        tx = Table.getAssocName(tc);
        ts = Table.getAssocPath(tc);
        tb =  this.db.getTable (tx);
        cols2 =  tb.getColumns (  );
        tns.addAll(new HashSet(Arrays.asList(ts)));
        tns.add(tn);
        caze2 = caze.gotJoin(ts).join(tn);
      }
      else
      {
        cols2 = columns;
        caze2 = caze;
      }

      cols = new HashSet();

      for (String col : colz)
      {
        boolean mnu = col.startsWith("-");
        if(mnu) col = col.substring ( 1 );

        if (! cols2.containsKey( col ))
        {
          continue;
        }

        if (mnu)
        {
          if (cols.isEmpty())
          {
            cols.addAll(cols2.keySet());
          }
          cols.remove(col);
        }
        else
        {
          cols.add   (col);
        }
      }

      for (String col : cols)
      {
        caze2.select(".`" + col + "`" );
      }
    }
  }

  /**
   * 排序过滤(被caseFileter调用)
   * 如果字段有前缀“-”则该字段为逆序
   * @param caze
   * @param val
   * @param columns
   * @throws HongsException
   */
  protected void sortFilter(FetchCase caze, Object val, Map columns)
    throws HongsException
  {
    if (caze.hasOrderBy())
    {
      return;
    }

    Set<String> cols;
    if (val instanceof String)
    {
      cols = new HashSet(Arrays.asList(((String)val).split("[ ,\\+]")));
    } else
    if (val instanceof Collection)
    {
      cols = new HashSet((Collection)val);
    } else
    {
      return;
    }
    if (cols.isEmpty())
    {
      return;
    }

    Set<String> tns = (Set<String>)caze.getOption("ASSOCS");
    if (tns == null)
    {
        tns  = new HashSet();
        caze.setOption("ASSOCS", tns);
    }

    for (String col : cols)
    {
      col = col.trim();
      boolean mnu = col.startsWith("-");
      if(mnu) col = col.substring ( 1 );

      int pos = col.indexOf(".");
      if (pos == -1)
      {
        if (! columns.containsKey(col))
        {
          continue;
        }

        caze.orderBy(".`" + col + "`" + (mnu ? " DESC" : ""));
      }
      else
      {
        String tn = col.substring(0,  pos);
        String fn = col.substring(pos + 1);

        Map          tc;
        Map          cs;
        Table        tb;
        String       tx;
        String[]     ts;

        tc =this.table.getAssoc(tn);
        if (tc == null)
        {
          continue;
        }

        tx = Table.getAssocName(tc);
        tb =  this.db.getTable (tx);
        cs =  tb.getColumns (  );
        if (! cs.containsKey(fn))
        {
          continue;
        }

        ts = Table.getAssocPath(tc);
        tns.addAll(new HashSet(Arrays.asList(ts)));
        tns.add(tn);
        FetchCase cace = caze.gotJoin(ts).join(tn);

        cace.orderBy(".`" + col + "`" + (mnu ? " DESC" : ""));
      }
    }
  }

  /**
   * 搜索过滤(被caseFileter调用)
   * @param caze
   * @param val
   * @param keys
   */
  protected void findFilter(FetchCase caze, Object val, String[] keys)
  {
    if (keys == null || keys.length == 0)
    {
      return;
    }

    /**
     * 也可指定只匹配其中一个可搜索字段
     */
    if (val instanceof Map)
    {
        List ks = Arrays.asList(keys);
        Map  m1 = (Map) val;
        for (Object o1 : m1.entrySet()) {
            Map.Entry e1 = (Map.Entry) o1;
            Object    v1 = e1.getValue( );
            String    k1 = e1.getKey().toString();

            if (v1 instanceof Map) {
                Map m2 = (Map) v1;
                for (Object o2 : m2.entrySet()) {
                    Map.Entry e2 = (Map.Entry) o2;
                    String    v2 = e2.getValue( ).toString();
                    String    k2 = k1 + "." + e2.getKey().toString();

                    if (ks.contains(k2)) {
                        this.findFilter(caze, v2, new String[]{k2});
                    }
                }
            } else {
                    if (ks.contains(k1)) {
                        this.findFilter(caze, v1, new String[]{k1});
                    }
            }
        }
        return;
    }

    Set<String> vals;
    if (val instanceof String)
    {
      vals = new HashSet(Arrays.asList(((String)val).split("\\s+")));
    } else
    if (val instanceof Collection)
    {
      vals = new HashSet((Collection)val);
    } else
    {
      return;
    }
    if (vals.isEmpty())
    {
      return;
    }

    for (String find : vals)
    {
      /**
       * 符号"%_^[]"在SQL LIKE中有特殊意义,
       * 需要对这些符号进行转义;
       * 前后加"%"用于模糊匹配.
       */
      find = Text.escape( find, "%_", "/" );
      find = "%" + find + "%";

      for (String key : keys)
      {
        if (key.indexOf('.') != -1 )
        {
          String[] b = key.split("\\.", 2 );
          key = "`"+ b[0] +"`.`"+ b[1] +"`";
        }
        else
        {
          key = ".`" + key + "`";
        }

        caze.where(key + " LIKE ? ESCAPE '/'", find);
      }
    }
  }

  /**
   * 当前表字段过滤
   * @param caze
   * @param val
   * @param key
   * @throws HongsException
   */
  protected void mkeyFilter(FetchCase caze, Object val, String key)
    throws HongsException
  {
    if (val instanceof Map)
    {
      Map map = (Map) val;
      Set set = map.keySet();
      if (map.containsKey("-lt"))
      {
        set.remove("-lt");
        Object vaz = map.get("-lt");
        caze.where(".`"+key+"` < ?", vaz);
      }
      if (map.containsKey("-le"))
      {
        set.remove("-le");
        Object vaz = map.get("-le");
        caze.where(".`"+key+"` <= ?", vaz);
      }
      if (map.containsKey("-gt"))
      {
        set.remove("-gt");
        Object vaz = map.get("-gt");
        caze.where(".`"+key+"` > ?", vaz);
      }
      if (map.containsKey("-ge"))
      {
        set.remove("-ge");
        Object vaz = map.get("-ge");
        caze.where(".`"+key+"` >= ?", vaz);
      }
      if (map.containsKey("-eq"))
      {
        set.remove("-eq");
        Object vaz = map.get("-eq");
        caze.where(".`"+key+"` = ?", vaz);
      }
      if (map.containsKey("-ne"))
      {
        set.remove("-ne");
        Object vaz = map.get("-ne");
        caze.where(".`"+key+"` != ?", vaz);
      }
      if (map.containsKey("-in"))
      {
        set.remove("-eq");
        Object vaz = map.get("-eq");
        caze.where(".`"+key+"` IN (?)", vaz);
      }
      if (map.containsKey("-ni"))
      {
        set.remove("-ne");
        Object vaz = map.get("-ne");
        caze.where(".`"+key+"` NOT IN (?)", vaz);
      }
      if (!set.isEmpty())
      {
        String ss = set.toString();
        HongsException ex = new HongsException(0x10ac, "Unrecognized symbols: "+ss);
        ex.setLocalizedOptions(ss);
        throw ex;
      }
    } else
    if (val instanceof Collection)
    {
        Set nul = new HashSet(  ); nul.add( "" );
        Set col = new HashSet(( Collection )val);
        if (!col.equals(nul) && !col.isEmpty( ))
        {
            caze.where(".`"+key+"` IN (?)", val);
        }
    } else
    {
        if (!"".equals( val )  &&  null  != val)
        {
            caze.where(".`"+key + "` = ?" , val);
        }
    }
  }

  /**
   * 关联表字段过滤
   * @param caze
   * @param val
   * @param key
   * @throws app.hongs.HongsException
   */
  protected void skeyFilter(FetchCase caze, Object val, String key)
  throws HongsException
  {
    Set<String> tns = (Set<String>) caze.getOption("ASSOCS");
    if (tns == null)
    {
        tns  = new HashSet( ); caze.setOption("ASSOCS", tns);
    }

    Map tc = this.table.getAssoc(key);
    if (tc == null) return;
    String[]     ts = Table.getAssocPath(tc);
    String       tn = Table.getAssocName(tc);
    Table        tb =  this.db.getTable (tn);
    Map cs = tb.getColumns();

    Map<String, Object>  vs = (Map) val ;
    for (Map.Entry et2 : vs.entrySet( ))
    {
      String key2 = (String)et2.getKey();
      Object val2 = et2.getValue();

      if (cs.containsKey(key2))
      {
        if (val2 != null)
        {
          tns.addAll(new HashSet( Arrays.asList(ts)));
          tns.add(key);
          FetchCase cace = caze.gotJoin(ts).join(key);
          this.mkeyFilter( cace , val2 , key2 );
        }
      }
    }
  }

}