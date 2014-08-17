package app.hongs.db;

import app.hongs.HongsException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.io.Serializable;

/**
 * 查询结构体
 *
 * <p>
 * 字段名前, 用"."表示属于当前表, 用":"表示属于上级表.<br/>
 * 关联字段, 用"表.列"描述字段时, "."的两侧不得有空格.<br/>
 * 本想自动识别字段的所属表(可部分区域), 但总是出问题;<br/>
 * 好的规则胜过万行代码, 定此规矩, 多敲了一个符号而已.<br/>
 * setOption用于登记特定查询选项, 以备组织查询结构的过程中读取.
 * </p>
 *
 * <h3>将SQL语句拆解成以下对应部分:</h3>
 * <pre>
 * fields         SELECT field1, field2...
 * tableName name FROM tableName AS name
 * wheres         WHERE expr1 AND expr2...
 * groups         GROUP BY field1, field2...
 * havins         HAVING expr1 AND expr2...
 * orders         ORDER BY field1, field2...
 * limits         LIMIT offset, length
 * </pre>
 *
 * <h3>系统已定义的"options":</h3>
 * <pre>
 * ASSOC_TABLES : Set         仅对某些表做关联; 作用域: FetchJoin.assocSelect
 * ASSOC_TYPES  : Set         仅对某些类型关联; 作用域: FetchJoin.assocSelect
 * ASSOC_JOINS  : Set         仅对某些类型连接; 作用域: FetchJoin.assocSelect
 * MULTI_ASSOC  : boolean     多行关联(使用IN方式关联); 作用域: FetchJoin
 * UNITY_ASSOC  : boolean     单体关联(仅限非多行关联); 作用域: FetchJoin
 * page         : int|String  分页页码; 作用域: FetchPage
 * rows         : int|String  分页行数; 作用域: FetchPage
 * </pre>
 *
 * <h3>异常代码:</h3>
 * <pre>
 * 区间: 0x10b0~0x10bf
 * 0x10b0 无法识别关联类型(JOIN)
 * 0x10b2 必须指定关联条件(FULL|LEFT|RIGHT)_JOIN
 * 0x10b4 没有指定查询表名
 * </pre>
 *
 * @author Hongs
 */
