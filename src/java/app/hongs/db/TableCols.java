package app.hongs.db;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import app.hongs.CoreSerially;
import app.hongs.HongsException;

/**
 * <h1>表字段信息缓存类</h1>
 * <pre>
 * 缓存文件存放在 "WEB-INF/tmps/dc-库名.表名.ser" 的文件中; 当表结构发生改变, 程序不
 * 会自动重载, 务必删除对应的缓存文件
 * </pre>
 * @author hongs
 */
public class TableCols
     extends CoreSerially
  implements Serializable
{

  public  Map columns;

  private Table table;

  public TableCols(Table table)
    throws HongsException
  {
    this.table = table;
    if (table.db.name != null && table.db.name.length()!=0)
    {
      this.init("dc-" +table.db.name+ "." +table.tableName);
    }
    else
    {
      this.loadData();
    }
  }

  @Override
  protected final void loadData()
    throws HongsException
  {
    this.columns = new HashMap();

    FetchNext rs = this.table.db.query("SELECT * FROM "
                 + this.table.tableName + " LIMIT 1" );
    try
    {
      ResultSetMetaData md = rs.getReusltSet().getMetaData();

      for (int i = 1; i <= md.getColumnCount(); i ++)
      {
        Map column = new HashMap();
        column.put("type",            md.getColumnType(i));
        column.put("typeName",        md.getColumnTypeName(i));
        column.put("className",       md.getColumnClassName(i));
        column.put("displaySize",     md.getColumnDisplaySize(i));
        column.put("precision",       md.getPrecision(i));
        column.put("scale",           md.getScale(i));
        column.put("isSigned",        md.isSigned(i));
        column.put("isCurrency",      md.isCurrency(i));
        column.put("isNullable",      md.isNullable(i));
        column.put("isWritable",      md.isWritable(i));
        column.put("isReadOnly",      md.isReadOnly(i));
        column.put("isCaseSensitive", md.isCaseSensitive(i));
        column.put("isAutoIncrement", md.isAutoIncrement(i));

        // 在这里没什么意义的属性:
        /*
        column.put("catalogName",     md.getCatalogName(i));
        column.put("schemaName",      md.getSchemaName(i));
        column.put("tableName",       md.getTableName(i));
        column.put("columnName",      md.getColumnName(i));
        column.put("columnLabel",     md.getColumnLable(i));
        column.put("isSearchable",    md.isSearchable(i));
        */

        this.columns.put(md.getColumnName(i), column);
      }
    }
    catch (SQLException ex)
    {
      // 抛出Table的异常代号
      throw new HongsException(0x1080, ex);
    }
    finally
    {
      rs.close();
    }
  }

}
