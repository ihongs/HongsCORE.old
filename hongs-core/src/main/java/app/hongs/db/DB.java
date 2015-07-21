package app.hongs.db;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.dl.ITrnsct;
import app.hongs.util.Synt;
import app.hongs.util.Text;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

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
 * core.load.db.[dbName].once 为true则仅加载一次
 * </pre>
 *
 * <h3>异常代码:</h3>
 * <pre>
 * 区间: 0x1020~0x105f
 *
 * 0x1021  找不到外部数据源配置
 * 0x1022  连接外部数据源失败
 * 0x1023  找不到内部数据源配置
 * 0x1024  连接内部数据源失败
 * 0x1025  找不到数据源配置
 * 0x1026  设置自动提交失败
 *
 * 0x1031  开启Connection失败
 * 0x1032  关闭Connection失败
 * 0x1033  取消Statement失败
 * 0x1034  关闭Statement失败
 * 0x1035  关闭ResultSet失败
 *
 * 0x1039  找不到表配置
 * 0x103a  找不到表对应的类
 * 0x103b  无法获取表构造器
 * 0x103c  无法获取表实例
 * 0x103d  找不到模型对应的类
 * 0x103e  无法获取模型构造器
 * 0x103f  无法获取模型实例
 *
 * 0x1041  构建语句失败
 * 0x1042  绑定参数失败
 * 0x1043  获取列名失败
 * 0x1044  获取行数据失败
 *
 * 0x1047  查询语句失败
 * 0x1048  获取查询结果失败
 *
 * 0x104a  执行语句失败
 * 0x104b  插入的值不能为空
 * 0x104c  执行插入语句失败
 * 0x104d  更新的值不能为空
 * 0x104e  执行更新语句失败
 * 0x104f  执行删除语句失败
 *
 * 0x1051  参数的个数与语句中的插入标识数不符
 * </pre>
 *
 * @author Hongs
 */
