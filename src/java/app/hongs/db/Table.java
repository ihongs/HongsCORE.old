package app.hongs.db;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.sql.Types;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
//import java.sql.ResultSet;
//import java.sql.SQLException;

import app.hongs.HongsException;
import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;

/**
 * <h1>数据表基础类</h1>
 * <pre>
 * 请总是用DB.getTable("Table_Name")来获取表对象
 * </pre>
 *
 * <h2>错误代码:</h2>
 * <pre>
 * 区间: 0x1070~0x109f
 *
 * 0x1070 缺少数据库对象
 * 0x1072 配置不能为空
 * 0x1074 缺少表名
 *
 * 0x1080 获取字段信息失败
 *
 * 0x1084 不能为空
 * 0x1086 精度超出
 * 0x1088 小数位超出
 * 0x108a 不是整型数值
 * 0x108c 不是浮点数值
 * 0x108e 不能为负值
 * 0x1090 无法识别的日期或时间格式
 * </pre>
 *
 * <h2>配置选项:</h2>
 * <pre>
 * core.disable.check.values  设置为true禁止在存储时对数据进行检查
 * core.default.date.format   可识别的日期类型, 默认"yyyy/MM/dd", 已移到语言
 * core.default.time.format   可识别的时间类型, 默认"HH:mm:ss", 已移到语言
 * core.table.field.ctime     创建时间字段名
 * core.table.field.mtime     修改时间字段名
 * </pre>
 *
 * @author Hongs
 */
public class Table
{
  /**
   * DB对象
   */
  public DB db;

  /**
   * 表名
   */
  public String name;

  /**
   * 表全名
   */
  public String tableName;

  /**
   * 主键名
   */
  public String primaryKey;

  /**
   * 表前缀(用于生成tableName, 构造后设置无效)
   */
  protected String tablePrefix;

  /**
   * 表后缀(用于生成tableName, 构造后设置无效)
   */
  protected String tableSuffix;

  private Map columns;
  private Map assocs;
  private Map scossa;

  public Table(DB db, Map tableConfig)
    throws HongsException
  {
    if (db == null)
    {
      throw new HongsException(0x1070, "Param db can not be null");
    }
    this.db = db;

    if (tableConfig == null)
    {
      throw new HongsException(0x1072, "Param tableConfig can not be null");
    }

    if (!tableConfig.containsKey("name"))
    {
      throw new HongsException(0x1074, "table name in tableConfig can not be empty");
    }
    this.name = (String)tableConfig.get("name");

    if (tableConfig.containsKey("prefix"))
    {
      this.tablePrefix = (String)tableConfig.get("prefix");
    }
    else if (this.tablePrefix == null)
    {
      this.tablePrefix = "";
    }

    if (tableConfig.containsKey("suffix"))
    {
      this.tableSuffix = (String)tableConfig.get("suffix");
    }
    else if (this.tableSuffix == null)
    {
      this.tableSuffix = "";
    }

    this.tableName = this.tablePrefix + this.name + this.tableSuffix;

    if (tableConfig.containsKey("primaryKey"))
    {
      this.primaryKey = (String)tableConfig.get("primaryKey");
    }
    else if (this.primaryKey == null)
    {
      this.primaryKey = "";
    }

    if (tableConfig.containsKey("assocs"))
    {
      this.assocs = (Map)tableConfig.get("assocs");
      this.scossa = (Map)tableConfig.get("scossa");
    }
  }

  public Table (DB db, String tableName)
    throws HongsException
  {
    this(db, buildTableConfig(tableName));
  }

  private static Map buildTableConfig(String name)
  {
    Map tableConfig = new HashMap();
    tableConfig.put( "name", name );
    return tableConfig;
  }

