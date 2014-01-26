package app.hongs.db;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.util.Str;
import app.hongs.HongsException;

/**
 * 基础模型
 * 
 * <p>
 * 当要使用 getInfo(get),save(add,put),remove(del) 时请确保表有配置主键.<br/>
 * getPage,getList,getInfo,save,update,remove,exists 为基础动作方法, 通常它们被动作类直接调用;
 * get,add,put,remove 为基础模型方法, 一般改写只需覆盖它们即可;
 * getFilter,idCheck 分别用于对获取和更改数据等常规操作进行过滤,
 * 其中 idCheck 默认是调用 getFilter 来实现的, 可覆盖它来做资源过滤操作.<br/>
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
 * 0x10a0 参数id不能为空(获取/删除)
 * 0x10a2 参数n,v不能为空(检查存在)
 * 0x10a4 指定字段不存在(检查存在)
 * 0x10a6 主键值不存在(修改)
 * 0x10a8 无权操作该资源
 * </pre>
 *
 * @author Hongs
 */
abstract public class AbstractBaseModel
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
   * id参数名
   * 影响getInfo/getList/save/update/remove/exists/getFilter
   */
  protected String idVar = "id";

  /**
   * 页码参数名
   * 影响getPage/getList/getFilter
   */
  protected String pageVar = "page";

  /**
   * 行数参数名
   * 影响getPage/getList/getFilter
   */
  protected String rowsVar = "rows";

  /**
   * 字段参数名
   * 影响getPage/getList/getFilter
   */
  protected String colsVar = "cols";

  /**
   * 排序参数名
   * 影响getPage/getList/getFilter
   */
  protected String sortVar = "sort";

  /**
   * 搜索参数名
   * 影响getPage/getList/getFilter
   */
  protected String findVar = "find";

  /**
   * 被搜索的字段
   * 影响getPage/getList/getFilter
   */
  protected String[] findKeys = new String[] {"name"};

  /**
   * 受影响的ID
   * 在save/update/remove后被设置为影响的行id
   */
  protected List<String> affectedIds;

  /**
   * 构造方法
   *
   * 需指定该模型对应的表对象.
   * 如传递的id/ids等参数名不同,
   * 可在构造时分别指定;
   * 请指定被搜索的字段.
   *
   * @param table
   */
  public AbstractBaseModel(Table table)
    throws HongsException
  {
    this.db = table.db;
    this.table = table;
    
    // 配置
    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    this.idVar   = conf.getProperty("js.model.id.var", "id");
    this.pageVar = conf.getProperty("js.model.page.var", "page");
    this.rowsVar = conf.getProperty("js.model.rows.var", "rows");
    this.colsVar = conf.getProperty("js.model.cols.var", "cols");
    this.sortVar = conf.getProperty("js.model.sort.var", "sort");
    this.findVar = conf.getProperty("js.model.find.var", "find");
  }
  public AbstractBaseModel(String tableName)
    throws HongsException
  {
    this("default", tableName);
  }
  public AbstractBaseModel(String dbName, String tableName)
    throws HongsException
  {
    this(DB.getInstance(dbName).getTable(tableName));
  }

  //** 标准动作方法 **/

  /**
   * 获取分页
   *
   * 为空则errno为1, 页码超出则errno为2
   *
   * @param req
   * @param more
   * @return 单页列表
   * @throws app.hongs.HongsException
   */
  public Map getPage(Map req, FetchMore more)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (more == null)
    {
      more = new FetchMore();
    }

    more.setOption("MODEL_METHOD", "getPage");
    this.getFilter(req, more);

    // 获取页码, 默认为第一页
    int page = 0;
    if (req.containsKey(this.pageVar))
    {
      page = Integer.parseInt((String)req.get(this.pageVar));
    }

    // 获取行数, 默认从配置读取
    int rows = 0;
    if (req.containsKey(this.rowsVar))
    {
      rows = Integer.parseInt((String)req.get(this.rowsVar));
    }

    // 构建分页对象
    FetchPage fp = new FetchPage(this.table, more);
    fp.setPage(page);
    fp.setRows(rows);
    List list = fp.getList();
    Map  info = fp.getInfo();
    int errno = (Integer)info.get("errno");

    // 组织返回数据
    Map data = new HashMap();
    data.put("list" , list );
    data.put("page" , info );
    data.put("errno", errno);

    return data;
  }

  /**
   * 获取分页(无查询结构)
   *
   * 为空则errno为1, 页码超出则errno为2
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
   * 获取列表
   *
   * 为空则errno为1
   *
   * @param req
   * @param more
   * @return 全部列表
   * @throws app.hongs.HongsException
   */
  public Map getList(Map req, FetchMore more)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (more == null)
    {
      more = new FetchMore();
    }

    more.setOption("MODEL_METHOD", "getList");
    this.getFilter(req, more);

    // 获取列表
    List list = this.table.fetchMore(more);
    int errno = list.isEmpty() ? 1 : 0;

    // 组织数据
    Map data = new HashMap();
    data.put("list" , list );
    data.put("errno", errno);

    return data;
  }

  /**
   * 获取列表(无查询结构)
   *
   * 为空则errno为1
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
   * 为空则errno为1
   *
   * @param req
   * @param more
   * @return 记录信息
   * @throws app.hongs.HongsException
   */
  public Map getInfo(Map req, FetchMore more)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (more == null)
    {
      more = new FetchMore();
    }

    String id = (String)req.get(this.idVar);

    Map info;
    if (id != null && id.length() != 0)
    {
      info = this.get(id, more);
    }
    else
    {
      info = new HashMap();
    }
    int errno = info.isEmpty() ? 1 : 0;

    Map data = new HashMap();
    data.put("info", info);
    data.put("errno", errno);

    return data;
  }

  /**
   * 获取信息(无查询结构)
   *
   * 为空则errno为1
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
    if (id == null || id.length() == 0 )
        id = (String)req.get(this.idVar);
    if (id == null || id.length() == 0 )
        id =  this.add(    req);
    else
        id =  this.put(id, req);

    // 记录为受影响的ID
    this.affectedIds = new ArrayList(  );
    this.affectedIds.add(id);

    return id;
  }

  /**
   * 更新记录
   *
   * @param req
   * @param more
   * @return 更新条数
   * @throws app.hongs.HongsException
   */
  public int update(Map req, FetchMore more)
    throws HongsException
  {
    List<String> ids = this.getOperableIds(req, more);
    if (ids.isEmpty()) this.put("", null);

    for (String id : ids )
    {
      this.put( id , req );
    }

    this.affectedIds = ids;
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
   * @param more
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int remove(Map req, FetchMore more)
    throws HongsException
  {
    List<String> ids = this.getOperableIds(req, more);
    if (ids.isEmpty()) this.del("", null);

    for (String id : ids )
    {
      this.del( id  );
    }

    this.affectedIds = ids;
    return ids.size();
  }

  /**
   * 删除记录
   *
   * @param req
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int remove(Map req)
    throws HongsException
  {
    return this.remove(req, null);
  }

  /**
   * 检查是否存在
   *
   * @param req
   * @param more
   * @return 存在为true, 反之为false
   * @throws app.hongs.HongsException
   */
  public boolean exists(Map req, FetchMore more)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (more == null)
    {
      more = new FetchMore();
    }

    if (!more.hasOption("ASSOC_TABLES")
    &&  !more.hasOption("ASSOC_TYPES")
    &&  !more.hasOption("ASSOC_JOINS"))
    {
      more.setOption("ASSOC_TABLES", new HashSet());
    }

    // 是否缺少n或v参数
    if (!req.containsKey("n") || !req.containsKey("v"))
    {
      throw new HongsException(0x10a2, "Param n or v can not be empty");
    }

    String n = (String) req.get("n");
    String v = (String) req.get("v");

    Map columns = this.table.getColumns();

    // 是否缺少n对应的字段
    if (!columns.containsKey(n))
    {
      throw new HongsException(0x10a4, "Column " + n + " is not exists");
    }

    more.where(".`"+n+"` = ?", v);

    Iterator it = req.entrySet( ).iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry)it.next();
      String field = (String) entry.getKey();
      String value = (String) entry.getValue( );

      if (columns.containsKey(field))
      {
        if (field.equals(this.idVar )
        ||  field.equals(this.table.primaryKey))
        {
          more.where(".`"+ this.table.primaryKey+"` != ?", value);
        }
        else
        {
          more.where(".`"+field+"` = ?", value);
        }
      }
    }

    Map row = this.table.fetchLess(more);
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

  public boolean unique(Map req, FetchMore more)
    throws HongsException
  {
    return !exists(req, more);
  }

  public boolean unique(Map req)
    throws HongsException
  {
    return !exists(req);
  }

  /**
   * 获取可操作的 ID
   * getOperableNames,update,remove 均是调用此方法获取 ID
   * @param req
   * @param more
   * @return IDs
   */
  protected List<String> getOperableIds(Map req, FetchMore more) throws HongsException {
    if (req == null)
    {
      req = new HashMap();
    }
    if (more == null)
    {
      more = new FetchMore();
    }

    List<String> ids = new ArrayList();
    if (req.containsKey(this.idVar)) {
      Object obj = req.get(this.idVar);
      if (obj instanceof List) {
        ids.addAll((List<String>) obj);
      }
      else {
        ids.add( obj.toString() );
      }
    }
    if (ids.isEmpty()) return ids;

    String pk = this.table.primaryKey;
    more = more.clone();
    more.setSelect(".`"+pk+"`").where(".`"+pk+"` IN (?)", ids);
    List<Map> rows = this.table.fetchMore(more);
    ids = new ArrayList();
    for (Map  row  : rows) {
      ids.add(row.get(pk).toString());
    }
    return ids;
  }
  /**
   * 获取可操作的名称
   * 同 getAffectedNames 一样, 用于对没有 dflag 的数据, 在 remove 前获取名称
   * 此方法逻辑与 update,remove 完全一致, 最终获取仍是调用 getAffetctedNames
   * 故如要重写获取名称的方法仅需重写 getAffectedNames 即可
   * @param req
   * @param more
   * @return 用", "连接的可操作的名称
   */
  public String getOperableNames(Map req, FetchMore more) throws HongsException {
    affectedIds = getOperableIds(req, more);
    return getAffectedNames();
  }
  /**
   * 获取可操作的名称
   * @param req
   * @return 用", "连接的可操作的名称
   */
  public String getOperableNames(Map req) throws HongsException {
    return getOperableNames(req, null);
  }

  /**
   * 获取受影响的 ID
   * @return IDs
   */
  public List<String> getAffectedIds() {
    return this.affectedIds;
  }
  /**
   * 获取受影响的名称
   * 默认取 findKeys 的第一位作为名称字段
   * 仅对调用过 save,update,remove 的有效
   * 如果没有 dflag 则在 remove 后获取不到名称, 请通过 getOperableNames 获取
   * 此方法不是线程安全的
   * @return 用", "连接的受影响的名称
   */
  public String getAffectedNames() throws HongsException {
    StringBuilder sb = new StringBuilder();
    FetchMore     fm = new FetchMore( );
    String        fn = this.findKeys[0];
    fm.setOption("FETCH_DFLAG" , true );
    fm.select(".`"+fn+"`").where("id IN (?)", affectedIds);
    List<Map> rows = this.table.fetchMore(fm);
    for (Map  row  : rows) {
      sb.append(", ").append(row.get(fn).toString());
    }
    return sb.length()>0 ? sb.substring(2) : sb.toString();
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
    if (id == null || id.length() == 0)
    {
        id = Core.getUniqueId();
    }
    data.put(this.table.primaryKey, id);

    // 存入主数据
    this.table.insert(data);

    // 存入子数据
    this.table.insertSubValues(data);

    return id;
  }

  /**
   * 更新记录
   *
   * @param id
   * @param data
   * @return 记录ID
   * @throws app.hongs.HongsException
   */
  public String put(String id, Map<String, Object> data)
    throws HongsException
  {
    if (id == null || id.length() == 0)
    {
      throw new HongsException(0x10a6, "Primary Key can not be empty");
    }

    FetchMore more = new FetchMore();
    more.setOption("MODEL_METHOD", "put");
    if (this.idCheck(id, more)  !=  true)
    {
      throw new HongsException(0x10a8, "Can not update the resource for id '"+id+"'");
    }

    String xd = (String)data.get(this.table.primaryKey);
    if (xd == null || xd.length() != 0)
    {
        xd = id; // Not changed!
    }
    data.put(this.table.primaryKey, xd);

    // 更新主数据
    this.table.update(data, this.table.primaryKey + " = ?", id);

    // 更新子数据
    this.table.insertSubValues(data);

    return xd;
  }

  /**
   * 删除指定记录
   *
   * 为避免逻辑复杂混乱
   * 如需重写删除方法,
   * 请总是重写该方法.
   *
   * @param id
   * @param more
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int del(String id, FetchMore more)
    throws HongsException
  {
    if (id == null || id.length() == 0)
    {
      throw new HongsException(0x10a0, "ID can not be empty for remove");
    }

    if (more == null) more = new FetchMore();
    more.setOption("MODEL_METHOD", "del");
    if (this.idCheck(id, more)  !=  true)
    {
      throw new HongsException(0x10a8, "Can not remove the resource for id '"+id+"'");
    }

    // 删除子数据(当有dflag时不删除子数据)
    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    String dflag = conf.getProperty("core.table.field.dflag", "__dflag__");
    if (!this.table.getColumns().containsKey(dflag))
    {
      this.table.deleteSubValues(id);
    }

    // 删除主数据
    int i = this.table.delete("`"+this.table.primaryKey+"` = ?", id);

    return i;
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

  /**
   * 获取指定记录
   *
   * 为避免逻辑复杂混乱
   * 如需重写获取方法,
   * 请总是重写该方法.
   *
   * @param id
   * @param more
   * @return 记录数据
   * @throws app.hongs.HongsException
   */
  public Map get(String id, FetchMore more)
    throws HongsException
  {
    if (id == null || id.length() == 0)
    {
      throw new HongsException(0x10a0, "ID can not be empty for get");
    }

    if (more == null) more = new FetchMore();
    more.setOption("MODEL_METHOD", "get");
    if (this.idCheck(id, more)  !=  true)
    {
      throw new HongsException(0x10a8, "Can not get the resource for id '"+id+"'");
    }

    more.where(".`"+this.table.primaryKey+"` = ?", id);

    return this.table.fetchLess(more);
  }

  /**
   * 获取指定记录
   *
   * @param id
   * @return 记录数据
   * @throws app.hongs.HongsException
   */
  public Map get(String id)
    throws HongsException
  {
    return this.get(id, null);
  }

  //** 辅助方法 **/

  /**
   * "获取"过滤
   *
   * <pre>
   * 作用于getPage,getList上
   *
   * 如需添加过滤条件, 请重写此方法.
   * 注意: 此处需要类似引用参数, 故调用前请务必实例化req和fs.
   * 默认仅关联join类型为LEFT,INNER和link类型为BLS_TO,HAS_ONE的表,
   * 如需指定关联方式请设置FetchMore的option: ASSOC_JOINS, ASSOC_TYEPS,
   * 如需指定关联的表请设置FetchMore的option: ASSOC_TABLES
   *
   * 设计目标:
   * 2. 按照cols参数设置查询字段;
   * 3. 按照sort参数设置排序方式,
   *    多个字段排序: sort=a+b+c或sort=-a+b+c, -表示该字段逆序;
   * 1. 按照find参数设置模糊查询,
   *    多关键词搜索: find=x+y+z或-find=x+y+z, -表示排除式搜索;
   *    指定字段搜索: find.a=x或find.a.b=y, 同样适用上面的规则,
   *    a.b为搜索关联表, 但需注意: a,a.b必须在findKeys中有指定;
   * 4. 如果有id或ids参数则仅获取id或ids对应的记录,
   *    可使用-id=x或-id[]=x表示排除;
   * 5. 如果有字段名相同的参数则获取与之对应的记录,
   *    可使用-field=xxx表示排除条件.
   * 注: "+"在URL中表示空格.
   * </pre>
   *
   * @param req
   * @param more
   * @throws app.hongs.HongsException
   */
  protected void getFilter(Map req, FetchMore more)
    throws HongsException
  {
    // 默认仅连接类型为LEFT,INNER的表(必须满足左表)
    if (more.getOption("ASSOC_JOINS") == null)
    {
      Set types = new HashSet();
      types.add( "LEFT"  );
      types.add( "INNER" );
      more.setOption("ASSOC_JOINS", types);
    }

    // 默认仅关联类型为BLS_TO,HAS_ONE的表(仅能关联一个)
    if (more.getOption("ASSOC_TYPES") == null)
    {
      Set types = new HashSet();
      types.add("BLS_TO" );
      types.add("HAS_ONE");
      more.setOption("ASSOC_TYPES", types);
    }

    // 如果req为空则返回
    if (req.isEmpty())
    {
      return;
    }

    Map columns = this.table
                   .getColumns();
    Iterator it = req.entrySet()
                     .iterator();

    while (it.hasNext())
    {
      Map.Entry et = (Map.Entry)it.next();
      String key = (String)et.getKey();
      Object value = et.getValue();

      if (key == null || value == null
      ||  key.equals(this.rowsVar)
      ||  key.equals(this.pageVar))
      {
        continue;
      }

      // 字段
      if (key.equals(this.colsVar))
      {
        this.colsFilter(value, columns, more);
        continue;
      }

      // 排序
      if (key.equals(this.sortVar))
      {
        this.sortFilter(value, columns, more);
        continue;
      }

      /**
       * 用"-"来表示排除条件
       */
      boolean  not = key.startsWith("-");
      if (not) key = key.substring ( 1 );

      // 搜索
      if (key.equals(this.findVar))
      {
        /**
         * 为实现对指定的字段进行模糊搜索
         * 增加find.col和find.sub.col的用法
         * Add by Hongs, 2013.8.9
         */
        if (value instanceof Map) {
            List ks = Arrays.asList(this.findKeys);
            Map  m1 = (Map) value;
            for (Object o1 : m1.entrySet()) {
                Map.Entry e1 = (Map.Entry) o1;
                String k1 = e1.getKey().toString();
                Object v1 = e1.getValue();

                if (v1 instanceof Map) {
                    Map m2 = (Map) v1;
                    for (Object o2 : m2.entrySet()) {
                        Map.Entry e2 = (Map.Entry) o2;
                        String k2 = k1+"."+e2.getKey().toString();
                        String v2 = e2.getValue().toString();

                        if (ks.contains(k2)) {
                            this.findFilter(new String[]{k2}, v2, not, more);
                        }
                    }
                } else {
                    if (ks.contains(k1)) {
                        this.findFilter(new String[]{k1}, v1, not, more);
                    }
                }
            }
        } else {
                this.findFilter(this.findKeys, value, not, more);
        }
        continue;
      }

      // 主键
      if (key.equals(this.idVar))
      {
        this.mkeyFilter(this.table.primaryKey, value, not, more);
        continue;
      }

      // 当前表字段
      if (columns.containsKey(key))
      {
        this.mkeyFilter(key, value, not, more);
        continue;
      }

      // 关联表字段
      if (value instanceof Map)
      {
        this.skeyFilter(key, value, not, more);
        continue;
      }
    }
  }

  /**
   * 字段过滤(被getFilter调用)
   * 根据请求的字段设置查询及判断需要关联的表
   * @param value
   * @param columns
   * @param more
   * @throws HongsException
   */
  protected void colsFilter(Object value, Map columns, FetchMore more)
    throws HongsException
  {
    if (more.hasSelect()
    ||!(value instanceof List))
    {
      return;
    }
    List<String> cols = (List)value;
    if (cols.isEmpty())
    {
      return;
    }

    Set<String> tns = (Set<String>)more.getOption("ASSOC_TABLES");
    if (tns == null)
    {
        tns =  new HashSet();
        more.setOption("ASSOC_TABLES", tns);
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

        more.select( ".`" + col + "`" );
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
        FetchMore more2 = more.join(ts).join(tn);
        more2.select(".`" +fn+ "`");
      }
    }
  }

  /**
   * 排序过滤(被getFilter调用)
   * 如果字段有前缀“-”则该字段为逆序
   * @param value
   * @param columns
   * @param more
   * @throws HongsException
   */
  protected void sortFilter(Object value, Map columns, FetchMore more)
    throws HongsException
  {
    if (more.hasOrderBy()
    ||!(value instanceof String))
    {
      return;
    }
    String text = (String)value;
    if (text.length() == 0)
    {
      return;
    }

    Set<String> tns = (Set<String>)more.getOption("ASSOC_TABLES");
    if (tns == null)
    {
        tns =  new HashSet();
    }

    String[] a = text.split("\\s+");
    for (int i = 0; i < a.length; i ++)
    {
      String    sort = a[i];
      boolean   desc = sort.startsWith("-");
      if (desc) sort = sort.substring ( 1 );

      int pos = sort.indexOf('.');
      if (pos == -1)
      {
        if (!columns.containsKey(sort))
        {
          continue;
        }

        more.orderBy(sort +(desc?" DESC":""));
      }
      else
      {
        String tn = sort.substring(0 , pos);
        String fn = sort.substring(pos + 1);

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
        more.join(ts).join(tn);
        more.orderBy(sort +(desc?" DESC":""));
      }
    }
  }

  /**
   * 搜索过滤(被getFilter调用)
   * @param keys
   * @param val
   * @param not
   * @param more
   */
  protected void findFilter(String[] keys, Object val, boolean not, FetchMore more)
  {
    if (keys  ==  null
    ||!(val instanceof String))
    {
      return;
    }
    String text = (String)val;
    if (text.length() == 0)
    {
      return;
    }

    String[] a = text.split("\\s+");
    for (int i = 0; i < a.length; i ++)
    {
      String find = a[i];

      /**
       * 符号"%_^[]"在SQL LIKE中有特殊意义,
       * 需要对这些符号进行转义;
       * 前后加"%"用于模糊匹配.
       */
      find = Str.escape( find, "%_", "/" );
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

        more.where(key + (not?" NOT LIKE ?":" LIKE ?") + " ESCAPE '/'", find);
      }
    }
  }

  /**
   * 当前表字段过滤
   * @param key
   * @param val
   * @param not
   * @param more
   */
  protected void mkeyFilter(String key, Object val, boolean not, FetchMore more)
  {
    if (val instanceof String)
    {
      String id = (String)val;
      if (!"".equals(id))
        more.where(".`"+key+(not?"` != ?":"` = ?"), id);
    }
    else
    if (val instanceof List)
    {
      List<String> ids = (List)val;
      if (!ids.isEmpty())
        more.where(".`"+key+(not?"` NOT IN (?)":"` IN (?)"), ids);
    }
  }

  /**
   * 关联表字段过滤
   * @param key
   * @param val
   * @param not
   * @param more
   */
  protected void skeyFilter(String key, Object val, boolean not, FetchMore more)
  throws HongsException
  {
    Set<String> tns = (Set<String>)more.getOption("ASSOC_TABLES");
    if (tns == null)
    {
        tns = new HashSet();
        more.setOption("ASSOC_TABLES", tns);
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
          FetchMore more2 = more.join(ts ).join( key );
          more2.where(".`"+key2+(not?"` != ?":"` = ?"), id);
        }
      }
      else
      if (val2 instanceof List)
      {
        List<String> ids = (List)val2;
        if (!ids.isEmpty())
        {
          tns.add( key ); tns.addAll( ts );
          FetchMore more2 = more.join(ts ).join( key );
          more2.where(".`"+key2+(not?"` NOT IN (?)":"` IN (?)"), ids);
        }
      }
    }
  }

  /**
   * 检查id对应的数据是否可获取/修改/删除
   *
   * 作用于get,put,del上
   *
   * 默认调用"getFilter"来判断id是否允许操作;
   * 如需对以上方法进行其他过滤,可覆盖该方法.
   * 在"getFilter"方法中可以通过
   * "idCheck".equals(FetchMore.getOption("CHECK_METHOD"))
   * 来区分是不是"idCheck"发起的
   *
   * @param id
   * @param more
   * @return 可操作则返回true, 反之返回false
   */
  protected boolean idCheck(String id, FetchMore more)
    throws HongsException
  {
    if (more != null)
    {
      more = more.clone();
    }
    else
    {
      more = new FetchMore();
    }

    more.setOption("CHECK_METHOD", "idCheck");
    more.setOption("ASSOC_TABLES", new HashSet( ));
    more.setSelect(".`"+this.table.primaryKey+"`")
          .where(".`"+this.table.primaryKey+"`=?", id);

    // 默认调用getFilter进行校验
    this.getFilter(new HashMap(), more );

    return !this.table.fetchLess( more ).isEmpty();
  }

}
