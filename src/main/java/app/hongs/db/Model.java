package app.hongs.db;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsException;
import app.hongs.util.Util;
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
 * 扩展动作方法: getPage,save,exists,unique 基础动作方法: getList,getInfo,create,update,delete
 * 通常它们被动作类直接调用; 基础模型方法: get,add,put,del 一般改写只需覆盖它们即可;
 * getFilter,setFilter 分别用于获取和更改数据等常规操作进行过滤,
 * setFilter 默认调用 getFilter 来实现的, 可覆盖它来做资源过滤操作.<br/>
 * 可使用查询参数:
 * <code>
 * ?f1=123&-f2=456&find=a+b&sort=-f1+f2&page=1&rows=10&cols[]=id&cols[]=f1&cols[]=f2
 * </code>
 * 详见 getFilter 方法说明
 * </p>
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
 影响getPage/getList/getFilter
   */
  protected String pageKey = "page";

  /**
   * 行数参数名
 影响getPage/getList/getFilter
   */
  protected String rowsKey = "rows";

  /**
   * 字段参数名
 影响getPage/getList/getFilter
   */
  protected String colsKey = "cols";

  /**
   * 排序参数名
 影响getPage/getList/getFilter
   */
  protected String sortKey = "ob";

  /**
   * 搜索参数名
 影响getPage/getList/getFilter
   */
  protected String findKey = "wd";

  /**
   * 被搜索的字段
 影响getPage/getList/getFilter
   */
  protected String[] findCols = new String[] {"name"};

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
    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    this.pageKey = conf.getProperty("fore.model.page.key", "pn");
    this.rowsKey = conf.getProperty("fore.model.rows.key", "rn");
    this.colsKey = conf.getProperty("fore.model.cols.key", "cs");
    this.sortKey = conf.getProperty("fore.model.sort.key", "ob");
    this.findKey = conf.getProperty("fore.model.find.key", "wd");
  }

  //** 扩展动作方法 **/
  
  /**
   * 获取分页
   *
   * 为空则page.errno为1, 页码超出则page.errno为2
   *
   * @param req
   * @param caze
   * @return 单页列表
   * @throws app.hongs.HongsException
   */
  public Map getPage(Map req, FetchCase caze)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (caze == null)
    {
      caze = new FetchCase();
    }

    caze.setOption("MODEL_METHOD", "getPage");
    this.getFilter(req, caze);

    // 获取页码, 默认为第一页
    int page = 0;
    if (req.containsKey(this.pageKey))
    {
      page = Integer.parseInt((String)req.get(this.pageKey));
    }

    // 获取行数, 默认从配置读取
    int rows = 0;
    if (req.containsKey(this.rowsKey))
    {
      rows = Integer.parseInt((String)req.get(this.rowsKey));
    }

    // 构建分页对象
    FetchPage fp = new FetchPage(this.table, caze);
    fp.setPage(page);
    fp.setRows(rows);
    List list = fp.getList();
    Map  info = fp.getPage();

    // 组织返回数据
    Map data = new HashMap();
    data.put("list" , list );
    data.put("page" , info );

    return data;
  }

  /**
   * 获取分页(无查询结构)
   *
   * 为空则page.errno为1, 页码超出则page.errno为2
   *
   * 含分页信息
   *
   * @param req
   * @return 单页列表
   * @throws app.hongs.HongsException
   */
  public Map getPage(Map req)
    throws HongsException
  {
    return this.getPage(req, null);
  }

  /**
   * 检查是否存在
   *
   * @param req
   * @param caze
   * @return 存在为true, 反之为false
   * @throws app.hongs.HongsException
   */
  public boolean exists(Map req, FetchCase caze)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (caze == null)
    {
      caze = new FetchCase();
    }

    // 默认不关联
    if (!caze.hasOption("ASSOC_TABLES")
    &&  !caze.hasOption("ASSOC_TYPES")
    &&  !caze.hasOption("ASSOC_JOINS"))
    {
      caze.setOption("ASSOC_TABLES", new HashSet());
    }

    // 是否缺少n或v参数
    if (!req.containsKey("n") || !req.containsKey("v"))
    {
      throw new HongsException(0x10aa, "Param n or v can not be empty");
    }

    String n = (String) req.get("n");
    String v = (String) req.get("v");

    Map columns = this.table.getColumns();

    // 是否缺少n对应的字段
    if (!columns.containsKey(n))
    {
      throw new HongsException(0x10ab, "Column " + n + " is not exists");
    }

    caze.where(".`"+n+"` = ?", v);

    Iterator it = req.entrySet( ).iterator();
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
   * @param req
   * @return 存在为true, 反之为false
   * @throws app.hongs.HongsException
   */
  public boolean exists(Map req)
    throws HongsException
  {
    return  exists(req, null);
  }

  public boolean unique(Map req, FetchCase caze)
    throws HongsException
  {
    return !exists(req, caze);
  }

  public boolean unique(Map req)
    throws HongsException
  {
    return !exists(req);
  }

  /**
   * 添加/修改记录
   *
   * @param req
   * @return 记录ID
   * @throws app.hongs.HongsException
   */
  public String save(Map req)
    throws HongsException
  {
    String id = (String)req.get(this.table.primaryKey);
    if (id == null || id.length() == 0)
      id = this.add(req);
    else
      this.put(id , req);
    return id;
  }

  //** 标准动作方法 **/

  /**
   * 获取列表
   *
   * @param req
   * @param caze
   * @return 全部列表
   * @throws app.hongs.HongsException
   */
  public Map getList(Map req, FetchCase caze)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (caze == null)
    {
      caze = new FetchCase();
    }

    caze.setOption("MODEL_METHOD", "getList");
    this.getFilter(req, caze);

    List list = this.table.fetchMore(caze);
    Map  data = new HashMap();
    data.put( "list" , list );
    return data;
  }

  /**
   * 获取列表(无查询结构)
   *
   * @param req
   * @return 全部列表
   * @throws app.hongs.HongsException
   */
  public Map getList(Map req)
    throws HongsException
  {
    return this.getList(req, null);
  }

  /**
   * 获取信息(调用get)
   *
   * @param req
   * @param caze
   * @return 记录信息
   * @throws app.hongs.HongsException
   */
  public Map getInfo(Map req, FetchCase caze)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (caze == null)
    {
      caze = new FetchCase();
    }

    caze.setOption("MODEL_METHOD", "getInfo");
    this.getFilter(req, caze);

    Map  info = this.table.fetchLess(caze);
    Map  data = new HashMap();
    data.put( "info" , info );
    return data;
  }

  /**
   * 获取信息(无查询结构)
   *
   * @param req
   * @return 记录信息
   * @throws app.hongs.HongsException
   */
  public Map getInfo(Map req)
    throws HongsException
  {
    return this.getInfo(req, null);
  }

  /**
   * 创建记录
   *
   * @param req
   * @return 记录ID
   * @throws HongsException 
   */
  public String create(Map req)
    throws HongsException
  {
    return this.add(req);
  }

  /**
   * 更新记录
   *
   * @param req
   * @param caze
   * @return 更新条数
   * @throws app.hongs.HongsException
   */
  public int update(Map req, FetchCase caze)
    throws HongsException
  {
    Object idz = req.get(this.table.primaryKey);
    if (idz == null) {
        return this.put(null, null);
    }

    Set<String> ids = new LinkedHashSet();
    if (idz instanceof Collection ) {
        ids.addAll((Collection)idz);
    } else {
        ids.add   ( idz.toString());
    }

    Map  dat  =  new  HashMap  (   req  );
    req.remove(  this.table.primaryKey  );

    for (String id : ids )
    {
      this.put( id , dat );
    }

    return ids.size();
  }

  /**
   * 更新记录
   *
   * @param req
   * @return 更新条数
   * @throws app.hongs.HongsException
   */
  public int update(Map req)
    throws HongsException
  {
    return this.update(req, null);
  }

  /**
   * 删除记录
   *
   * @param req
   * @param caze
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int delete(Map req, FetchCase caze)
    throws HongsException
  {
    Object idz = req.get(this.table.primaryKey);
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
   * @param req
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int delete(Map req)
    throws HongsException
  {
    return this.delete(req, null);
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
    String id = (String)data.get(this.table.primaryKey);
    if (id != null && id.length() != 0)
    {
      throw new HongsException(0x10a1, "Add can not have a id");
    }
    
    id = Core.getUniqueId();
    data.put(this.table.primaryKey, id);

    // 存入主数据
    int an = this.table.insert ( data );

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
    if (!this.setFilter("put", id, caze))
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
    if (!this.setFilter("del", id, caze))
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

  //** 辅助过滤方法 **/

  protected boolean setFilter(String mm, String id, FetchCase caze)
    throws HongsException
  {
    caze.setOption("MODEL_METHOD", mm  );
    caze.setOption("ASSOC_TABLES", new HashSet( ));
    caze.setSelect(".`"+this.table.primaryKey+"`")
            .where(".`"+this.table.primaryKey+"`=?", id);
    this.getFilter(new HashMap(), caze);
    return ! this.table.fetchLess(caze).isEmpty( );
  }
  
  /**
   * "获取"过滤
   *
   * <pre>
   * 作用于getPage,getList上
   *
   * 如需添加过滤条件, 请重写此方法;
   * 注意: 此处需要类似引用参数, 故调用前请务必实例化req和caze;
   * 默认仅关联join类型为LEFT,INNER和link类型为BLS_TO,HAS_ONE的表,
   * 如需指定关联方式请设置FetchCase的option: ASSOC_JOINS, ASSOC_TYEPS,
   * 如需指定关联的表请设置FetchCase的option: ASSOC_TABLES
   *
   * 设计目标:
   * 1) 按照cols参数设置查询字段;
   * 2) 按照sort参数设置排序方式,
   *    多个字段排序: sort=a+b+c或sort=-a+b+c, -表示该字段逆序;
   * 3) 按照find参数设置模糊查询,
   *    多关键词搜索: find=x+y+z或-find=x+y+z, -表示排除式搜索;
   *    指定字段搜索: find.a=x或find.a.b=y, 同样适用上面的规则,
   *    a.b为搜索关联表, 但需注意: a,a.b必须在findKeys中有指定;
   * 4) 如果有字段名相同的参数则获取与之对应的记录,
   *    可使用-field=xxx表示排除条件;
   * 5) 如果有子表.字段名相同的参数则获取与之对应的记录,
   *    可是有-table.field=xxx表示排除条件;
   * 注: "+"在URL中表示空格; 以上设计目录均已实现; 以上1/2/3中的参数名可统一设置或单独指定;
   * </pre>
   *
   * @param req
   * @param caze
   * @throws app.hongs.HongsException
   */
  protected void getFilter(Map req, FetchCase caze)
    throws HongsException
  {
    if (!"getInfo".equals(caze.getOption("MODEL_METHOD")))
    {
      // 默认仅关联类型为BLS_TO,HAS_ONE的表(仅能关联一个)
      if (caze.getOption("ASSOC_TYPES") == null)
      {
        Set types = new HashSet();
        types.add("BLS_TO" );
        types.add("HAS_ONE");
        caze.setOption("ASSOC_TYPES", types);
      }

      // 默认仅连接类型为LEFT,INNER的表(必须满足左表)
      if (caze.getOption("ASSOC_JOINS") == null)
      {
        Set types = new HashSet();
        types.add( "LEFT"  );
        types.add( "INNER" );
        caze.setOption("ASSOC_JOINS", types);
      }
    }

    if (req.isEmpty())
    {
      return;
    }

    Map columns = this.table
                   .getColumns();
    Iterator it = req.entrySet()
                     .iterator();

    /**
     * 依据设计规则, 解析请求参数, 转为查询结构
     */
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
        this.colsFilter(value, columns, caze);
        continue;
      }

      // 排序
      if (key.equals(this.sortKey))
      {
        this.sortFilter(value, columns, caze);
        continue;
      }

      /**
       * 用"-"来表示排除条件
       */
      boolean  not = key.startsWith("-");
      if (not) key = key.substring ( 1 );

      // 搜索
      if (key.equals(this.findKey))
      {
        this.findFilter(this.findCols, value, not, caze);
        continue;
      }

      // 当前表字段
      if (columns.containsKey(key))
      {
        this.mkeyFilter(key, value, not, caze);
        continue;
      }

      // 关联表字段
      if (value instanceof Map)
      {
        this.skeyFilter(key, value, not, caze);
        continue;
      }
    }
  }

  /**
   * 字段过滤(被caseFileter调用)
   * 根据请求的字段设置查询及判断需要关联的表
   * @param value
   * @param columns
   * @param caze
   * @throws HongsException
   */
  protected void colsFilter(Object value, Map columns, FetchCase caze)
    throws HongsException
  {
    List<String> cols;
    if (caze.hasSelect())
    {
      return;
    }
    else if (value instanceof List)
    {
      cols = (List<String>)value;
    }
    else if (value instanceof String)
    {
      cols = Arrays.asList(((String)value).split("[ \\+]"));
    }
    else
    {
      return;
    }
    if (cols.isEmpty())
    {
      return;
    }

    Set<String> tns = (Set<String>)caze.getOption("ASSOC_TABLES");
    if (tns == null)
    {
        tns =  new HashSet();
        caze.setOption("ASSOC_TABLES", tns);
    }

    for (String col : cols)
    {
      int pos = col.indexOf(".");
      if (pos == -1)
      {
        if (! columns.containsKey(col))
        {
          continue;
        }

        caze.select( ".`" + col + "`" );
      }
      else
      {
        String tn = col.substring(0,  pos);
        String fn = col.substring(pos + 1);

        Map   tc;
        Map   cs;
        Table tb;
        List<String> ts;
        tc = this.table.getAssoc(tn);
        if (tc == null) continue;
        tb = this.db.getTable(Table.getAssocName(tc));
        ts = Table.getAssocPath(tc);
        cs = tb.getColumns();
        if (! cs.containsKey( fn )) continue;

        tns.add(tn); tns.addAll(ts);
        FetchCase caze2 = caze.join(ts).join(tn);
        caze2.select(".`" +fn+ "`");
      }
    }
  }

  /**
   * 排序过滤(被caseFileter调用)
   * 如果字段有前缀“-”则该字段为逆序
   * @param value
   * @param columns
   * @param caze
   * @throws HongsException
   */
  protected void sortFilter(Object value, Map columns, FetchCase caze)
    throws HongsException
  {
    List<String> cols;
    if (caze.hasOrderBy())
    {
      return;
    }
    else if (value instanceof List)
    {
      cols = (List<String>)value;
    }
    else if (value instanceof String)
    {
      cols = Arrays.asList(((String)value).split("[ \\+]"));
    }
    else
    {
      return;
    }
    if (cols.isEmpty())
    {
      return;
    }

    Set<String> tns = (Set<String>)caze.getOption("ASSOC_TABLES");
    if (tns == null)
    {
        tns =  new HashSet();
    }

    for (String col : cols)
    {
      boolean   des = col.startsWith("-");
      if (des)  col = col.substring ( 1 );

      int pos = col.indexOf('.');
      if (pos == -1)
      {
        if (!columns.containsKey(col))
        {
          continue;
        }

        caze.orderBy(col +(des?" DESC":""));
      }
      else
      {
        String tn  = col.substring(0 , pos);
        String fn  = col.substring(pos + 1);

        Map   tc;
        Map   cs;
        Table tb;
        List<String> ts;
        tc = this.table.getAssoc(tn);
        if (tc == null) continue;
        tb = this.db.getTable(Table.getAssocName(tc));
        ts = Table.getAssocPath(tc);
        cs = tb.getColumns();
        if (!cs.containsKey( fn )) continue;

        tns.add(tn);
        tns.addAll(ts);
        caze.join(ts).join(tn);
        caze.orderBy(col +(des?" DESC":""));
      }
    }
  }

  /**
   * 关联表字段过滤
   * @param key
   * @param val
   * @param not
   * @param caze
   * @throws app.hongs.HongsException
   */
  protected void skeyFilter(String key, Object val, boolean not, FetchCase caze)
  throws HongsException
  {
    Set<String> tns = (Set<String>)caze.getOption("ASSOC_TABLES");
    if (tns == null)
    {
        tns = new HashSet();
        caze.setOption("ASSOC_TABLES", tns);
    }

    Map tc = this.table.getAssoc(key);
    if (tc == null) return;
    List<String> ts = Table.getAssocPath(tc);
    String       tn = Table.getAssocName(tc);
    Table        tb =  this.db.getTable (tn);
    Map cs = tb.getColumns();

    Map<String, Object> vs = (Map) val;
    for (Map.Entry et2 : vs.entrySet( ))
    {
      String key2 = (String)et2.getKey();
      Object val2 = et2.getValue();

      if (! cs.containsKey(key2))
          continue;

      if (val2 instanceof String)
      {
        String id = (String)val2;
        if (!"".equals(id))
        {
          tns.add( key ); tns.addAll( ts );
          FetchCase caze2 = caze.join(ts ).join( key );
          caze2.where(".`"+key2+(not?"` != ?":"` = ?"), id);
        }
      }
      else
      if (val2 instanceof List)
      {
        List<String> ids = (List)val2;
        if (!ids.isEmpty())
        {
          tns.add( key ); tns.addAll( ts );
          FetchCase caze2 = caze.join(ts ).join( key );
          caze2.where(".`"+key2+(not?"` NOT IN (?)":"` IN (?)"), ids);
        }
      }
    }
  }

  /**
   * 当前表字段过滤
   * @param key
   * @param val
   * @param not
   * @param caze
   */
  protected void mkeyFilter(String key, Object val, boolean not, FetchCase caze)
  {
    if (val instanceof String)
    {
      String id = (String)val;
      if (!"".equals(id))
        caze.where(".`"+key+(not?"` != ?":"` = ?"), id);
    }
    else
    if (val instanceof List)
    {
      List<String> ids = (List)val;
      if (!ids.isEmpty())
        caze.where(".`"+key+(not?"` NOT IN (?)":"` IN (?)"), ids);
    }
  }

  /**
   * 搜索过滤(被caseFileter调用)
   * @param keys
   * @param val
   * @param not
   * @param caze
   */
  protected void findFilter(String[] keys, Object val, boolean not, FetchCase caze)
  {
    // 由于 getFilter 取的是 Object
    // 不会直接调用上面的函数
    // 故需要进行强制类型转换
    if ( val instanceof Map)
    {
      findFilter(keys, (Map)val, not, caze);
    }

    List<String> vals;
    if (keys  ==  null)
    {
      return;
    }
    else if (val instanceof List)
    {
      vals = (List<String>)val;
    }
    else if (val instanceof String)
    {
      vals = Arrays.asList(((String)val).split("[ \\s]+"));
    }
    else
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
      find = Util.escape( find, "%_", "/" );
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

        caze.where(key + (not?" NOT LIKE ?":" LIKE ?") + " ESCAPE '/'", find);
      }
    }
  }

  /**
   * 搜索过滤(被caseFileter调用)
   * 针对具体搜索字段
   * @param keys
   * @param val
   * @param not
   * @param caze
   */
  protected void findFilter(String[] keys, Map val, boolean not, FetchCase caze)
  {
    List ks = Arrays.asList(this.findCols);
    Map  m1 = val;
    for (Object o1 : m1.entrySet()) {
        Map.Entry e1 = (Map.Entry) o1;
        Object v1 = e1.getValue();
        String k1 = e1.getKey().toString();

        if (v1 instanceof Map) {
            Map m2 = (Map) v1;
            for (Object o2 : m2.entrySet()) {
                Map.Entry e2 = (Map.Entry) o2;
                String v2 = e2.getValue().toString();
                String k2 = k1 + "." + e2.getKey().toString();

                if (ks.contains(k2)) {
                    this.findFilter(new String[]{k2}, v2, not, caze);
                }
            }
        } else {
                if (ks.contains(k1)) {
                    this.findFilter(new String[]{k1}, v1, not, caze);
                }
        }
    }
  }

}