  /**
   * 查询多条记录(采用查询结构)
   * @param more
   * @return 全部记录
   * @throws app.hongs.HongsException
   */
  public List fetchMore(FetchMore more)
    throws HongsException
  {
    more.from(tableName, name);

    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    String dflag = conf.getProperty("core.table.field.dflag", "__dflag__");

    this.getColumns();

    // 默认不查询已经删除的记录
    if (this.columns.containsKey(dflag) && !more.hasOption("FETCH_DFLAG"))
    {
        more.where("`"+dflag+"` != 1" );
    }

    return FetchJoin.assocSelect(this, assocs, more);
  }

  /**
   * 获取单条记录(采用查询结构)
   * @param less
   * @return 单条记录
   * @throws app.hongs.HongsException
   */
  public Map fetchLess(FetchMore less)
    throws HongsException
  {
    less.limit(1);
    List<Map> rows = this.fetchMore(less);

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
   * 插入数据
   * @param values
   * @return 插入条数
   * @throws app.hongs.HongsException
   */
  public int insert(Map<String, Object> values)
    throws HongsException
  {
    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    String ctime = conf.getProperty("core.table.field.ctime", "__ctime__");
    String etime = conf.getProperty("core.table.field.etime", "__etime__");
    String mtime = conf.getProperty("core.table.field.mtime", "__mtime__");

    long time = System.currentTimeMillis();

    this.getColumns();

    // 存在__mtime__字段则自动放入当前时间
    if (this.columns.containsKey(mtime)
          && !values.containsKey(mtime))
    {
      int type = (Integer)((Map)this.columns.get(mtime)).get("type");
      switch (type)
      {
        case Types.DATE:
          values.put(mtime, new Date(time));
          break;
        case Types.TIME:
          values.put(mtime, new Time(time));
          break;
        case Types.TIMESTAMP:
          values.put(mtime, new Timestamp(time));
          break;
        default:
          values.put(mtime, time / 1000);
      }
    }

    // 存在__ctime__字段则自动放入当前时间
    if (this.columns.containsKey(ctime)
          && !values.containsKey(ctime))
    {
      int type = (Integer)((Map)this.columns.get(ctime)).get("type");
      switch (type)
      {
        case Types.DATE:
          values.put(ctime, new Date(time));
          break;
        case Types.TIME:
          values.put(ctime, new Time(time));
          break;
        case Types.TIMESTAMP:
          values.put(ctime, new Timestamp(time));
          break;
        default:
          values.put(ctime, time / 1000);
      }
    }

    // 存在__etime__字段则自动放入结束时间
    if (this.columns.containsKey(etime)
          && !values.containsKey(etime)
          && this.primaryKey  !=  null)
    {
      Map<String, Object> valuez = new HashMap();
      List<Object> paramz = new ArrayList(  );
      paramz.add(values.get(this.primaryKey));

      int type = (Integer)((Map)this.columns.get(etime)).get("type");
      switch (type)
      {
        case Types.DATE:
          valuez.put(etime, new Date(time));
          values.put(etime, new Date(0));
          paramz.add(new Date(0));
          break;
        case Types.TIME:
          valuez.put(etime, new Time(time));
          values.put(etime, new Time(0));
          paramz.add(new Time(0));
          break;
        case Types.TIMESTAMP:
          valuez.put(etime, new Timestamp(time));
          values.put(etime, new Timestamp(0));
          paramz.add(new Timestamp(0));
          break;
        default:
          valuez.put(etime, time / 1000);
          values.put(etime, 0);
          paramz.add(0);
      }

      this.update(valuez, "`"+this.primaryKey+"`=? AND `"+etime+"`=?", paramz);
    }

    // 整理数据
    Map mainValues = this.checkMainValues(values, true);

    // 插入数据
    return this.db.insert(this.tableName, mainValues);
  }

  /**
   * 更新数据
   * @param values
   * @param where
   * @param params
   * @return 更新条数
   * @throws app.hongs.HongsException
   */
  public int update(Map<String, Object> values, String where, Object... params)
    throws HongsException
  {
    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    String mtime = conf.getProperty("core.table.field.mtime", "__mtime__");

    long time = System.currentTimeMillis();

    this.getColumns();

    // 存在__mtime__字段则自动放入当前时间
    if (this.columns.containsKey(mtime)
          && !values.containsKey(mtime))
    {
      int type = (Integer)((Map)this.columns.get(mtime)).get("type");
      switch (type)
      {
        case Types.DATE:
          values.put(mtime, new Date(time));
          break;
        case Types.TIME:
          values.put(mtime, new Time(time));
          break;
        case Types.TIMESTAMP:
          values.put(mtime, new Timestamp(time));
          break;
        default:
          if (time > 2147483647) {
            time = time / 1000;
          }
          values.put(mtime, time);
      }
    }

    // 整理数据
    Map mainValues = this.checkMainValues(values, false);

    // 更新数据
    return this.db.update(this.tableName, mainValues, where, params);
  }

  /**
   * 删除数据
   * @param where
   * @param params
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int delete(String where, Object... params)
    throws HongsException
  {
    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    String dflag = conf.getProperty("core.table.field.dflag", "__dflag__");

    this.getColumns();

    // 存在__dflag__字段则将删除标识设置为1
    if (this.columns.containsKey(dflag))
    {
      Map data=new HashMap();data.put(dflag,1);
      return  this.update(data, where, params);
    }

    return this.db.delete(this.tableName, where, params);
  }

  /** 工具方法 **/

  /**
   * 获取字段(包含名称及类型等)
   * @return 全部字段信息
   * @throws app.hongs.HongsException
   */
  public Map getColumns()
    throws HongsException
  {
    if (this.columns == null)
    {
        this.columns = (new TableCols(this)).columns;
    }
    return this.columns;
  }

  /**
   * 获取字段名
   * @return 全部字段名
   * @throws app.hongs.HongsException
   */
  public String[] getColumnNames()
    throws HongsException
  {
    Map<String, Map> cols = (Map)this.getColumns();
    return cols.keySet( ).toArray( new String[0] );
  }

  /**
   * 获取关联信息
   * @param name 关联名
   * @return 关联信息
   */
  public Map getAssoc(String name)
  {
    if (this.scossa.containsKey(name))
    return (Map)this.scossa.get(name);
    return null;
  }

  /**
   * 获取关联表对象
   * @param name 关联名
   * @return 关联表对象
   * @throws HongsException
   */
  public Table getAssocTable(String name)
    throws HongsException
  {
    Map tc =  this.getAssoc(name);
    if (tc == null) return  null ;
    return this.db.getTable(Table.getAssocName(tc));
  }

  /**
   * 获取关联查询体
   * @param more 查询体
   * @param name 关联名
   * @return 关联查询体
   * @throws HongsException
   */
  public FetchMore getAssocInst(FetchMore more, String name)
    throws HongsException
  {
    Map tc =  this.getAssoc(name);
    if (tc == null) return  null ;
    return more.join(Table.getAssocPath(tc)).join(Table.getAssocName(tc));
  }

  /**
   * 获取关联表名
   * @param assoc 关联信息
   * @return 关联表名
   */
  protected static String getAssocName(Map assoc)
  {
    String tn = (String)assoc.get("realName");
    if (   tn == null || tn.length() == 0   )
           tn = (String)assoc.get(  "name"  );
    return tn;
  }

  /**
   * 获取关联路径
   * @param assoc 关联信息
   * @return 关联路径
   */
  protected static List<String> getAssocPath(Map assoc)
  {
    List<String> ts = (List)assoc.get("name");
    if (   ts == null  ) ts = new ArrayList();
    return ts;
  }

  /**
   * 检验主数据
   *
   * 会进行校验的类型:
   * CHAR, VARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, NLONGVARCHAR
   * TINYINT, SMALLINT, INTEGER, BIGINT
   * FLOAT, DOUBLE, NUMERIC, DECIMAL
   * DATE, TIME, TIMESTAMP
   * 推荐构建数据库时采用以上类型
   * 日期时间格式采用默认配置定义
   * 通过配置"core.dsiable.check.values=true"来关闭检查
   * 通过配置"core.default.date.format=日期格式串"来设置可识别的日期格式, 已移到语言
   * 通过配置"core.defualt.time.format=时间格式串"来设置可识别的时间格式, 已移到语言
   *
   * @param values
   * @param isNew
   * @return
   * @throws app.hongs.HongsException
   */
  private Map checkMainValues(Map values, boolean isNew)
    throws HongsException
  {
    /**
     * 日期时间格式
     */
    String dateFormat = null, timeFormat = null, dateTimeFormat = null;

    /**
     * 是否关闭检查
     */
    boolean disable = this.getDisableCheck(values);

    /**
     * 获取字段信息
     */
    this.getColumns();

    Map mainValues = new HashMap();

    Iterator it = this.columns.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry et = (Map.Entry)it.next();
      String  namc = (String) et.getKey();
      Map   column = (Map)  et.getValue();

      Object value = values.get(namc);

      /**
       * 如果存在该值, 而该值为空
       * 或不存在该值, 而处于新建
       * 则判断该字段是否可以为空
       * 新建时自增值为空跳过
       */
      if (values.containsKey(namc))
      {
        // 空串被视为null
        if ("".equals(value))
            value =  null;

        if (value == null
        && (Integer)column.get("isNullable")
        ==  java.sql.ResultSetMetaData.columnNoNulls)
        {
          throw nullException(namc);
        }
      }
      else
      {
        if (isNew == true
        &&!(Boolean)column.get("isAutoIncrement")
        && (Integer)column.get("isNullable")
        ==  java.sql.ResultSetMetaData.columnNoNulls)
        {
          throw nullException(namc);
        }
        continue;
      }

      /**
       * 如果关闭了检查或值不是基础类型, 则跳过数据检查
       * 通常POST或GET过来的总是String, 而JSON过来的是String/Number/Boolean/Null
       */
      if (disable || !(value instanceof String || value instanceof Number))
      {
        mainValues.put(namc, value);
        continue;
      }

      String valueStr = value.toString().trim();

      int precision = (Integer)column.get("precision");
      int scale = (Integer)column.get("scale");
      int type = (Integer)column.get("type");

      // 判断字符串类型
      if (type == Types.CHAR  || type == Types.VARCHAR  || type == Types.LONGVARCHAR
       || type == Types.NCHAR || type == Types.NVARCHAR || type == Types.LONGNVARCHAR)
      {
        // 判断精度
        if (valueStr.length() > precision)
        {
          throw precisionException(namc, valueStr, precision);
        }

        mainValues.put(namc, valueStr);
      }

      // 判断整型数值
      else if (type == Types.TINYINT || type == Types.SMALLINT || type == Types.INTEGER || type == Types.BIGINT)
      {
        if (!valueStr.matches("^[\\-+]?[0-9]+$"))
        {
          throw intException(namc, valueStr);
        }

        if (!(Boolean)column.get("isSigned") && valueStr.startsWith("-"))
        {
          throw unsignedException(namc, valueStr);
        }

        /**
         * 取数字的绝对值(去负号), 便于检查长度
         */
        DecimalFormat df = new DecimalFormat("#");
        double valueNum = Double.parseDouble(valueStr);
        String valueStr2 = df.format(Math.abs(valueNum));

        // 判断精度
        if (valueStr2.length() > precision)
        {
          throw precisionException(namc, valueStr, precision);
        }

        mainValues.put(namc, valueNum);
      }

      // 判断非整型数值
      else if (type == Types.FLOAT || type == Types.DOUBLE || type == Types.NUMERIC || type == Types.DECIMAL)
      {
        if (!valueStr.matches("^[\\-+]?[0-9]+(\\.[0-9]+)?$"))
        {
          throw floatException(namc, valueStr);
        }

        if (!(Boolean)column.get("isSigned") && valueStr.startsWith("-"))
        {
          throw unsignedException(namc, valueStr);
        }

        // 判断小数位数, 填充小数位
        StringBuilder sb = new StringBuilder();
        sb.append("#.#");
        for (int i = 0; i < scale; i ++)
          sb.append('#');
        String sbs = sb.toString();

        /**
         * 取数字的绝对值(去负号), 便于检查长度
         */
        DecimalFormat df = new DecimalFormat(sbs);
        double valueNum = Double.parseDouble(valueStr);
        String valueStr2 = df.format(Math.abs(valueNum));

        int dotPos = valueStr2.indexOf('.');
        if (dotPos == -1)
        {
          /**
           * 判断精度
           */
          if (valueStr2.length() > precision)
          {
            throw precisionException(namc, valueStr, precision);
          }
        }
        else
        {
          int allLen;
          int subLen;
          if (valueStr.startsWith("0"))
          {
            allLen = valueStr.length() - 2;
            subLen = allLen;
          }
          else
          {
            allLen = valueStr.length() - 1;
            subLen = allLen - dotPos;
          }

          /**
           * 判断精度
           */
          if (allLen > precision)
          {
            throw precisionException(namc, valueStr, precision);
          }

          /**
           * 判断小数
           */
          if (subLen > scale)
          {
            throw scaleException(namc, valueStr, precision);
          }
        }

        mainValues.put(namc, valueNum);
      }

      // 判断日期类型
      else if (type == Types.DATE)
      {
        if (value instanceof java.sql.Date || value instanceof java.util.Date)
        {
          mainValues.put(namc, value);
        }
        else if (valueStr.matches("^\\d+$"))
        {
          long valueNum = Long.parseLong(valueStr);
          mainValues.put(namc, new Date(valueNum));
        }
        else
        {
          if (dateFormat == null)
          {
            dateFormat = this.getDatimeFormat(values, "date");
          }
          SimpleDateFormat sdf = new java.text.SimpleDateFormat(dateFormat);

          try
          {
            sdf.parse(valueStr);
          }
          catch (ParseException ex)
          {
            throw dateTimeException(namc, valueStr, dateFormat);
          }

          mainValues.put(namc, value);
        }
      }

      // 判断时间类型
      else if (type == Types.TIME)
      {
        if (value instanceof java.sql.Time || value instanceof java.util.Date)
        {
          mainValues.put(namc, value);
        }
        else if (valueStr.matches("^\\d+$"))
        {
          long valueNum = Long.parseLong(valueStr);
          mainValues.put(namc, new Time(valueNum));
        }
        else
        {
          if (timeFormat == null)
          {
            timeFormat = this.getDatimeFormat(values, "time");
          }
          SimpleDateFormat sdf = new java.text.SimpleDateFormat(timeFormat);

          try
          {
            sdf.parse(valueStr);
          }
          catch (ParseException ex)
          {
            throw dateTimeException(namc, valueStr, timeFormat);
          }

          mainValues.put(namc, value);
        }
      }

      // 判断时间戳或日期时间类型
      else if (type == Types.TIMESTAMP)
      {
        if (value instanceof java.sql.Timestamp || value instanceof java.util.Date)
        {
          mainValues.put(namc, value);
        }
        else if (valueStr.matches("^\\d+$"))
        {
          long valueNum = Long.parseLong(valueStr);
          mainValues.put(namc, new Timestamp(valueNum));
        }
        else
        {
          if (dateTimeFormat == null)
          {
            dateTimeFormat = this.getDatimeFormat(values, "datetime");
          }
          SimpleDateFormat sdf = new java.text.SimpleDateFormat(dateTimeFormat);

          try
          {
            sdf.parse(valueStr);
          }
          catch (ParseException ex)
          {
            throw dateTimeException(namc, valueStr, dateTimeFormat);
          }

          mainValues.put(namc, value);
        }
      }

      // 其他类型则直接放入(推荐建库时采用上面有校验的类型)
      else
      {
        mainValues.put(namc, value);
      }
    }

    return mainValues;
  }

