package app.hongs.db;

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

import app.hongs.HongsException;

/**
 * <h1>查询结构体</h1>
 *
 * <h2>将SQL语句拆解成以下对应部分:</h2>
 * <pre>
 * fields       SELECT field1, field2...
 * tableName name FROM tableName AS name
 * wheres         WHERE  expr1 AND expr2...
 * groups         GROUP BY field1, field2...
 * havins         HAVING expr1 AND expr2...
 * orders         ORDER BY field1, field2...
 * limits         LIMIT offset, length
 * </pre>
 *
 * <h2>注意:</h2>
 * <pre>
 * 字段名前, 用"."表示属于当前表, 用":"表示属于上级表.
 * 关联字段, 用"表.列"描述字段时, "."的两侧不得有空格.
 * 本想自动识别字段的所属表(可部分区域), 但总是出问题;
 * 好的规则胜过万行代码, 定此规矩, 多敲了一个符号而已.
 * setOption用于登记特定查询选项, 以备组织查询结构的过程中读取.
 * </pre>
 *
 * <h2>系统已定义的"options":</h2>
 * <pre>
 * ASSOC_TABLES : Set         仅对某些表做关联; 作用域: FetchMore.assocSelect
 * ASSOC_TYPES  : Set         仅对某些类型关联; 作用域: FetchMore.assocSelect
 * ASSOC_JOINS  : Set         仅对某些类型连接; 作用域: FetchMore.assocSelect
 * MULTI_ASSOC  : boolean     多行关联(使用IN方式关联); 作用域: FetchMore
 * UNITY_ASSOC  : boolean     单体关联(仅限非多行关联); 作用域: FetchMore
 * page         : int|String  分页页码; 作用域: FetchPage
 * rows         : int|String  分页行数; 作用域: FetchPage
 * </pre>
 *
 * <h2>异常代码:</h2>
 * <pre>
 * 区间: 0x10b0~0x10bf
 * 0x10b0 无法识别的关联类型(JOIN)
 * 0x10b2 (FULL|LEFT|RIGHT)_JOIN必须指定关联条件
 * </ul>
 *
 * @author Hongs
 */