public class DB
  implements Core.Destroy, ITrnsct
{

  /**
   * 是否为对象模式(即获取的是对象)
   * 注意: 仅当使用 DB.getInstance 才根据环境设置, new DB 的都是 false
   */
  public boolean IN_OBJECT_MODE;

  /**
   * 是否为事务模式(即不会自动提交)
   * 注意: 仅当使用 DB.getInstance 才根据环境设置, new DB 的都是 false
   */
  public boolean IN_TRNSCT_MODE;

  /**
   * 库名
   */
  public String name;

  /**
   * 表类
   */
  protected String tableClass;

  /**
   * 模型类
   */
  protected String modelClass;

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
  protected Map<String, Map  > tableConfigs;

  /**
   * 表对象
   */
  protected Map<String, Table> tableObjects;

  /**
   * 模型对象
   */
  protected Map<String, Model> modelObjects;

  private Map           source;
  private Map           origin;
  private Connection    connection;

  private static Map<String, ComboPooledDataSource> sourcePool = new HashMap();
  private static ReadWriteLock  sourceLock  =  new  ReentrantReadWriteLock(  );

  protected DB()
    throws HongsException
  {
    this.name         = "";
    this.source       = new HashMap();
    this.origin       = new HashMap();
    this.tableClass   = Table.class.getName();
    this.modelClass   = Model.class.getName();
    this.tablePrefix  = "";
    this.tableSuffix  = "";
    this.tableConfigs = new HashMap();
    this.tableObjects = new HashMap();
    this.modelObjects = new HashMap();
  }

  public DB(DBConfig dbConf)
    throws HongsException
  {
    this.name         = dbConf.name;
    this.source       = dbConf.source;
    this.origin       = dbConf.origin;
    this.tableClass   = dbConf.tableClass;
    this.modelClass   = dbConf.modelClass;
    this.tablePrefix  = dbConf.tablePrefix;
    this.tableSuffix  = dbConf.tableSuffix;
    this.tableConfigs = dbConf.tableConfigs;
    this.tableObjects = new HashMap();
    this.modelObjects = new HashMap();
  }

  public Connection connect()
    throws HongsException
  {
    TOP: do
    {

    try
    {
      if (this.connection != null
      && !this.connection.isClosed())
      {
        break;
      }
    }
    catch (SQLException ex)
    {
      throw new HongsException(0x1031, ex);
    }

    Exception ez = null;

    /** 使用外部数据源 **/

    do
    {
      if (origin == null || origin.isEmpty( ))
      {
        break;
      }

      if (origin.containsKey("name") == false)
      {
        throw new HongsException(0x1021, "Can not find name in origin");
      }

      String comp = "java:comp/env";
      String namc = (String)origin.get("name");
      Properties info = (Properties)origin.get("info");

      Context ct;
      DataSource ds;
      InitialContext ic;
      try
      {
        ic = new InitialContext( );
        ct = (Context)ic.lookup(comp);
        ds = (DataSource)ct.lookup(namc);
      }
      catch (NamingException ex)
      {
        ez=ex;
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
                 info.getProperty(  "user"  ),
                 info.getProperty("password"));
        }
        this.connection.setAutoCommit( false );

        if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG))
        {
          CoreLogger.debug("Connect to database(origin mode): "+name);
        }
      }
      catch (SQLException ex)
      {
        throw new app.hongs.HongsException(0x1022, ex);
      }

      break TOP;
    }
    while (false);

    /** 使用内部数据源 **/

    do
    {
      if (source == null || source.isEmpty())
      {
        break;
      }

      if (!source.containsKey("drv"))
      {
        throw new app.hongs.HongsException(0x1023, "Can not find drv in source");
      }

      if (!source.containsKey("url"))
      {
        throw new app.hongs.HongsException(0x1023, "Can not find url in source");
      }

      String drv = (String)source.get("drv");
      String url = (String)source.get("url");
      Properties info = (Properties)source.get("info");

      // SQLite 数据路径处理
      if (url.startsWith("jdbc:sqlite:"))
      {
        String uri  =  url.substring (12);
        if (! new File(uri).isAbsolute())
        {
          uri = CoreConfig.getInstance().getProperty("core.sqlite.datapath", "${VARS_PATH}/sqlite") +"/"+ uri;
        }

        // 注入特定路径
        Map inj = new HashMap();
        inj.put("CORE_PATH", Core.CORE_PATH);
        inj.put("CONF_PATH", Core.CONF_PATH);
        inj.put("VARS_PATH", Core.VARS_PATH);
        inj.put("TMPS_PATH", Core.VARS_PATH);
        uri = Text.inject(uri, inj);
        url = "jdbc:sqlite:" + uri ;

        if (! new File(uri).getParentFile().exists())
        {
          /**/new File(uri).getParentFile().mkdirs();
        }
      }

      try
      {
        /*
        Class.forName(drv);

        if (info.isEmpty())
        {
          this.connection = DriverManager.getConnection(url);
        }
        else
        {
          this.connection = DriverManager.getConnection(url, info);
        }
        */

        String namc = drv+" "+url;
        ComboPooledDataSource pool;
        sourceLock.readLock( ).lock();
        try
        {
          pool = sourcePool.get(namc);
        }
        finally
        {
          sourceLock.readLock().unlock();
        }

        if (pool == null)
        {
          sourceLock.writeLock( ).lock();
          try
          {
            pool = new ComboPooledDataSource();
            sourcePool.put( namc, pool );
            pool.setDriverClass(drv);
            pool.setJdbcUrl/**/(url);
//          pool.setProperties(info); // 无效, 只能用下面的方式来设置

            if (info.containsKey("user"    )) {
              pool.setUser    (info.getProperty("user"    ));
            }
            if (info.containsKey("password")) {
              pool.setPassword(info.getProperty("password"));
            }
            if (info.containsKey("maxIdleTime")) {
              pool.setMaxIdleTime(Integer.parseInt(info.getProperty("maxIdleTime")));
            }
            if (info.containsKey("maxPoolSize")) {
              pool.setMaxPoolSize(Integer.parseInt(info.getProperty("maxPoolSize")));
            }
            if (info.containsKey("minPoolSize")) {
              pool.setMinPoolSize(Integer.parseInt(info.getProperty("minPoolSize")));
            }
            if (info.containsKey("initialPoolSize")) {
              pool.setInitialPoolSize(Integer.parseInt(info.getProperty("initialPoolSize")));
            }

            if (info.containsKey("maxStatements"  )) {
              pool.setMaxStatements  (Integer.parseInt(info.getProperty("maxStatements"  )));
            }
            if (info.containsKey("maxStatementsPerConnection")) {
              pool.setMaxStatementsPerConnection(Integer.parseInt(info.getProperty("maxStatementsPerConnection")));
            }

            if (info.containsKey("checkoutTimeout")) {
              pool.setCheckoutTimeout(Integer.parseInt(info.getProperty("checkoutTimeout")));
            }
            if (info.containsKey("idleConnectionTestPeriod"  )) {
              pool.setIdleConnectionTestPeriod  (Integer.parseInt(info.getProperty("idleConnectionTestPeriod"  )));
            }

            if (info.containsKey("numHelperThreads"    )) {
              pool.setNumHelperThreads    (Integer.parseInt(info.getProperty("numHelperThreads"    )));
            }
            if (info.containsKey("acquireIncrement"    )) {
              pool.setAcquireIncrement    (Integer.parseInt(info.getProperty("acquireIncrement"    )));
            }
            if (info.containsKey("acquireRetryDelay"   )) {
              pool.setAcquireRetryDelay   (Integer.parseInt(info.getProperty("acquireRetryDelay"   )));
            }
            if (info.containsKey("acquireRetryAttempts")) {
              pool.setAcquireRetryAttempts(Integer.parseInt(info.getProperty("acquireRetryAttempts")));
            }

            if (info.containsKey("testConnectionOnCheckin" )) {
              pool.setTestConnectionOnCheckin (Boolean.parseBoolean(info.getProperty("testConnectionOnCheckin" )));
            }
            if (info.containsKey("testConnectionOnCheckout")) {
              pool.setTestConnectionOnCheckout(Boolean.parseBoolean(info.getProperty("testConnectionOnCheckout")));
            }
          }
          finally
          {
            sourceLock.writeLock().unlock();
          }
        }

        this.connection = pool.getConnection();
        this.connection.setAutoCommit( false );

        if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG))
        {
          CoreLogger.debug("Connect to database(source mode): "+drv+" "+url);
        }
      }
      catch (PropertyVetoException ex)
      {
        throw new app.hongs.HongsException(0x1024, ex);
      }
      catch (SQLException ex)
      {
        throw new app.hongs.HongsException(0x1024, ex);
      }

      break TOP;
    }
    while (false);

    if (ez !=null)
    {
      throw new HongsException(0x1025, ez);
    }
    else
    {
      throw new HongsException(0x1025, "Can not find source or origin");
    }

    } while (false);

    /** 初始化设置 **/

    // 自动提交设置
    try
    {
      this.connection.setAutoCommit(!this.IN_TRNSCT_MODE);
    }
    catch (SQLException ex )
    {
      throw  new  app.hongs.HongsException( 0x1026 , ex );
    }

    return this.connection;
  }

  @Override
  public void destroy()
    throws Throwable
  {
    try
    {
      if (this.connection == null
      ||  this.connection.isClosed())
      {
        return;
      }

      // 默认退出时提交
      if(this.IN_TRNSCT_MODE)
      {
        try
        {
          this.commit();
        }
        catch (Error e)
        {
          this.rolbak();
          throw e;
        }
      }

      if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG))
      {
        CoreLogger.debug("Close database connection, URL: "
          + this.connection.getMetaData().getURL());
      }

      this.connection.close();
      this.connection = null;
    }
    catch (SQLException ex)
    {
      throw new Error(new HongsException(0x1032, ex));
    }
  }

  /**
   * 事务:开始
   */
  @Override
  public void trnsct()
  {
    this.IN_TRNSCT_MODE = true ;
  }

  /**
   * 事务:提交
   */
  @Override
  public void commit()
  {
    if (IN_TRNSCT_MODE) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.commit(  );
            }
        } catch (SQLException ex) {
          throw new HongsError(0x44, ex);
        }
        IN_TRNSCT_MODE = Synt.declare(Core.getInstance().got( "__IN_TRNSCT_MODE__" ), false);
    }
  }

  /**
   * 事务:回滚
   */
  @Override
  public void rolbak()
  {
    if (IN_TRNSCT_MODE) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException ex) {
          throw new HongsError(0x44, ex);
        }
        IN_TRNSCT_MODE = Synt.declare(Core.getInstance().got( "__IN_TRNSCT_MODE__" ), false);
    }
  }

  /**
   * 获得全部的关联表名
   * @return 关联名集合
   */
  public Set<String> getTableNames()
  {
    return this.tableConfigs.keySet();
  }

  /**
   * 通过表名获取表对象
   * 表名可以为"库名.表名"
   * @param tableName
   * @return 指定表对象
   * @throws app.hongs.HongsException
   */
  public Table getTable(String tableName)
    throws HongsException
  {
    /**
     * 表名可以是"数据库.表名"
     * 用于引用另一个库中的表
     */
    int pos = tableName.indexOf('.');
    if (pos > 0)
    {
      String db = tableName.substring(0,  pos);
          tableName = tableName.substring(pos + 1);
      return DB.getInstance(db).getTable(tableName);
    }

    if ( this.tableObjects.containsKey(tableName))
    {
      return this.tableObjects.get(tableName);
    }

    if (!this.tableConfigs.containsKey(tableName))
    {
      throw new app.hongs.HongsException(0x1039, "Can not find config for table '"+tableName+"'.");
    }

    /**
     * 读取库指定的tableClass
     * 读取表对应的tableConfig
     */
    Map<String, String> tcfg = this.tableConfigs.get(tableName);
    //this.tableConfigs.remove(tableName);
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
      this.tableObjects.put(tableName, tobj);
      return tobj;
    }

    if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG))
    {
      app.hongs.CoreLogger.debug(
          "DB: tableClass("+tcls+") for table("+tableName+") has been defined, try to get it");
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
      throw new app.hongs.HongsException(0x103a, ex);
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
      throw new app.hongs.HongsException(0x103b, ex);
    }
    catch (SecurityException ex)
    {
      throw new app.hongs.HongsException(0x103b, ex);
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
      throw new app.hongs.HongsException(0x103c, ex);
    }
    catch (IllegalAccessException ex)
    {
      throw new app.hongs.HongsException(0x103c, ex);
    }
    catch (IllegalArgumentException ex)
    {
      throw new app.hongs.HongsException(0x103c, ex);
    }
    catch (InvocationTargetException ex)
    {
      throw new app.hongs.HongsException(0x103c, ex);
    }

    this.tableObjects.put(tableName, tobj);
    return tobj;
  }

  /**
   * 通过表名获取表模型
   * 表名可以为"库名.表名"
   * @param tableName
   * @return 指定表模型
   * @throws app.hongs.HongsException
   */
  public Model getModel(String tableName)
    throws HongsException
  {
    /**
     * 表名可以是"数据库.表名"
     * 用于引用另一个库中的表
     */
    int pos = tableName.indexOf('.');
    if (pos > 0)
    {
      String db = tableName.substring(0,  pos);
          tableName = tableName.substring(pos + 1);
      return DB.getInstance(db).getModel(tableName);
    }

    if ( this.modelObjects.containsKey(tableName))
    {
      return this.modelObjects.get(tableName);
    }

    if (!this.tableConfigs.containsKey(tableName))
    {
      throw new app.hongs.HongsException(0x1039, "Can not find config for table '"+tableName+"'.");
    }

    /**
     * 读取库指定的modelClass
     */
    Map<String, String> tcfg = this.tableConfigs.get(tableName);
    //this.tableConfigs.remove(tableName);
    String mcls = this.modelClass;

    /**
     * 就近原则:
     * 如果表配置中有设置model则采用表配置中的
     */
    if (tcfg.containsKey("model"))
    {
      mcls = tcfg.get("model");
    }

    /**
     * 如果class为空则直接使用默认的Table
     */
    if (mcls == null || mcls.length() == 0)
    {
      Model mobj = new Model(this.getTable(tableName));
      this.modelObjects.put(tableName, mobj);
      return mobj;
    }

    if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG))
    {
      app.hongs.CoreLogger.debug(
          "DB: modelClass("+mcls+") for table("+tableName+") has been defined, try to get it");
    }

    /**
     * 获取指定的Table类
     */
    Class cls;
    try
    {
      cls = Class.forName(mcls);
    }
    catch (ClassNotFoundException ex)
    {
      throw new app.hongs.HongsException(0x103d, ex);
    }

    /**
     * 获取构造器
     */
    Constructor cst;
    try
    {
      cst = cls.getConstructor(new Class[]{Table.class});
    }
    catch (NoSuchMethodException ex)
    {
      throw new app.hongs.HongsException(0x103e, ex);
    }
    catch (SecurityException ex)
    {
      throw new app.hongs.HongsException(0x103e, ex);
    }

    /**
     * 获取表实例
     */
    Model mobj;
    try
    {
      mobj = (Model)cst.newInstance(new Object[]{this.getTable(tableName)});
    }
    catch (InstantiationException ex)
    {
      throw new app.hongs.HongsException(0x103f, ex);
    }
    catch (IllegalAccessException ex)
    {
      throw new app.hongs.HongsException(0x103f, ex);
    }
    catch (IllegalArgumentException ex)
    {
      throw new app.hongs.HongsException(0x103f, ex);
    }
    catch (InvocationTargetException ex)
    {
      throw new app.hongs.HongsException(0x103f, ex);
    }

    this.modelObjects.put(tableName, mobj);
    return mobj;
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
      int i = 0;
      for (Object x : paramz)
      {
        ps.setObject(++ i, x);
      }
    }
    catch (SQLException ex)
    {
      throw new HongsException(0x1042, ex);
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
   * 异常代码为: 0x1041
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
      throw new HongsException(0x1041, ex);
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
   * 异常代码为: 0x1041
   * </p>
   * @return Statement对象
   * @throws HongsException
   */
  public Statement createStatement()
    throws HongsException
  {
    Statement ps;

    try
    {
      ps = this.connection.createStatement();
    }
    catch (SQLException ex)
    {
      throw new HongsException(0x1041, ex);
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
      throw new app.hongs.HongsException(0x1033, ex);
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
      throw new app.hongs.HongsException(0x1034, ex);
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
      throw new app.hongs.HongsException(0x1035, ex);
    }
  }

  //** 查询语句 **/

  /**
   * 分页方法
   * 已改为使用 JDBC 的 setFetchSize,setMaxRows,absolute 等方法;
   * 另, 请不要在 update,delete 中使用 limit 至少 MySQL 是可以的,
   * 您要更新/删除的记录应该是明确的, 应该能够通过 where 做出限定的.
   * @deprecated
   * @param sql
   * @param start
   * @param limit
   * @return
   */
  public String limit(String sql, int start, int limit) {
      try {
          Connection con = connect();
          String nam = con.getMetaData().getDatabaseProductName();
          if ("MySQL".equals(nam)) {
              sql += " LIMIT " + start + "," + limit;
          } else if ("PostgreSQL".equals(nam)) {
              sql += " LIMIT " + limit + " OFFSET " + start;
          } else if ("Oracle".equals(nam)) {
              sql = "SELECT * FROM (" + sql + ") WHERE rno>" + (start - 1) + " AND rno<" + (start + limit);
//        } else if ("SQLServer".equals(nam)) {
//            sql = "SELECT * FROM (" + sql + ") AS __table__ WHERE __table__.rownum>" + (start - 1) + " AND rno<" + (start + limit);
          } else {
              throw new HongsError(0x10, "Limit not support " + nam);
          }
      } catch (HongsException ex) {
          throw new HongsError(0x10, ex);
      } catch (SQLException ex) {
          throw new HongsError(0x10, ex);
      }
      return sql;
  }

  /**
   * 查询方法
   * @param sql
   * @param start
   * @param limit
   * @param params
   * @return 查询结果
   * @throws HongsException
   */
  public FetchNext query(String sql, int start, int limit, Object... params)
    throws HongsException
  {
    this.connect();

    if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG))
    {
      StringBuilder sb = new StringBuilder(sql);
      List      paramz = new ArrayList(Arrays.asList(params));
      DB.checkSQLParams(sb, paramz);
      DB.mergeSQLParams(sb, paramz);
      app.hongs.CoreLogger.debug("DB.query: " + sb.toString());
    }

    PreparedStatement ps = this.prepareStatement(sql, params);
            ResultSet rs;

    try
    {
      if (limit > 0)
      {
        ps.setFetchSize   (   limit);
        ps.setMaxRows(start + limit);
      }
      rs = ps.executeQuery();
      if (start > 0)
      {
        rs. absolute (start);
      }
    }
    catch (SQLException ex )
    {
      throw new app.hongs.HongsException(0x1047, ex);
    }

    return new FetchNext(this, ps, rs);
  }

  /**
   * 获取查询的全部数据
   * <p>会自动执行closeStatement和closeResultSet</p>
   * @param sql
   * @param start
   * @param limit
   * @param params
   * @return 全部数据
   * @throws app.hongs.HongsException
   */
  public List<Map<String, Object>> fetch(String sql, int start, int limit, Object... params)
    throws HongsException
  {
    List<Map<String, Object>> rows = new ArrayList();
         Map<String, Object>  row;

    FetchNext rs = this.query(sql, start, limit, params);
    while (( row = rs.fetch() ) != null)
    {
      rows.add(row);
    }

    return rows;
  }

  /**
   * 获取查询的全部数据
   * <p>注: 调fetch实现</p>
   * @param sql
   * @param params
   * @return 全部数据
   * @throws app.hongs.HongsException
   */
  public List<Map<String, Object>> fetchAll(String sql, Object... params)
    throws HongsException
  {
    return this.fetch(sql, 0, 0, params);
  }

  /**
   * 获取查询的单条数据
   * <p>注: 调fetch实现</p>
   * @param sql
   * @param params
   * @return 单条数据
   * @throws app.hongs.HongsException
   */
  public Map<String, Object> fetchOne(String sql, Object... params)
    throws HongsException
  {
    List< Map<String, Object> > rows = this.fetch(sql, 0, 1, params);
    if (! rows.isEmpty( ))
    {
      return rows.get( 0 );
    }
    else
    {
      return new HashMap();
    }
  }

  /**
   * 采用查询体获取全部数据
   * <p>注: 调fetch实现</p>
   * @param caze
   * @return 全部数据
   * @throws app.hongs.HongsException
   */
  public List fetchMore(FetchCase caze)
    throws HongsException
  {
    return this.fetch(caze.getSQL(), caze.getStart(), caze.getLimit(), caze.getParams());
  }

  /**
   * 采用查询体获取单条数据
   * <p>注: 调fetch实现</p>
   * @param caze
   * @return 单条数据
   * @throws app.hongs.HongsException
   */
  public Map fetchLess(FetchCase caze)
    throws HongsException
  {
    List< Map<String, Object> > rows = this.fetch(caze.getSQL(), 0, 1, caze.getParams());
    if (! rows.isEmpty() )
    {
      return rows.get( 0 );
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
    this.connect();

    if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG))
    {
      StringBuilder sb = new StringBuilder(sql);
      List      paramz = new ArrayList(Arrays.asList(params));
      DB.checkSQLParams(sb, paramz);
      DB.mergeSQLParams(sb, paramz);
      app.hongs.CoreLogger.debug("DB.execute: " + sb.toString());
    }

    /**
     * 获取PreparedStatement对象
     */
    PreparedStatement ps = this.prepareStatement(sql, params);

    try
    {
      return ps.execute(  );
    }
    catch (SQLException ex)
    {
      throw new app.hongs.HongsException(0x104a, ex);
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
   * @return 更新的条数
   * @throws HongsException
   */
  public int updates(String sql, Object... params)
    throws HongsException
  {
    this.connect();

    if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG))
    {
      StringBuilder sb = new StringBuilder(sql);
      List      paramz = new ArrayList(Arrays.asList(params));
      DB.checkSQLParams(sb, paramz);
      DB.mergeSQLParams(sb, paramz);
      app.hongs.CoreLogger.debug("DB.updates: " + sb.toString());
    }

    PreparedStatement ps = this.prepareStatement(sql, params);

    try
    {
      return ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      throw new app.hongs.HongsException(0x104e, ex);
    }
    finally
    {
      this.closeStatement(ps);
    }
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
      throw new app.hongs.HongsException(0x104b, "Insert values can not be empty.");
    }

    /** 组织语句 **/

    String sql = "INSERT INTO `" + Text.escape(table, "`") + "`";
    List params2 = new ArrayList();
    String fs = "", vs = "";

    Iterator it = values.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry)it.next();
      String field = (String)entry.getKey();
      params2.add((Object)entry.getValue());

      fs += "`" + Text.escape(field, "`") + "`, ";
      vs += "?, ";
    }

    sql += " (" + fs.substring(0, fs.length() - 2) + ")";
    sql += " VALUES";
    sql += " (" + vs.substring(0, vs.length() - 2) + ")";

    /** 执行更新 **/

    return this.updates(sql, params2.toArray());
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
      throw new app.hongs.HongsException(0x104d, "Update values can not be empty.");
    }

    /** 组织语言 **/

    String sql = "UPDATE `" + Text.escape(table, "`") + "` SET ";
    List params2 = new ArrayList();

    Iterator it = values.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry)it.next();
      String field = (String)entry.getKey();
      params2.add((Object)entry.getValue());

      sql += "`" + Text.escape(field, "`") + "` = ?, ";
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

    return this.updates(sql, params2.toArray());
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

    String sql = "DELETE FROM `" + Text.escape(table, "`") + "`";

    if (where != null && where.length() != 0)
    {
      sql += " WHERE " + where;
    }

    /** 执行更新 **/

    return this.updates(sql, params);
  }

  //** 静态工具 **/

  /**
   * 引用字段名
   * @param field
   * @return 引用后的串
   */
  public static String quoteField(String field)
  {
    return "`" + Text.escape(field, "`") + "`";
  }

  /**
   * 引用值
   * @param value
   * @return 引用后的串
   */
  public static String quoteValue(String value)
  {
    return "'" + Text.escape(value, "'") + "'";
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

        // 加一个空参数防止查询失败
        if (set.isEmpty())
        {
          set = new HashSet();
          set.add( null );
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

  /**
   * 获取指定数据库对象
   * <b>注意:</b>
   * <p>
   * 如果指定数据库配置中有指定dbClass, 务必添加方法:
   * </p>
   * <pre>
 public static XxxDB getLogger()
    throws HongsException
 {
    return new XxxDB();
 }
 </pre>
   * @param name
   * @return 指定DB对象
   * @throws app.hongs.HongsException
   */
  public static DB getInstance(String name)
    throws HongsException
  {
    DB db;
    do
    {

    String key = DB.class.getName() + ":" + name;

    Core core = Core.THREAD_CORE.get();
    if ( core.containsKey(key))
    {
      db =  (DB) core.get(key);
      break;
    }

    Core gore = Core.GLOBAL_CORE;
    if ( gore.containsKey(key))
    {
      db =  (DB) gore.get(key);
      break;
    }

    /**
     * 如果存在dbClass描述则调用对应类来获取实例
     */

    DBConfig cf = new DBConfig (name);

    if (cf.dbClass != null && cf.dbClass.length() != 0)
    {
      db = (DB)Core.getInstance(cf.dbClass);
    }
    else
    {
      db = new DB(cf);
    }

    /**
     * 如有设置dbName的单次加载则将其放入静态映射
     */

    CoreConfig conf = Core.getInstance(CoreConfig.class);
    if (conf.getProperty("core.load.db."+name+".once", false))
    {
      gore.put(key, db);
    }
    else
    {
      core.put(key, db);
    }

    db.IN_OBJECT_MODE = conf.getProperty("core.in.object.mode", false);
    db.IN_TRNSCT_MODE = conf.getProperty("core.in.trnsct.mode", false);

    }
    while (false);

    db.IN_OBJECT_MODE = Synt.declare(
              Core.getInstance().got("__IN_OBJECT_MODE__"), db.IN_OBJECT_MODE);
    db.IN_TRNSCT_MODE = Synt.declare(
              Core.getInstance().got("__IN_TRNSCT_MODE__"), db.IN_TRNSCT_MODE);

    return db;
  }

  /**
   * 获取默认数据库对象
   * <b>注意:</b>
   * <p>
   * 如果指定数据库配置中有指定dbClass, 务必添加方法:
   * </p>
   * <pre>
 public static XxxDB getLogger()
    throws HongsException
 {
    return new XxxDB();
 }
 </pre>
   * @return 默认DB对象
   * @throws app.hongs.HongsException
   */
  public static DB getInstance()
    throws HongsException
  {
    return DB.getInstance("default");
  }

  /**
   * 以外部数据源的形式构造对象
   * @param name
   * @param info
   * @return
   * @throws HongsException
   */
  public static DB newInstance(String name, Properties info)
  throws HongsException {
      DB db = new DB();
      db.origin = new HashMap();
      db.origin.put("name", name);
      db.origin.put("info", info);
      return db;
  }

  /**
   * 以外部数据源的形式构造对象
   * @param name
   * @return
   * @throws HongsException
   */
  public static DB newInstance(String name)
  throws HongsException {
      return DB.newInstance(name, new Properties());
  }

  /**
   * 以内部数据源的形式构造对象(c3p0)
   * @param drv
   * @param url
   * @param info
   * @return
   * @throws HongsException
   */
  public static DB newInstance(String drv, String url, Properties info)
  throws HongsException {
      DB db = new DB();
      db.source = new HashMap();
      db.source.put("drv", drv);
      db.source.put("url", url);
      db.source.put("info", info);
      return db;
  }

  /**
   * 以内部数据源的形式构造对象
   * @param drv
   * @param url
   * @return
   * @throws HongsException
   */
  public static DB newInstance(String drv, String url)
  throws HongsException {
      return DB.newInstance(drv, url, new Properties());
  }

}