  /**
   * 插入子数据
   *
   * 用于Model中, Table中不自动删除关联数据
   *
   * @param values
   * @throws app.hongs.HongsException
   */
  protected void insertSubValues(Map values)
    throws HongsException
  {
    FetchJoin.assocInsert(this, assocs, values);
  }

  /**
   * 删除子数据
   *
   * 用于Model中, Table中不自动删除关联数据
   *
   * @param id
   * @throws app.hongs.HongsException
   */
  protected void deleteSubValues(String id)
    throws HongsException
  {
    FetchJoin.assocDelete(this, assocs, id);
  }

  /** 其他私有方法 **/

  private static boolean getDisableCheck(Map values)
  {
    String key = "__disable_check__";
    if (values.containsKey(key))
    {
      if (values.get(key) instanceof Boolean)
      {
        return !(Boolean)values.remove(key);
      }
    }

    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    return conf.getProperty("core.disable.check.values", false);
  }

  private static String getDatimeFormat(Map values, String type)
  {
    String key = "__"+type+"_format__";
    if (values.containsKey(key))
    {
      if (values.get(key) instanceof String)
      {
        return (String)values.remove(key);
      }
    }

    String dftFmt;
    if ("time".equals(type))
      dftFmt = "HH:mm:ss";
    else
    if ("date".equals(type))
      dftFmt = "yyyy/MM/dd";
    else
      dftFmt = "yyyy/MM/dd HH:mm:ss";

    CoreConfig conf = (CoreConfig)Core.getInstance(CoreLanguage.class);
    return conf.getProperty("core.default."+type+".format", dftFmt);
  }

