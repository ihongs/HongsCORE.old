package app.hongs.db;

import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.util.Str;

/**
 * 数据库基础类
 *
 * <p>
 * 当需要库对象时, 一般情况可调用其工厂方法getInstance获取;
 * 当需要扩展类时, 请从DB继承并实现一个无参getInstance方法.
 * </p>
 *
 * <h3>配置选项:</h3>
 * <pre>
 * core.load.db.[dbName].once 为true则仅加载一次, 为false由Core控制
 * </pre>
 *
 * <h3>异常代码:</h3>
 * <pre>
 * 区间: 0x1010~0x105f
 *
 * 0x1011  找不到数据源名称配置
 * 0x1013  连接数据源失败
 * 0x1015  找不到数据库驱动配置
 * 0x1017  连接数据库失败
 * 0x1019  无数据源或无驱动配置
 *
 * 0x1021  找不到表配置
 * 0x1023  找不到表对应的类
 * 0x1025  无法获取表构造器
 * 0x1027  无法获取表实例
 *
 * 0x35    开启Connection失败
 * 0x37    关闭Connection失败
 * 0x102b  取消Statement失败
 * 0x102d  关闭Statement失败
 * 0x102f  关闭ResultSet失败
 *
 * 0x1031  构建查询体失败
 * 0x1032  绑定查询参数失败
 * 0x1034  获取列名失败
 * 0x1036  获取行数据失败
 *
 * 0x1041  执行查询语句失败
 * 0x1042  获取查询结果失败
 *
 * 0x1045  执行语句失败
 * 0x1046  插入的值不能为空
 * 0x1047  执行插入语句失败
 * 0x1048  更新的值不能为空
 * 0x1049  执行更新语句失败
 * 0x104b  执行删除语句失败
 *
 * 0x1051  查询参数的个数与语句中的插入位置数不符
 * </pre>
 *
 * @author Hongs
 */