public class FetchCase
  implements Cloneable, Serializable
{

  protected String              tableName;
  protected String              name;

  protected StringBuilder       fields;
  protected StringBuilder       wheres;
  protected StringBuilder       groups;
  protected StringBuilder       havins;
  protected StringBuilder       orders;
  protected int[]               limits;

  private   List<Object>        wparams;
  private   List<Object>        hparams;
  protected Map<String,Object>  options;

  private   short               joinType;
  private   String              joinExpr;
  protected Set<FetchCase>      joinList;

  public static final short     INNER = 1;
  public static final short      LEFT = 2;
  public static final short     RIGHT = 3;
  public static final short      FULL = 4;
  public static final short     CROSS = 5;

  private static final Pattern p1 = Pattern
          .compile("(^|[^`\\w])\\.([`\\w\\*])");
  private static final Pattern p2 = Pattern
          .compile("(^|[^`\\w])\\:([`\\w\\*])");
  private static final Pattern pf = Pattern
          .compile("^\\s*,\\s*", Pattern.CASE_INSENSITIVE);
  private static final Pattern pw = Pattern
          .compile("^\\s*(AND|OR)\\s+", Pattern.CASE_INSENSITIVE);

  //** 构造 **/

  /**
   * 构造表结构对象
   * @param caze 复制其全部属性
   */
  public FetchCase(FetchCase caze)
  {
    this.tableName  = caze.tableName;
    this.name       = caze.name;
    this.fields     = new StringBuilder(caze.fields);
    this.wheres     = new StringBuilder(caze.wheres);
    this.groups     = new StringBuilder(caze.groups);
    this.havins     = new StringBuilder(caze.havins);
    this.orders     = new StringBuilder(caze.orders);
    this.limits     = caze.limits;
    this.wparams    = new ArrayList(caze.wparams);
    this.hparams    = new ArrayList(caze.hparams);
    this.options    = new HashMap(caze.options);
    this.joinType   = caze.joinType;
    this.joinExpr   = caze.joinExpr;
    this.joinList   = new LinkedHashSet(caze.joinList);
  }

  /**
   * 构建表结构对象
   * @param name
   * @param tableName
   */
  public FetchCase(String tableName, String name)
  {
    this.tableName  = tableName;
    this.name       = name;
    this.fields     = new StringBuilder();
    this.wheres     = new StringBuilder();
    this.groups     = new StringBuilder();
    this.havins     = new StringBuilder();
    this.orders     = new StringBuilder();
    this.limits     = new int[0];
    this.wparams    = new ArrayList();
    this.hparams    = new ArrayList();
    this.options    = new HashMap();
    this.joinType   = 0 ;
    this.joinExpr   = "";
    this.joinList   = new LinkedHashSet();
  }

  /**
   * 构建表结构对象
   * @param tableName
   */
  public FetchCase(String tableName)
  {
    this(tableName, tableName);
  }

  /**
   * 构造表结构对象
   * @param table 取tableName和name
   */
  public FetchCase(Table  table)
  {
    this(table.tableName, table.name);
  }

  /**
   * 构建空结构
   */
  public FetchCase()
  {
    this(null, null);
  }

  //** 查询 **/

  /***
   * 设置查询表和别名
   * @param tableName
   * @param name
   * @return 当前查询结构对象
   */
  public FetchCase from(String tableName, String name)
  {
    this.tableName = tableName;
    this.name = name;
    return this;
  }

  /**
   * 设置查询表(如果别名已设置则不会更改)
   * @param tableName
   * @return 当前查询结构对象
   */
  public FetchCase from(String tableName)
  {
    this.tableName = tableName;
    if (this.name == null)
    this.name = tableName;
    return this;
  }

  /**
   * 追加查询字段
   * 必须包含当前表字段, 必须在当前表字段前加".";
   * @param fields
   * @return 当前查询结构对象
   */
  public FetchCase select(String fields)
  {
    this.fields.append(", ").append(fields);
    return this;
  }

  /**
   * 追加查询条件
   * 字段名前, 用"."表示属于当前表, 用":"表示属于上级表
   * @param where
   * @param params 对应 where 中的 ?
   * @return 当前查询结构对象
   */
  public FetchCase where(String where, Object... params)
  {
    this.wheres.append(" AND ").append(where);
    this.wparams.addAll(Arrays.asList(params));
    return this;
  }

  /**
   * 追加分组字段
   * 必须包含当前表字段, 必须在当前表字段前加".";
   * @param fields
   * @return 当前查询结构对象
   */
  public FetchCase groupBy(String fields)
  {
    this.groups.append(", ").append(fields);
    return this;
  }

  /**
   * 追加过滤条件
   * 字段名前, 用"."表示属于当前表, 用":"表示属于上级表
   * @param where
   * @param params 对应 where 中的 ?
   * @return 当前查询结构对象
   */
  public FetchCase having(String where, Object... params)
  {
    this.havins.append(" AND ").append(where);
    this.hparams.addAll(Arrays.asList(params));
    return this;
  }

  /**
   * 追加排序字段
   * 必须包含当前表字段, 必须在当前表字段前加".";
   * @param fields
   * @return 当前查询结构对象
   */
  public FetchCase orderBy(String fields)
  {
    this.orders.append(", ").append(fields);
    return this;
  }

  /**
   * 设置限额
   * @param start
   * @param limit
   * @return 当前查询结构对象
   */
  public FetchCase limit(int start, int limit)
  {
    this.limits = limit == 0 ? new int[0] : new int[] {start, limit};
    return this;
  }

  /**
   * 设置限额
   * @param limit
   * @return 当前查询结构对象
   */
  public FetchCase limit(int limit)
  {
    this.limits = limit == 0 ? new int[0] : new int[] {0, limit};
    return this;
  }

  public int[] getLimit() {
      return this.limits;
  }

  //** 关联 **/

  private FetchCase link(FetchCase caze,
    String joinExpr, short joinType)
    throws HongsException
  {
    if (joinType < 0 || joinType > 5)
    {
      throw new HongsException(0x10b0,
        "Unrecognized join type '"+joinType+"'");
    }
    if ((joinType == FetchCase.INNER
    ||   joinType == FetchCase.LEFT
    ||   joinType == FetchCase.RIGHT
    ||   joinType == FetchCase.FULL)
    &&  (joinExpr == null || joinExpr.length() == 0))
    {
      throw new HongsException(0x10b2,
        "JoinExpr be required in (FULL|LEFT|RIGHT)");
    }

    this.joinList.add(caze);
    caze.joinExpr  = joinExpr;
    caze.joinType  = joinType;
    caze.options   = this.options;
    return caze;
  }

  /**
   * 关联一个表(采用指定结构的方式)
   * 注意: 此方法将自动克隆原查询结构,
   * 需追加查询参数请接收其返回的对象,
   * 并在该对象上进行相应的操作.
   * @param caze
   * @param joinExpr .被join的表 :执行join的表
   * @param joinType INNER,LEFT,RIGHT,FULL,CROSS
   * @return 返回该关联的查询结构
   * @throws HongsException
   */
  public FetchCase join(FetchCase caze,
    String joinExpr, short joinType)
    throws HongsException
  {
    return this.link(new FetchCase(caze),
           joinExpr, joinType);
  }
  public FetchCase join(FetchCase caze,
    String joinExpr)
    throws HongsException
  {
    return this.link(new FetchCase(caze),
           joinExpr, INNER);
  }

  /**
   * 关联一个表(采用指定表名和别名的方式)
   * @param tableName
   * @param name
   * @param joinExpr .被join的表 :执行join的表
   * @param joinType INNER,LEFT,RIGHT,FULL,CROSS
   * @return 返回该关联的查询结构
   * @throws HongsException
   */
  public FetchCase join(String tableName, String name,
    String joinExpr, short joinType)
    throws HongsException
  {
    return this.link(new FetchCase(tableName, name),
           joinExpr, joinType);
  }
  public FetchCase join(String tableName, String name,
    String joinExpr)
    throws HongsException
  {
    return this.link(new FetchCase(tableName, name),
           joinExpr, INNER);
  }

  /**
   * 关联一个表(采用指定表名或别名的方式)
   * @param tableName
   * @param joinExpr .被join的表 :执行join的表
   * @param joinType INNER,LEFT,RIGHT,FULL,CROSS
   * @return 返回该关联的查询结构
   * @throws HongsException
   */
  public FetchCase join(String tableName,
    String joinExpr, short joinType)
    throws HongsException
  {
    return this.link(new FetchCase(tableName),
           joinExpr, joinType);
  }
  public FetchCase join(String tableName,
    String joinExpr)
    throws HongsException
  {
    return this.link(new FetchCase(tableName),
           joinExpr, INNER);
  }

  /**
   * 关联一个表(采用指定表对象的方式)
   * @param table
   * @param joinExpr .被join的表 :执行join的表
   * @param joinType INNER,LEFT,RIGHT,FULL,CROSS
   * @return 返回该关联的查询结构
   * @throws HongsException
   */
  public FetchCase join(Table  table,
    String joinExpr, short joinType)
    throws HongsException
  {
    return this.link(new FetchCase(table),
           joinExpr, joinType);
  }
  public FetchCase join(Table  table,
    String joinExpr)
    throws HongsException
  {
    return this.link(new FetchCase(table),
           joinExpr, INNER);
  }

  /**
   * 设置/获取一个关联表
   * 注意: 如果之前没设置该别名的关联,
   * 则会自动创建一个该别名的关联结构,
   * 最后查询时务必补全表名和关联属性.
   * @param name
   * @return 返回该关联的查询结构
   * @throws HongsException
   */
  public FetchCase join(String name)
    throws HongsException
  {
    FetchCase caze = this.getJoin(name);
    if (caze == null)
        caze =  this.link(new FetchCase(name), null, (short)0);
    return caze;
  }
  public FetchCase join(String... path)
    throws HongsException
  {
    FetchCase caze = this;
    for (String n : path) caze = caze.join(n);
    return caze;
  }
  public FetchCase join(List<String> path)
    throws HongsException
  {
    FetchCase caze = this;
    for (String n : path) caze = caze.join(n);
    return caze;
  }

  /**
   * 获取关联的表查询结构
   * @param name
   * @return 指定关联的表查询结构对象
   */
  public FetchCase getJoin(String name)
  {
    for (FetchCase caze : this.joinList)
    {
      if (this.name.equals(name))
      {
        return caze;
      }
    }
    return null;
  }

  /**
   * 设置管理的表查询关系
   * @param joinExpr .被join的表 :执行join的表
   * @param joinType INNER,LEFT,RIGHT,FULL,CROSS
   * @return 当前查询结构对象
   */
  public FetchCase setJoin(String joinExpr, short joinType)
  {
    this.joinExpr = joinExpr;
    this.joinType = joinType;
    return this;
  }

  //** 选项 **/

  /**
   * 是否存在选项
   * @param key
   * @return 存在为true, 反之为false
   */
  public boolean hasOption(String key)
  {
    return this.options.containsKey(key);
  }

  /**
   * 获取选项
   * @param key
   * @return 指定选项
   */
  public Object getOption(String key)
  {
    return this.options.get(key);
  }

  /**
   * 获取选项(可指定类型)
   * @param <T>
   * @param key
   * @param def
   * @return 指定选项
   */
  public <T> T getOption(String key, T def)
  {
    Object obj = this.options.get(key);
    return obj != null ? (T)obj : def;
  }

  /**
   * 设置参数(单个)
   * @param key
   * @param obj
   * @return 当前查询结构对象
   */
  public FetchCase setOption(String key, Object obj)
  {
    this.options.put(key, obj);
    return this;
  }

  //** 获取构造结果 **/

  /**
   * 获取SQL
   * @return SQL
   */
  public String getSQL()
  {
    return this.getSQLStrb().toString();
  }

  /**
   * 获取SQL字串
   * @return SQL字串
   */
  private StringBuilder getSQLStrb()
  {
    StringBuilder t = new StringBuilder();
    StringBuilder f = new StringBuilder();
    StringBuilder g = new StringBuilder();
    StringBuilder o = new StringBuilder();
    StringBuilder w = new StringBuilder();
    StringBuilder h = new StringBuilder();
    this.getSQLDeep(  t, f, g, o, w, h, null);

    StringBuilder sql = new StringBuilder("SELECT");

    // 字段
    if (f.length() != 0)
    {
      sql.append(" ")
         .append(pf.matcher(f).replaceFirst(""));
    }

    // 表名
    sql.append(" FROM ").append(t);

    // 条件
    if (w.length() != 0)
    {
      sql.append(" WHERE ")
         .append(pw.matcher(w).replaceFirst(""));
    }

    // 分组
    if (g.length() != 0)
    {
      sql.append(" GROUP BY ")
         .append(pf.matcher(g).replaceFirst(""));
    }

    // 过滤
    if (h.length() != 0)
    {
      sql.append(" WHERE ")
         .append(pw.matcher(h).replaceFirst(""));
    }

    // 排序
    if (o.length() != 0)
    {
      sql.append(" ORDER BY ")
         .append(pf.matcher(o).replaceFirst(""));
    }

    // 限额(不同数据库的限额方式不一样, 在 DB.limit 中实现)
//    if (this.limits.length > 0)
//    {
//      sql.append(" LIMIT ?, ?");
//    }

    //sql = DB.formatSQLFields(sql);

    return sql;
  }

  /**
   * 获取SQL组合
   * @return SQL组合
   */
  private void getSQLDeep(StringBuilder t, StringBuilder f,
                          StringBuilder g, StringBuilder o,
                          StringBuilder w, StringBuilder h,
                          String rp2)
  {
    if (this.tableName == null
    ||  this.tableName.length() == 0)
    {
        throw new Error( new HongsException(0x10b4) );
    }

    // 表名/替换
    String rp;
    StringBuilder b = new StringBuilder();
    b.append("`").append(this.tableName ).append("`");
    if (this.name != null
    &&  this.name.length() != 0
    && !this.name.equals(this.tableName))
    {
      b.append(" AS `").append(this.name).append("`");
      rp = "$1`"+this.name+"`.$2";
    }
    else
    {
      rp = "$1`"+this.tableName+"`.$2";
    }

    // 关联
    if (rp2 != null)
    {
      switch (this.joinType)
      {
        case FetchCase.INNER: b.insert(0," INNER JOIN "); break;
        case FetchCase.LEFT : b.insert(0, " LEFT JOIN "); break;
        case FetchCase.RIGHT: b.insert(0," RIGHT JOIN "); break;
        case FetchCase.FULL : b.insert(0, " FULL JOIN "); break;
        case FetchCase.CROSS: b.insert(0," CROSS JOIN "); break;
        default: return;
      }
      if (this.joinExpr != null && this.joinExpr.length() != 0)
      {
        String s  =  this.joinExpr;
        s = p1.matcher(s).replaceAll(rp );
        s = p2.matcher(s).replaceAll(rp2);
        b.append(" ON ").append(s);
      }
    }

    t.append(b);

    // 字段
    if (this.fields.length() != 0)
    {
      String s = this.fields.toString().trim();
                       s = p1.matcher(s).replaceAll(rp);
      if (rp2 != null) s = p2.matcher(s).replaceAll(rp2);
      f.append(" ").append(s);
    }
    else if (rp2 == null)
    {
      f.append(" ,`").append(this.name).append("`.*");
    }

    // 分组
    if (this.groups.length() != 0)
    {
      String s = this.groups.toString().trim();
                       s = p1.matcher(s).replaceAll(rp);
      if (rp2 != null) s = p2.matcher(s).replaceAll(rp2);
      g.append(" ").append(s);
    }

    // 排序
    if (this.orders.length() != 0)
    {
      String s = this.orders.toString().trim();
                       s = p1.matcher(s).replaceAll(rp);
      if (rp2 != null) s = p2.matcher(s).replaceAll(rp2);
      o.append(" ").append(s);
    }

    // 条件
    if (this.wheres.length() != 0)
    {
      String s = this.wheres.toString().trim();
                       s = p1.matcher(s).replaceAll(rp);
      if (rp2 != null) s = p2.matcher(s).replaceAll(rp2);
      w.append(" ").append(s);
    }

    // 过滤
    if (this.havins.length() != 0)
    {
      String s = this.havins.toString().trim();
                       s = p1.matcher(s).replaceAll(rp);
      if (rp2 != null) s = p2.matcher(s).replaceAll(rp2);
      h.append(" ").append(s);
    }

    // 追加子级查询片段
    Iterator it = this.joinList.iterator( );
    while (it.hasNext())
    {
      FetchCase caze = (FetchCase)it.next();
      if (0  != caze.joinType)
      {
        caze.getSQLDeep(t, f, g,o, w,h, rp);
      }
    }
  }

  /**
   * 获取参数
   * @return 参数
   */
  public Object[] getParams()
  {
    return this.getParamsList().toArray();
  }

  /**
   * 获取参数列表
   * @return 参数列表
   */
  private List getParamsList()
  {
    List paramz = new ArrayList();
    List wparamz = new ArrayList();
    List hparamz = new ArrayList();

    // 参数
    this.getParamsDeep(wparamz, hparamz);
    paramz.addAll(wparamz);
    paramz.addAll(hparamz);

    // 限额(不同数据库的限额方式不一样, 在 DB.limit 中实现)
//    if (this.limits.length > 0)
//    {
//      paramz.add(this.limits[0]);
//      paramz.add(this.limits[1]);
//    }

    return paramz;
  }

  /**
   * 获取参数组合
   * @return 参数组合
   */
  private void getParamsDeep(List wparamz, List hparamz)
  {
    wparamz.addAll(this.wparams);
    hparamz.addAll(this.hparams);

    for (FetchCase caze  :  this.joinList)
    {
      if (0 == caze.joinType)
      {
        continue;
      }

      caze.getParamsDeep(wparamz, hparamz);
    }
  }

  /**
   * 转换为字符串
   * @return 合并了SQL和参数
   */
  @Override
  public String toString()
  {
    StringBuilder sb = this.getSQLStrb();
    List   paramz = this.getParamsList();
    try
    {
      DB.checkSQLParams(sb, paramz);
      DB.mergeSQLParams(sb, paramz);
    }
    catch (HongsException ex)
    {
           return   null;
    }
    return sb.toString();
  }

  /**
   * 克隆
   * @return 新查询结构对象
   */
  @Override
  public FetchCase clone()
  {
    return new FetchCase(this);
  }

  //** 不推荐的方法 **/

  /**
   * 是否有设置查询字段
   * @return 存在为true, 反之为false
   * @deprecated
   */
  public boolean hasSelect()
  {
    return this.fields.length() != 0;
  }

  /**
   * 设置查询字段
   * @param fields
   * @return 当前查询结构对象
   * @deprecated
   */
  public FetchCase setSelect(String fields)
  {
    this.fields = new StringBuilder(checkField(fields));
    return this;
  }

  /**
   * 是否有设置查询条件
   * @return 存在为true, 反之为false
   * @deprecated
   */
  public boolean hasWhere()
  {
    return this.wheres.length() != 0;
  }

  /**
   * 设置查询条件
   * 字段名前, 用"."表示属于当前表, 用":"表示属于上级表
   * @param where
   * @param params 对应 where 中的 ?
   * @return 当前查询结构对象
   * @deprecated
   */
  public FetchCase setWhere(String where, Object... params)
  {
    this.wheres = new StringBuilder(checkWhere(where));
    this.wparams = Arrays.asList(params);
    return this;
  }

  /**
   * 是否有设置分组
   * @return 存在为true, 反之为false
   * @deprecated
   */
  public boolean hasGroupBy()
  {
    return this.groups.length() != 0;
  }

  /**
   * 设置分组字段
   * @param fields
   * @return 当前查询结构对象
   * @deprecated
   */
  public FetchCase setGroupBy(String fields)
  {
    this.groups = new StringBuilder(checkField(fields));
    return this;
  }

  /**
   * @deprecated
   */
  public boolean hasHaving()
  {
    return this.havins.length() != 0;
  }

  /**
   * @deprecated
   */
  public FetchCase setHaving(String where, Object... params)
  {
    this.havins = new StringBuilder(checkWhere(where));
    this.hparams = Arrays.asList(params);
    return this;
  }

  /**
   * 是否有设置排序
   * @return 存在为true, 反之为false
   * @deprecated
   */
  public boolean hasOrderBy()
  {
    return this.orders.length() != 0;
  }

  /**
   * 设置排序字段
   * @param fields
   * @return 当前查询结构对象
   * @deprecated
   */
  public FetchCase setOrderBy(String fields)
  {
    this.orders = new StringBuilder(checkField(fields));
    return this;
  }

  private String checkField(String field)
  {
    if (field == null) return "";
        field = field.trim();
    if (field.length() != 0
    && !field.startsWith(","))
    {
      return ", " + field;
    }
    else
    {
      return field;
    }
  }

  private String checkWhere(String where)
  {
    if (where == null) return "";
        where = where.trim();
    if (where.length() != 0
    && !where.matches("^(AND|OR) (?i)"))
    {
      return "AND " + where;
    }
    else
    {
      return where;
    }
  }
}