  private HongsException nullException(String name) {
    String error = "Value for column '"+name+"' can not be NULL";
    return validateException(0x1084, error, name);
  }

  private HongsException scaleException(String name, String value, int scale) {
    String error = "Scale for column '"+name+"'("+value+") must be a less than "+scale;
    return validateException(0x1088, error, name, value, String.valueOf(scale));
  }

  private HongsException precisionException(String name, String value, int precision) {
    String error = "Precision for column '"+name+"'("+value+") must be a less than "+precision;
    return validateException(0x1086, error, name, value, String.valueOf(precision));
  }

  private HongsException intException(String name, String value) {
    String error = "Value for column '"+name+"'("+value+") is not a int number";
    return validateException(0x108a, error, name, value);
  }

  private HongsException floatException(String name, String value) {
    String error = "Value for column '"+name+"'("+value+") is not a float number";
    return validateException(0x108c, error, name, value);
  }

  private HongsException unsignedException(String name, String value) {
    String error = "Value for column '"+name+"'("+value+") must be a unsigned number";
    return validateException(0x108e, error, name, value);
  }

  private HongsException dateTimeException(String name, String value, String format) {
    String error = "Format for column '"+name+"'("+value+") must like this '"+format+"'";
    return validateException(0x1090, error, name, value, format);
  }

  private HongsException validateException(int code, String error, String fieldName, String... otherParams)
  {
    CoreLanguage lang = (CoreLanguage)
        Core.getInstance(CoreLanguage.class);

    String tl = lang.getProperty("user.table."+name, name);
    String fl = lang.getProperty("user.field."+name+"."+fieldName);
    if (fl == null) {
        fl =  lang.getProperty("user.field."+fieldName, fieldName);
    }

    List<String> trans = new ArrayList();
    trans.add(tl);
    trans.add(fl);
    trans.addAll(Arrays.asList(otherParams));

    HongsException ex = new HongsException(code, error+" (Table:"+name);
    ex.setTranslate(trans.toArray(new String[] {}));
    return ex;
  }
}