public class DB
  implements Core.Destroy
{

  public boolean IN_OBJECT_MODE;

  /**
   * 数据库连接
   */
  public Connection connection;

  /**
   * 库名
   */
  protected String name;

  /**
   * 表类
   */
  protected String tableClass;

  /**
   * 表前缀
   */
  protected String tablePrefix;

  /**
   * 表后缀
   */
  protected String tableSuffix;

  /**
   * 表配置
   */
  protected Map<String, Map> tableConfigs;

  /**
   * 表对象
   */
  protected Map<String, Table> tableObjects;

  private Map       driver;
  private Map       source;

  public DB(DBConfig cf)
    throws HongsException
  {
    this.name         = cf.name;
    this.driver       = cf.driver;
    this.source       = cf.source;
    this.tableClass   = cf.tableClass;
    this.tablePrefix  = cf.tablePrefix;
    this.tableSuffix  = cf.tableSuffix;
    this.tableConfigs = cf.tableConfigs;
    this.tableObjects = new  HashMap( );

    this.connection   = null;
  }

  public DB (String db)
    throws HongsException
  {
    this(new DBConfig(db)); //this(DBConfig.parseByName(db));
  }

  public DB (java.io.File db)
    throws HongsException
  {
    this(DBConfig.parseByFile(db));
  }

  public DB (java.io.InputStream db)
    throws HongsException
  {
    this(DBConfig.parseByStream(db));
  }

  public DB (org.w3c.dom.Document db)
    throws HongsException
  {
    this(DBConfig.parseByDocument(db));
  }

  public final void open()
    throws HongsException
  {
    TOP: do {

    try
    {
      if (this.connection != null
      && !this.connection.isClosed())
      {
        break TOP;
      }
    }
    catch (SQLException ex)
    {
      throw new HongsError(0x35, ex);
    }

    Exception e = null;

    do
    {
      if (source == null || source.isEmpty())
      {
        break;
      }

      if (!source.containsKey("name"))
      {
        throw new HongsException(0x1011, "Can not find name in source");
      }

      String comp = "java:comp/env";
      String namc = (String)source.get("name");
      Properties info = (Properties)source.get("info");

      Context ct;
      DataSource ds;
      InitialContext ic;
      try
      {
        ic = new InitialContext();
        ct = (Context)ic.lookup(comp);
        ds = (DataSource)ct.lookup(namc);
      }
      catch (NamingException ex)
      {
        e =ex;
        break;
      }

      try
      {
        if (info.isEmpty())
        {
          this.connection = ds.getConnection();
        }
        else
        {
          this.connection = ds.getConnection(
                 info.getProperty("user"),
                 info.getProperty("password"));
        }

        if (Core.IN_DEBUG_MODE)
        {
          CoreLogger.debug("Connect to database(source mode), URL: "
                           +this.connection.getMetaData().getURL());
        }
      }
      catch (SQLException ex)
      {
        throw new app.hongs.HongsException(0x1013, ex);
      }

      break TOP;
    }
    while (false);

    do
    {
      if (driver == null || driver.isEmpty())
      {
        break;
      }

      if (!driver.containsKey("drv"))
      {
        throw new app.hongs.HongsException(0x1015, "Can not find drv in driver");
      }

      if (!driver.containsKey("url"))
      {
        throw new app.hongs.HongsException(0x1015, "Can not find url in driver");
      }

      String drv = (String)driver.get("drv");
      String url = (String)driver.get("url");
      Properties info = (Properties)driver.get("info");

      try
      {
        Class.forName(drv);

        if (info.isEmpty())
        {
          this.connection = DriverManager.getConnection(url);
        }
        else
        {
          this.connection = DriverManager.getConnection(url, info);
        }

        if (Core.IN_DEBUG_MODE)
        {
          CoreLogger.debug("Connect to database(driver mode), URL: "
                           +this.connection.getMetaData().getURL());
        }
      }
      catch (ClassNotFoundException ex)
      {
        throw new app.hongs.HongsException(0x1017, ex);
      }
      catch (SQLException ex)
      {
        throw new app.hongs.HongsException(0x1017, ex);
      }

      break TOP;
    }
    while (false);

    if (e !=null)
    {
      throw new HongsException(0x1019, e);
    }
    else
    {
      throw new HongsException(0x1019, "Can not find source and driver");
    }

    } while (false);

    /** 初始化设置 **/

    Core core = Core.getInstance();

    // 自动提交设置
    if (core.containsKey("__DB_AUTO_COMMIT__"))
    try {
        this.connection.setAutoCommit((Boolean)
                core.get("__DB_AUTO_COMMIT__"));
    }
    catch (SQLException ex) {
        throw new app.hongs.HongsError(0x10,ex);
    }

    // 对象模式设置
    CoreConfig conf = (CoreConfig)
      Core.getInstance(CoreConfig.class);
    IN_OBJECT_MODE  =  conf.getProperty("core.in.object.mode", false);
  }

  public void close()
  {
    try
    {
      if (this.connection == null
      ||  this.connection.isClosed())
      {
        return;
      }

      if (Core.IN_DEBUG_MODE)
      {
        CoreLogger.debug("Close database connection, URL: "
          + this.connection.getMetaData().getURL());
      }

      this.connection.close();
      this.connection = null;
    }
    catch (SQLException ex)
    {
      throw new HongsError(0x37, ex);
    }
  }

  @Override
  public void destroy()
  {
    this.close();
  }

  /**
   * 通过表名获取表对象
   * 表名可以为"库名.表名"
   * @param table
   * @return 指定表对象
   * @throws app.hongs.HongsException
   */
  public Table getTable(String table)
    throws HongsException
  {
    /**
     * 表名可以是"数据库.表名"
     * 用于引用另一个库中的表
     */
    int pos = table.indexOf('.');
    if (pos > 0)
    {
      String db = table.substring(0,  pos);
          table = table.substring(pos + 1);
      return DB.getInstance(db).getTable(table);
    }

    if (this.tableObjects.containsKey(table))
    {
      return this.tableObjects.get(table);
    }

    if (!this.tableConfigs.containsKey(table))
    {
      throw new app.hongs.HongsException(0x1021, "Can not find config for table '"+table+"'.");
    }

    /**
     * 读取库指定的tableClass
     * 读取表对应的tableConfig
     */
    Map<String, String> tcfg = this.tableConfigs.get(table);
    this.tableConfigs.remove(table);
    String tcls = this.tableClass;
    String tpfx = this.tablePrefix;
    String tsfx = this.tableSuffix;

    /**
     * 就近原则:
     * 如果表配置中有设置class则采用表配置中的
     * 如果表配置中没有设置prefix则采用库配置中的
     * 如果表配置中没有设置suffix则采用库配置中的
     */
    if (tcfg.containsKey("class"))
    {
      tcls = tcfg.get("class");
    }
    if (!tcfg.containsKey("prefix"))
    {
      tcfg.put("prefix", tpfx);
    }
    if (!tcfg.containsKey("suffix"))
    {
      tcfg.put("suffix", tsfx);
    }

    /**
     * 如果class为空则直接使用默认的Table
     */
    if (tcls == null || tcls.length() == 0)
    {
      Table tobj = new Table(this, tcfg);
      this.tableObjects.put(table, tobj);
      return tobj;
    }

    if (Core.IN_DEBUG_MODE)
    {
      app.hongs.CoreLogger.debug(
        "INFO(DB): tableClass("+tcls+") for table("+table+") has been defined, try to get it");
    }

    /**
     * 获取指定的Table类
     */
    Class cls;
    try
    {
      cls = Class.forName(tcls);
    }
    catch (ClassNotFoundException ex)
    {
      throw new app.hongs.HongsException(0x1023, ex);
    }

    /**
     * 获取构造器
     */
    Constructor cst;
    try
    {
      cst = cls.getConstructor(new Class[]{DB.class, Map.class});
    }
    catch (NoSuchMethodException ex)
    {
      throw new app.hongs.HongsException(0x1025, ex);
    }
    catch (SecurityException ex)
    {
      throw new app.hongs.HongsException(0x1025, ex);
    }

    /**
     * 获取表实例
     */
    Table tobj;
    try
    {
      tobj = (Table)cst.newInstance(new Object[]{this, tcfg});
    }
    catch (InstantiationException ex)
    {
      throw new app.hongs.HongsException(0x1027, ex);
    }
    catch (IllegalAccessException ex)
    {
      throw new app.hongs.HongsException(0x1027, ex);
    }
    catch (IllegalArgumentException ex)
    {
      throw new app.hongs.HongsException(0x1027, ex);
    }
    catch (InvocationTargetException ex)
    {
      throw new app.hongs.HongsException(0x1027, ex);
    }

    this.tableObjects.put(table, tobj);
    return tobj;
  }

  public Set<String> getTableNames()
  {
    return this.tableConfigs.keySet();
  }

  /** 查询辅助 **/

  /**
   * 预编译Statement并设置查询选项
   * <p>可使用cacheStatement开启缓存</p>
   * @param sql
   * @param params
   * @return PreparedStatement对象
   * @throws HongsException
   */
  public PreparedStatement prepareStatement(String sql, Object... params)
    throws HongsException
  {
    /**
     * 检查SQL语句及Params
     * 以发现里面的Set对象
     */
    List      paramz = new ArrayList(Arrays.asList(params));
    StringBuilder sb = new StringBuilder(sql);
    DB.checkSQLParams(sb, paramz);
    sql = sb.toString();

    PreparedStatement ps  = this.prepareStatement(  sql  );

    /**
     * 遍历params以执行PreparedStatement.setObject
     * 如果开启字符串模式, 则参数均以字符串形式绑定
     */
    try
    {
      int   i =0;
      for (Object x : paramz)
      {
            i ++;
        if (x == null)
        {
          ps.setString(i, "");
        }
        else
        {
          ps.setObject(i,  x);
        }
      }
    }
    catch (SQLException ex)
    {
      throw new HongsException(0x1032, ex);
    }

    return ps;
  }

  /**
   * 当需要在prepareStatement时设定参数, 可重载该方法
   * <p>
   * 如需要单条获取, 可使用以下代码:
   * <code>
   * ps = this.connection.prepareStatement(sql,
   *        ResultSet.TYPE_FORWARD_ONLY,
   *        ResultSet.CONCUR_READ_ONLY);
   * ps.setFetchSize(Integer.MIN_VALUE);
   * </code>
   * 异常代码为: 0x1031
   * </p>
   * @param sql
   * @return PreparedStatement对象
   * @throws HongsException
   */
  public PreparedStatement prepareStatement(String sql)
    throws HongsException
  {
    PreparedStatement ps;

    try
    {
      ps = this.connection.prepareStatement(sql);
    }
    catch (SQLException ex)
    {
      throw new HongsException(0x1031, ex);
    }

    return ps;
  }

  /**
   * 当需要在createStatement时设定参数, 可重载该方法
   * <p>
   * 如需要单条获取, 可使用以下代码:
   * <code>
   * ps = this.connection.createStatement(
   *        ResultSet.TYPE_FORWARD_ONLY,
   *        ResultSet.CONCUR_READ_ONLY);
   * ps.setFetchSize(Integer.MIN_VALUE);
   * </code>
   * 异常代码为: 0x1031
   * </p>
   * @return Statement对象
   * @throws HongsException
   */
  public Statement createStatemenet()
    throws HongsException
  {
    Statement ps;

    try
    {
      ps = this.connection.createStatement();
    }
    catch (SQLException ex)
    {
      throw new HongsException(0x1031, ex);
    }

    return ps;
  }

  /**
   * 取消Statement
   * @param ps
   * @throws HongsException
   */
  public void cancelStatement(Statement ps)
    throws HongsException
  {
    try
    {
      if (ps == null || ps.isClosed()) return;
      ps.cancel();
    }
    catch (SQLException ex)
    {
      throw new app.hongs.HongsException(0x102b, ex);
    }
  }

  /**
   * 关闭Statement
   * @param ps
   * @throws HongsException
   */
  public void closeStatement(Statement ps)
    throws HongsException
  {
    try
    {
      if (ps == null || ps.isClosed()) return;
      ps.close();
    }
    catch (SQLException ex)
    {
      throw new app.hongs.HongsException(0x102d, ex);
    }
  }

  /**
   * 关闭ResultSet
   * @param rs
   * @throws HongsException
   */
  public void closeResultSet(ResultSet rs)
    throws HongsException
  {
    try
    {
      if (rs == null || rs.isClosed()) return;
      rs.close();
    }
    catch (SQLException ex)
    {
      throw new app.hongs.HongsException(0x102f, ex);
    }
  }

  /**
   * 获取列名集合
   * @param rs
   * @return 列名集合
   * @throws HongsException
   */
  public String[] getColLabels(ResultSet rs)
    throws HongsException
  {
    try
    {
      ResultSetMetaData md = rs.getMetaData();
      int c = md.getColumnCount();
      String[] ls = new String[c];
      for (int i = 0; i < c; i ++)
      {
        ls[i] = md.getColumnLabel(i + 1);
      }
      return ls;
    }
    catch (SQLException ex)
    {
      throw new app.hongs.HongsException(0x1034, ex);
    }
  }

  /**
   * 获取行值集合
   * @param rs
   * @param labels
   * @return 行值集合
   * @throws HongsException
   */
  public Map<String, Object> getRowValues(ResultSet rs, String... labels)
    throws HongsException
  {
    try
    {
      /**
       * 如果开启字符串模式,
       * 则仅获取返回字符串;
       * 否则获取其对象形式.
       */
      Map<String, Object> values = new HashMap();
      if (IN_OBJECT_MODE)
      {
        for (int i = 0; i < labels.length; i ++)
        {
          values.put(labels[i], rs.getObject(i + 1));
        }
      }
      else
      {
        for (int i = 0; i < labels.length; i ++)
        {
          values.put(labels[i], rs.getString(i + 1));
        }
      }
      return values;
    }
    catch (SQLException ex)
    {
      throw new app.hongs.HongsException(0x1036, ex);
    }
  }

  //** 查询语句 **/

  /**
   * 查询方法
   * @param sql
   * @param params
   * @return 查询结果
   * @throws HongsException
   */
  public FetchNext query(String sql, Object... params)
    throws HongsException
  {
    this.open();

    if (Core.IN_DEBUG_MODE)
    {
      StringBuilder sb = new StringBuilder(sql);
      List      paramz = new ArrayList(Arrays.asList(params));
      DB.checkSQLParams(sb, paramz);
      DB.mergeSQLParams(sb, paramz);
      app.hongs.CoreLogger.debug("INFO(DB.query): " + sb.toString());
    }

    PreparedStatement ps = this.prepareStatement(sql, params);
            ResultSet rs;

    try
    {
      rs = ps.executeQuery();
    }
    catch (SQLException ex )
    {
      throw new app.hongs.HongsException(0x1041, ex);
    }

    return new FetchNext(this, ps, rs);
  }

  /**
   * 获取查询的全部数据
   * <p>会自动执行closeStatement和closeResultSet</p>
   * @param sql
   * @param params
   * @return 全部数据
   * @throws app.hongs.HongsException
   */
  public List<Map<String, Object>> fetchAll(String sql, Object... params)
    throws HongsException
  {
    List<Map<String, Object>> rows = new ArrayList();
         Map<String, Object>  row;

    FetchNext rs  = this.query(sql, params);
    while ( ( row = rs.fetch( ) ) != null )
    {
      rows.add(row);
    }

    return rows;
  }

  /**
   * 获取查询的单条数据
   * <p>注: 调fetchAll实现</p>
   * @param sql
   * @param params
   * @return 单条数据
   * @throws app.hongs.HongsException
   */
  public Map<String, Object> fetchOne(String sql, Object... params)
    throws HongsException
  {
    if (Pattern.compile("^SELECT\\s+.*\\s+(?!LIMIT[\\s\\d,]+)$",
                Pattern.CASE_INSENSITIVE)
               .matcher( sql ).matches())
    {
      sql += " LIMIT 1";
    }

    List<Map<String, Object>> rows = this.fetchAll(sql, params);

    if (!rows.isEmpty())
    {
      return rows.get(0);
    }
    else
    {
      return new HashMap();
    }
  }

  /**
   * 采用查询体获取全部数据
   * <p>注: 调fetchAll实现</p>
   * @param more
   * @return 全部数据
   * @throws app.hongs.HongsException
   */
  public List fetchMore(FetchMore more)
    throws HongsException
  {
    return this.fetchAll(more.getSQL(), more.getParams());
  }

  /**
   * 采用查询体获取单条数据
   * <p>注: 调fetchMore实现</p>
   * @param less
   * @return 单条数据
   * @throws app.hongs.HongsException
   */
  public Map fetchLess(FetchMore less)
    throws HongsException
  {
    List<Map<String, Object>> rows = this.fetchMore(less.limit(1));

    if (!rows.isEmpty())
    {
      return rows.get(0);
    }
    else
    {
      return new HashMap();
    }
  }

  /** 执行语句 **/

  /**
   * 执行方法
   * @param sql
   * @param params
   * @return 成功或失败
   * @throws HongsException
   */
  public boolean execute(String sql, Object... params)
    throws HongsException
  {
    this.open();

    if (Core.IN_DEBUG_MODE)
    {
      StringBuilder sb = new StringBuilder(sql);
      List      paramz = new ArrayList(Arrays.asList(params));
      DB.checkSQLParams(sb, paramz);
      DB.mergeSQLParams(sb, paramz);
      app.hongs.CoreLogger.debug("INFO(DB.execute): " + sb.toString());
    }

    /**
     * 获取PreparedStatement对象
     */
    PreparedStatement ps = this.prepareStatement(sql, params);

    try
    {
      return ps.execute();
    }
    catch (SQLException ex)
    {
      throw new app.hongs.HongsException(0x1045, ex);
    }
    finally
    {
      this.closeStatement(ps);
    }
  }

  /**
   * 更新方法
   * @param sql
   * @param params
   * @return 更新条数
   * @throws HongsException
   */
  public int update(String sql, Object... params)
    throws HongsException
  {
    this.open();

    if (Core.IN_DEBUG_MODE)
    {
      StringBuilder sb = new StringBuilder(sql);
      List      paramz = new ArrayList(Arrays.asList(params));
      DB.checkSQLParams(sb, paramz);
      DB.mergeSQLParams(sb, paramz);
      app.hongs.CoreLogger.debug("INFO(DB.update): " + sb.toString());
    }

    PreparedStatement ps = this.prepareStatement(sql, params);

    try
    {
      return ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      throw new app.hongs.HongsException(0x1049, ex);
    }
    finally
    {
      this.closeStatement(ps);
    }
  }

  /**
   * 更新记录
   * <p>注: 调用update(sql, params...)实现</p>
   * @param table
   * @param values
   * @param where
   * @param params
   * @return 更新条数
   * @throws app.hongs.HongsException
   */
  public int update(String table, Map<String, Object> values, String where, Object... params)
    throws HongsException
  {
    if (values == null || values.isEmpty())
    {
      throw new app.hongs.HongsException(0x1048, "Update values can not be empty.");
    }

    /** 组织语言 **/

    String sql = "UPDATE `" + Str.escape(table, "`") + "` SET ";
    List params2 = new ArrayList();

    Iterator it = values.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry)it.next();
      String field = (String)entry.getKey();
      params2.add((Object)entry.getValue());

      sql += "`" + Str.escape(field, "`") + "` = ?, ";
    }

    sql = sql.substring(0, sql.length()  - 2);

    if (where != null && where.length() != 0)
    {
      sql += " WHERE " + where;
    }

    if (params.length > 0)
    {
      params2.addAll(Arrays.asList(params));
    }

    /** 执行更新 **/

    return this.update(sql, params2.toArray());
  }

  /**
   * 添加记录
   * <p>注: 调用update(sql, params...)实现</p>
   * @param table
   * @param values
   * @return 插入条数
   * @throws app.hongs.HongsException
   */
  public int insert(String table, Map<String, Object> values)
    throws HongsException
  {
    if (values == null || values.isEmpty())
    {
      throw new app.hongs.HongsException(0x1046, "Insert values can not be empty.");
    }

    /** 组织语句 **/

    String sql = "INSERT INTO `" + Str.escape(table, "`") + "`";
    List params2 = new ArrayList();
    String fs = "", vs = "";

    Iterator it = values.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry)it.next();
      String field = (String)entry.getKey();
      params2.add((Object)entry.getValue());

      fs += "`" + Str.escape(field, "`") + "`, ";
      vs += "?, ";
    }

    sql += " (" + fs.substring(0, fs.length() - 2) + ")";
    sql += " VALUES";
    sql += " (" + vs.substring(0, vs.length() - 2) + ")";

    /** 执行更新 **/

    return this.update(sql, params2.toArray());
  }

  /**
   * 删除记录
   * <p>注: 调用update(sql, params...)实现</p>
   * @param table
   * @param where
   * @param params
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int delete(String table, String where, Object... params)
    throws HongsException
  {
    /** 组织语句 **/

    String sql = "DELETE FROM `" + Str.escape(table, "`") + "`";

    if (where != null && where.length() != 0)
    {
      sql += " WHERE " + where;
    }

    /** 执行更新 **/

    return this.update(sql, params);
  }

  //** 静态工具 **/

  /**
   * 引用字段名
   * @param field
   * @return 引用后的串
   */
  public static String quoteField(String field)
  {
    return "`" + Str.escape(field, "`") + "`";
  }

  /**
   * 引用值
   * @param value
   * @return 引用后的串
   */
  public static String quoteValue(String value)
  {
    return "'" + Str.escape(value, "'") + "'";
  }

  /**
   * 格式化SQL字段名
   * @param sql
   * @return 格式好的SQL
   */
  public static String formatSQLFields(String sql)
  {
    return sql.replaceAll("(^|[ ,\\(])([^ ,`\\(\\)]+)\\.([^ ,`\\(\\)]+)([ ,\\)]|$)", "$1`$2`.`$3`$4")
              .replaceAll("([^ ,`'\\(\\)]+) +AS +", "`$1` AS ")
              .replaceAll(" +AS +([^ ,`'\\(\\)]+)", " AS `$1`")
              .replaceAll("`\\*`", "*");
  }

  /**
   * 检查SQL数据项
   * @param sql
   * @param params
   * @throws app.hongs.HongsException
   */
  public static void checkSQLParams(StringBuilder sql, List params)
    throws HongsException
  {
    if (params == null)
    {
      params = new ArrayList();
    }

    int pos = 0;
    int num = 0;

    while ((pos = sql.indexOf("?", pos)) != -1)
    {
      if (num >= params.size())
      {
        break;
      }

      /**
       * 如果参数是数组或List
       * 则将其全部转化为Set
       * 以供继续后面的处理
       */
      Object obj = params.get(num);
      if (obj != null && obj.getClass().isArray())
      {
        obj = new HashSet(Arrays.asList((Object[])obj));
      }
      else
      if (obj instanceof List)
      {
        obj = new HashSet((List)obj);
      }
      else
      if (obj instanceof Map)
      {
        obj = new HashSet(((Map)obj).values());
      }

      /**
       * 如果参数是Set,
       * 则视为"SQL IN"语句,
       * 将在当前问号后补充足量的问号,
       * 并将参数补充到当前参数列表中.
       */
      if (obj instanceof Set)
      {
        Set set = (Set)obj;
        int off =      num;

        // 如果为空, 则补一个null
        if (set.isEmpty())
        {
            set.add(null);
        }

        // 从第二个参数开始补充问号
        for (int i = 1; i < set.size(); i ++)
        {
          sql.insert(pos + 1, ",?");
          pos += 2;
          num += 1;
        }

        // 平铺到参数列表中
        params.remove(off);
        params.addAll(off, set);
      }

      pos += 1;
      num += 1;
    }

    if (num != params.size())
    {
      throw new HongsException(0x1051,
        "The number of '?' and the number of parameters are inconsistent."
        + " ?s: " + num + " params: " + params.size() + " SQL: " + sql);
    }
  }

  /**
   * 绑定SQL数据项
   * 调用本方法前务必先调用内checkSQLParams
   * @param sql
   * @param params
   * @throws app.hongs.HongsException
   */
  public static void mergeSQLParams(StringBuilder sql, List params)
    throws HongsException
  {
    if (params == null)
    {
      params = new ArrayList();
    }

    int pos = 0;
    int num = 0;

    /**
     * 填充参数
     */
    while ((pos = sql.indexOf("?", pos)) != -1)
    {
      if (num >= params.size())
      {
        break;
      }

      /**
       * 如果参数是NULL,
       * 则直接加入NULL;
       * 如果参数是数字,
       * 则直接加入数字;
       * 如果参数是其他类型,
       * 则转换成字符串并加引号.
       */

      Object obj = params.get(num);

      String str;
      if (obj == null)
      {
        str = "NULL";
      }
      else
      if (obj instanceof Number)
      {
        str = obj.toString();
      }
      else
      {
        str = obj.toString();
        str = DB.quoteValue(str);
      }

      sql.replace(pos, pos + 1, str);

      pos += str.length() - 1;
      num += 1;
    }

    if (num != params.size())
    {
      throw new HongsException(0x1051,
        "The number of '?' and the number of parameters are inconsistent."
        + " ?s: " + num + " params: " + params.size() + " SQL: " + sql);
    }
  }

  //** 构造工厂 **/

  public static Map<String, DB> instances;

  /**
   * 获取默认数据库对象
   * <b>注意:</b>
   * <p>
   * 如果指定数据库配置中有指定dbClass, 务必添加方法:
   * </p>
   * <pre>
   * public static XxxDB getInstance()
   *    throws HongsException
   * {
   *    return new XxxDB();
   * }
   * </pre>
   * @return 默认DB对象
   * @throws app.hongs.HongsException
   */
  public static DB getInstance()
    throws HongsException
  {
    return DB.getInstance("default");
  }

  /**
   * 获取指定数据库对象
   * <b>注意:</b>
   * <p>
   * 如果指定数据库配置中有指定dbClass, 务必添加方法:
   * </p>
   * <pre>
   * public static XxxDB getInstance()
   *    throws HongsException
   * {
   *    return new XxxDB();
   * }
   * </pre>
   * @param dbName
   * @return 指定DB对象
   * @throws app.hongs.HongsException
   */
  public static DB getInstance(String dbName)
    throws HongsException
  {
    if (DB.instances != null
    &&  DB.instances.containsKey(dbName))
    {
      return    DB.instances.get(dbName);
    }

    /**
     * 如果存在dbClass描述则调用对应类来获取实例
     */

    DB       db ;
    DBConfig cf = (new DBConfig(dbName));

    if (cf.dbClass != null && cf.dbClass.length() != 0)
    {
      db = (DB)Core.getInstance(cf.dbClass);
    }
    else
    {
      db = new DB(cf);
      Core.getInstance().put("__DB__." + dbName, db);
    }

    /**
     * 如有设置dbName的单次加载则将其放入静态映射
     */

    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    if (conf.getProperty("core.load.db."+dbName+".once", false))
    {
      if (DB.instances == null)
      {
          DB.instances = new HashMap();
      }
          DB.instances.put(dbName, db);
    }

    return db;
  }

  public static DB getInstanceByDriver(String drv, String url, Properties info)
  throws HongsException {
      Map config = new HashMap();
      Map driver = new HashMap();

      config.put("driver", driver);
      driver.put("drv" , drv );
      driver.put("url" , url );
      driver.put("info", info);

      return new DB(config);
  }

  public static DB getInstanceByDriver(String drv, String url)
  throws HongsException {
      return getInstanceByDriver(drv, url, new Properties());
  }

  public static DB getInstanceBySource(String name, Properties info)
  throws HongsException {
      Map config = new HashMap();
      Map source = new HashMap();

      config.put("source", source);
      source.put("name", name);
      source.put("info", info);

      return new DB(config);
  }

  public static DB getInstanceBySource(String name)
  throws HongsException {
      return getInstanceBySource(name, new Properties());
  }

  private DB(Map cf)
    throws HongsException
  {
    if (cf == null) cf = new HashMap();

    this.name         = "";
    this.driver       = (Map) cf.get("driver");
    this.source       = (Map) cf.get("source");
    this.tableClass   = "";
    this.tablePrefix  = "";
    this.tableSuffix  = "";
    this.tableConfigs = new  HashMap( );
    this.tableObjects = new  HashMap( );

    this.connection   = null;
  }

}
