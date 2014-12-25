package app.hongs.db;

import app.hongs.CoreSerially;
import app.hongs.HongsException;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 表字段信息缓存类
 *
 * <p>
 * 缓存文件存放在 "WEB-INF/tmps/库名.表名.dc.ser" 的文件中;
 * 当表结构发生改变, 程序不会自动重载, 务必删除对应的缓存文件.
 * </p>
 *
 * @author hongs
 */
public class DTColumn
     extends CoreSerially
  implements Serializable
{

  private Table table;

  public Map<String, Object> columns;

  public DTColumn(Table table)
    throws HongsException
  {
    this.table = table;
    if (table.db.name != null && table.db.name.length()!=0)
    {
      this.init(table.db.name +"."+ table.tableName +".dc");
    }
    else
    {
      this.imports();
    }
  }

  @Override
  protected final void imports()
    throws HongsException
  {
    this.columns = new HashMap();

    FetchNext rs = this.table.db.query("SELECT * FROM "
                 + this.table.tableName, 0, 1);
    try
    {
      ResultSetMetaData md = rs.getReusltSet().getMetaData();

      for (int i = 1; i <= md.getColumnCount(); i ++)
      {
        Map column = new HashMap();
        column.put("type",            md.getColumnType(i));
        column.put("size",            md.getPrecision(i));
        column.put("scale",           md.getScale(i));
        column.put("signed",          md.isSigned(i));
        column.put("nullable",        md.isNullable(i));
        column.put("autoIncrement",   md.isAutoIncrement(i));
        column.put("caseSensitive",   md.isCaseSensitive(i));

        // 在这里没什么意义的属性:
        /*
        column.put("catalogName",     md.getCatalogName(i));
        column.put("schemaName",      md.getSchemaName(i));
        column.put("tableName",       md.getTableName(i));
        column.put("label",           md.getColumnLable(i));
        column.put("typeName",        md.getColumnTypeName(i));
        column.put("className",       md.getColumnClassName(i));
        column.put("displaySize",     md.getColumnDisplaySize(i));
        column.put("currency",        md.isCurrency(i));
        column.put("writable",        md.isWritable(i));
        column.put("readOnly",        md.isReadOnly(i));
        column.put("searchable",      md.isSearchable(i));
        */

        this.columns.put(md.getColumnName(i), column);
      }
    }
    catch (SQLException ex)
    {
      // 抛出Table的异常代号
      throw new HongsException(0x106a, ex);
    }
    finally
    {
      rs.close();
    }
  }

}
