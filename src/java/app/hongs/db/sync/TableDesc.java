package app.hongs.db.sync;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Table;

/**
 * <h1>表结构描述(Table structure descriptions)</h1>
 *
 * <b>注意: 参考MySQL编写, 可能不适用于其他数据库</b>
 *
 * @author Hongs
 */
public class TableDesc
{

  /**
   * 主键信息
   * [主键代码]
   */
  public Set<String>              primaryKey;

  /**
   * 唯一键信息
   * {键名 : [键代码]}
   */
  public Map<String, Set<String>> uniqueKeys;

  /**
   * 索引键信息
   * {键名 : [键代码]}
   */
  public Map<String, Set<String>> indexKeys;

  /**
   * 字段信息
   * {字段名 : 字段描述串}
   */
  public Map<String, String>      fields;

  public TableDesc(Table table)
  throws HongsException
  {
    this.primaryKey = new HashSet<String>();
    this.uniqueKeys = new HashMap<String, Set<String>>();
    this.indexKeys  = new HashMap<String, Set<String>>();
    this.fields     = new HashMap<String, String>();

    List rows = table.db.fetchAll("DESCRIBE `"+table.tableName+"`");
    Iterator it = rows.iterator();
    while (it.hasNext())
    {
      Map row = (Map)it.next();
      String field = (String)row.get("Field");
      String type = (String)row.get("Type");
      String key = (String)row.get("Key");

      /**
       * 获取字段的附加标识
       * 组织字段描述
       */

      if ("NO".equals(row.get("Null")))
      {
        type += " NOT NULL";
      }
      else
      {
        type += " NULL";
      }

      String def = (String)row.get("Default");
      if (def != null)
      {
        type += " DEFAULT " + DB.quoteValue(def);
      }

      String ext = (String)row.get("Extra");
      if (ext != null)
      {
        type += " " + ext;
      }

      this.fields.put(field, type);

      /**
       * 获取键组合
       */

      if (key.equals("PRI"))
      {
        this.primaryKey.add(field);
      }
      else if (key.equals("UNI"))
      {
        Set<String> keys = new HashSet<String>();
        keys.add(field);
        this.uniqueKeys.put(key, keys);
      }
      else if (key.equals("MUL"))
      {
        Set<String> keys = new HashSet<String>();
        keys.add(field);
        this.indexKeys.put(key, keys);
      }
    }
  }

  /** 静态属性及方法 **/

  public static final int DROP = 0;
  public static final int ADD = 1;
  public static final int CHANGE = 2;
  public static final String PRIMARY = "PRIMARY";

  private static Map<Table, TableDesc> instances = new HashMap<Table, TableDesc>();

  /**
   * 获取唯一表描述实例
   * @param table
   * @return 表描述对象
   * @throws app.hongs.HongsException
   */
  public static TableDesc getInstance(Table table)
  throws HongsException
  {
    if (instances.containsKey(table))
    {
      return instances.get(table);
    }
    else
    {
      TableDesc td = new TableDesc(table);
      instances.put(table, td);
      return td;
    }
  }

  /**
   * 清除全部表描述实例
   */
  public static void clearInstances()
  {
    TableDesc.instances.clear();
  }

  /**
   * 更改字段
   * @param alterType 更改类型(DROP,ADD,CHANGE)
   * @param dstTable 要更改的表
   * @param srcTable 参考表
   * @param fieldName 字段名
   * @throws app.hongs.HongsException
   */
  public static void alterField(int alterType, Table dstTable, Table srcTable, String fieldName)
  throws HongsException
  {
    String sql = TableDesc.alterFieldSql(alterType, dstTable, srcTable, fieldName);
    dstTable.db.execute(sql);
  }