public class FetchBean
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
  protected Set<FetchBean>      joinList;

  public static final short     INNER = 1;
  public static final short      LEFT = 2;
  public static final short     RIGHT = 3;
  public static final short      FULL = 4;
  public static final short     CROSS = 5;

  private static final Pattern p1 = Pattern
          .compile("(^|[^`\\w\\.])\\.([`\\w\\*])");
  private static final Pattern p2 = Pattern
          .compile("(^|[^`\\w\\.])\\:([`\\w\\*])");
  private static final Pattern pf = Pattern
          .compile("^\\s*,\\s*", Pattern.CASE_INSENSITIVE);
  private static final Pattern pw = Pattern
          .compile("^\\s*(AND|OR)\\s+", Pattern.CASE_INSENSITIVE);

  /** 构造 **/

  /**
   * 构建表结构对象
   * @param name
   * @param tableName
   */
  public FetchBean(String name, String tableName)
  {
    this.name       = name;
    this.tableName  = tableName;
    this.fields     = new StringBuilder();
    this.wheres     = new StringBuilder();
    this.groups     = new StringBuilder();
    this.havins     = new StringBuilder();
    this.orders     = new StringBuilder();
    this.limits     = new int[0];
    this.joinType   = 0;
    this.joinExpr   = "";
    this.joinList   = new LinkedHashSet();
    this.wparams    = new ArrayList();
    this.hparams    = new ArrayList();
    this.options    = new HashMap();
  }

  /**
   * 构建表结构对象
   * @param tableName
   */
  public FetchBean(String name)
  {
    this(name, name);
  }

  /**
   * 构建空结构
   */
  public FetchBean()
  {
    this("", "");
  }

  /**
   * 构造表结构对象
   * @param table 取tableName和name
   */
  public FetchBean(Table table)
  {
    this(table.name, table.tableName);
  }

  /**
   * 构造表结构对象
   * @param fs 复制其全部属性
   */
  public FetchBean(FetchBean fs)
  {
    this.name       = fs.name;
    this.tableName  = fs.tableName;
    this.fields     = new StringBuilder(fs.fields);
    this.wheres     = new StringBuilder(fs.wheres);
    this.groups     = new StringBuilder(fs.groups);
    this.havins     = new StringBuilder(fs.havins);
    this.orders     = new StringBuilder(fs.orders);
    this.limits     = fs.limits;
    this.joinType   = fs.joinType;
    this.joinExpr   = fs.joinExpr;
    this.options    = new HashMap(fs.options);
    this.wparams    = new ArrayList(fs.wparams);
    this.hparams    = new ArrayList(fs.hparams);
    this.joinList = new LinkedHashSet(fs.joinList);
  }

  /** 查询 **/

  /**
   * 追加查询字段
   * 必须包含当前表字段, 必须在当前表字段前加".";
   * @param fields
   * @return 当前查询结构对象
   */
  public FetchBean select(String fields)
  {
    this.fields.append(", ").append(fields);
    return this;
  }

  /**
   * 追加查询条件
   * 字段名前, 用"."表示属于当前表, 用":"表示属于上级表
   * @param wheres
   * @return 当前查询结构对象
   */
  public FetchBean where(String where, Object... params)
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
  public FetchBean groupBy(String fields)
  {
    this.groups.append(", ").append(fields);
    return this;
  }

  /**
   * 追加过滤条件
   * 字段名前, 用"."表示属于当前表, 用":"表示属于上级表
   * @param wheres
   * @return 当前查询结构对象
   */
  public FetchBean having(String where, Object... params)
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
  public FetchBean orderBy(String fields)
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
  public FetchBean limit(int start, int limit)
  {
    this.limits = limit == 0 ? new int[0] : new int[] {start, limit};
    return this;
  }

  /**
   * 设置限额
   * @param limit
   * @return 当前查询结构对象
   */
  public FetchBean limit(int limit)
  {
    this.limits = limit == 0 ? new int[0] : new int[] {0, limit};
    return this;
  }

  /** 关联 **/

  private FetchBean link(FetchBean fs,
    String joinExpr, short joinType)
    throws HongsException
  {
    if (joinType < 0 || joinType > 5)
    {
      throw new HongsException(0x10b0,
        "Unrecognized join type '"+joinType+"'");
    }
    if ((joinType == FetchBean.INNER
    ||   joinType == FetchBean.LEFT
    ||   joinType == FetchBean.RIGHT
    ||   joinType == FetchBean.FULL)
    &&  (joinExpr == null || joinExpr.length() == 0))
    {
      throw new HongsException(0x10b2,
        "JoinExpr be required in (FULL|LEFT|RIGHT)");
    }

    this.joinList.add(fs);
    fs.joinExpr  = joinExpr;
    fs.joinType  = joinType;
    fs.options   = this.options;
    return fs;
  }

  /**
   * 关联一个表(采用指定结构的方式)
   * 注意: 此方法将自动克隆原查询结构,
   * 需追加查询参数请接收其返回的对象,
   * 并在该对象上进行相应的操作.
   * @param fs
   * @param joinExpr
   * @param joinType
   * @return 返回该关联的查询结构
   * @throws HongsException
   */
  public FetchBean join(FetchBean fs,
    String joinExpr, short joinType)
    throws HongsException
  {
    return this.link(new FetchBean(fs),
           joinExpr, joinType);
  }
  public FetchBean join(FetchBean fs,
    String joinExpr)
    throws HongsException
  {
    return this.link(new FetchBean(fs),
           joinExpr, INNER);
  }

  /**
   * 关联一个表(采用指定表对象的方式)
   * @param table
   * @param joinExpr
   * @param joinType
   * @return 返回该关联的查询结构
   * @throws HongsException
   */
  public FetchBean join(Table table,
    String joinExpr, short joinType)
    throws HongsException
  {
    return this.link(new FetchBean(table),
           joinExpr, joinType);
  }
  public FetchBean join(Table table,
    String joinExpr)
    throws HongsException
  {
    return this.link(new FetchBean(table),
           joinExpr, INNER);
  }

  /**
   * 关联一个表(采用指定表名和别名的方式)
   * @param tableName
   * @param name
   * @param joinExpr
   * @param joinType
   * @return 返回该关联的查询结构
   * @throws HongsException
   */
  public FetchBean join(String name, String tableName,
    String joinExpr, short joinType)
    throws HongsException
  {
    return this.link(new FetchBean(name, tableName),
           joinExpr, joinType);
  }
  public FetchBean join(String name, String tableName,
    String joinExpr)
    throws HongsException
  {
    return this.link(new FetchBean(name, tableName),
           joinExpr, INNER);
  }

  /**
   * 关联一个表(采用指定表名或别名的方式)
   * @param name
   * @param joinExpr
   * @param joinType
   * @return 返回该关联的查询结构
   * @throws HongsException
   */
  public FetchBean join(String name,
    String joinExpr, short joinType)
    throws HongsException
  {
    return this.link(new FetchBean(name),
           joinExpr, joinType);
  }
  public FetchBean join(String name,
    String joinExpr)
    throws HongsException
  {
    return this.link(new FetchBean(name),
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
  public FetchBean join(String name)
    throws HongsException
  {
    FetchBean bean = this.getJoin(name);
    if (bean == null)
        bean =  this.link(new FetchBean(name), null, (short)0);
    return bean;
  }
  public FetchBean join(String[] path)
    throws HongsException
  {
    FetchBean bean = this;
    for (String n : path) bean = bean.join(n);
    return bean;
  }
  public FetchBean join(List<String> path)
    throws HongsException
  {
    FetchBean bean = this;
    for (String n : path) bean = bean.join(n);
    return bean;
  }

  /**
   * 是否有关联指定表
   * @param name
   * @return 存在为true, 反之为false
   */
  protected boolean hasJoin(String name)
  {
    for (FetchBean bean : this.joinList)
    {
      if (this.name.equals(name))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * 获取关联的表查询结构
   * @param tableName
   * @return 指定关联的表查询结构对象
   */
  protected FetchBean getJoin(String name)
  {
    for (FetchBean bean : this.joinList)
    {
      if (this.name.equals(name))
      {
        return bean;
      }
    }
    return null;
  }

  /** 选项 **/

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
  public FetchBean setOption(String key, Object obj)
  {
    this.options.put(key, obj);
    return this;
  }

  /** 获取构造结果部分 **/

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

    // 查询限额
    if (this.limits.length > 0)
    {
      sql.append(" LIMIT ?, ?");
    }

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
    // 替换表别名
    String rp = "$1`" + this.name + "`.$2";

    // 表名
    StringBuilder b = new StringBuilder();
    b.append("`")
     .append(this.tableName )
     .append("` AS `")
     .append(this.name)
     .append("`");

    // 关联
    if (rp2 != null)
    {
      switch (this.joinType)
      {
        case FetchBean.INNER: b.insert(0, " INNER JOIN "); break;
        case FetchBean.LEFT : b.insert(0,  " LEFT JOIN "); break;
        case FetchBean.RIGHT: b.insert(0, " RIGHT JOIN "); break;
        case FetchBean.FULL : b.insert(0,  " FULL JOIN "); break;
        case FetchBean.CROSS: b.insert(0, " CROSS JOIN "); break;
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
    Iterator it = this.joinList.iterator();
    while (it.hasNext())
    {
      FetchBean fs = (FetchBean)it.next( );
      if (0  !=  fs.joinType)
      {
                 fs.getSQLDeep(t, f, g, o, w, h, rp);
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

    // 查询参数
    this.getParamsDeep(wparamz, hparamz);
    paramz.addAll(wparamz);
    paramz.addAll(hparamz);

    // 查询限额
    if (this.limits.length > 0)
    {
      paramz.add(this.limits[0]);
      paramz.add(this.limits[1]);
    }

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

    for (FetchBean fs : this.joinList)
    {
      if (0 == fs.joinType)
      {
        continue;
      }

      fs.getParamsDeep(wparamz, hparamz);
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
  public FetchBean clone()
  {
    return new FetchBean(this);
  }

  /** 不推荐的和废弃的方法 **/

  /**
   * 设置别名
   * @param name
   * @return 当前查询结构对象
   */
  public FetchBean setName(String name)
  {
    this.name = name != null ? name : "";
    if (this.tableName.length() == 0)
        this.tableName = this.name;
    return this;
  }

  /**
   * 设置表名
   * @param tableName
   * @return 当前查询结构对象
   */
  public FetchBean setTableName(String tableName)
  {
    this.tableName = tableName != null ? tableName : "";
    if (this.name.length( )  ==  0)
        this.name = this.tableName;
    return this;
  }

  public FetchBean setJoinParam(String expr, short type) {
      this.joinExpr = expr;
      this.joinType = type;
      return this;
  }

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
  public FetchBean setSelect(String fields)
  {
    this.fields = new StringBuilder(checkField(fields));
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
  public FetchBean setGroupBy(String fields)
  {
    this.groups = new StringBuilder(checkField(fields));
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
  public FetchBean setOrderBy(String fields)
  {
    this.orders = new StringBuilder(checkField(fields));
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
   * @param wheres
   * @return 当前查询结构对象
   * @deprecated
   */
  public FetchBean setWhere(String where, Object... params)
  {
    this.wheres = new StringBuilder(checkWhere(where));
    this.wparams = Arrays.asList(params);
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
  public FetchBean setHaving(String where, Object... params)
  {
    this.havins = new StringBuilder(checkWhere(where));
    this.hparams = Arrays.asList(params);
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