  /**
   * 更改键
   * @param alterType 更改类型(DROP,ADD)
   * @param dstTable 要更改的表
   * @param srcTable 参考表
   * @param keyName 字段名
   * @throws app.hongs.HongsException
   */
  public static void alterKey(int alterType, Table dstTable, Table srcTable, String keyName)
  throws HongsException
  {
    String sql = TableDesc.alterKeySql(alterType, dstTable, srcTable, keyName);
    dstTable.db.execute(sql);
  }

  /**
   * 获取更改字段的SQL
   * @param alterType 更改类型(DROP,ADD,CHANGE)
   * @param dstTable 要更改的表
   * @param srcTable 参考表
   * @param fieldName 字段名
   * @return 返回构造好的SQL语句
   * @throws app.hongs.HongsException
   */
  public static String alterFieldSql(int alterType, Table dstTable, Table srcTable, String fieldName)
  throws HongsException
  {
    TableDesc srcTd = TableDesc.getInstance(srcTable);

    String sql = "ALTER TABLE `" + dstTable.tableName + "`";
    String s;

    switch (alterType)
    {
      case TableDesc.DROP:
        sql += " DROP `" + fieldName + "`";
        break;
      case TableDesc.ADD:
        s = srcTd.fields.get(fieldName);
        sql += " ADD `" + fieldName + "` " + s;
        break;
      case TableDesc.CHANGE:
        s = srcTd.fields.get(fieldName);
        sql += " CHANGE `" + fieldName + "` `" + fieldName + "` " + s;
        break;
    }

    return sql;
  }

  /**
   * 获取更改键的SQL
   * @param alterType 更改类型(DROP,ADD)
   * @param dstTable 要更改的表
   * @param srcTable 参考表
   * @param keyName 字段名
   * @return 返回构造好的SQL语句
   * @throws app.hongs.HongsException
   */
  public static String alterKeySql(int alterType, Table dstTable, Table srcTable, String keyName)
  throws HongsException
  {
    TableDesc srcTd = TableDesc.getInstance(srcTable);

    String sql = "ALTER TABLE `" + dstTable.tableName + "`";
    String keyType, keyCode;

    switch (alterType)
    {
      case TableDesc.DROP:
        if (keyName.equals(TableDesc.PRIMARY))
        {
          sql += " DROP PRIMARY KEY";
        }
        else
        {
          keyType = TableDesc.getKeyType(srcTd, keyName);
          if (keyType != null)
          {
            sql += " DROP " + keyType + " `" + keyName + "`";
          }
        }
        break;
      case TableDesc.ADD:
        if (keyName.equals(TableDesc.PRIMARY))
        {
          keyCode = TableDesc.getKeyCode(srcTd.primaryKey);
          sql += " ADD PRIMARY KEY (" + keyCode + ")";
        }
        else
        {
          keyType = TableDesc.getKeyType(srcTd, keyName);
          keyCode = TableDesc.getKeyCode(srcTd, keyName);
          if (keyType != null && keyCode != null)
          {
            sql += " ADD " + keyType + " `" + keyName + "` (" + keyCode + ")";
          }
        }
        break;
    }

    return sql;
  }

  /** 私有静态工具方法 **/

  private static String getKeyType(TableDesc td, String key)
  {
    if (td.uniqueKeys.containsKey(key))
    {
      return "UNIQUE";
    }
    if (td.indexKeys.containsKey(key))
    {
      return "INDEX";
    }
    return null;
  }

  private static String getKeyCode(TableDesc td, String key)
  {
    if (td.uniqueKeys.containsKey(key))
    {
      return TableDesc.getKeyCode(td.uniqueKeys.get(key));
    }
    if (td.indexKeys.containsKey(key))
    {
      return TableDesc.getKeyCode(td.indexKeys.get(key));
    }
    return null;
  }

  private static String getKeyCode(Set<String> keys)
  {
    StringBuilder sb = new StringBuilder();
    Iterator it = keys.iterator();
    while (it.hasNext())
    {
      sb.append(",`" + (String)it.next() + "`");
    }
    return sb.substring(1);
  }

}
