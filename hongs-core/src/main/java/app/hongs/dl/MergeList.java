package app.hongs.dl;

import app.hongs.util.Dict;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 合并列表
 * @author Hongs
 */
public class MergeList
{

  protected List<Map> rows;

  public MergeList(List<Map> rows)
  {
    this.rows = rows;
  }

  /**
   * 获取关联ID和行
   *
   * @param map
   * @param key
   */
  private void mapped(Map<Object, List> map, String... key)
  {
    Iterator it = this.rows.iterator();
    W:while (it.hasNext())
    {
      Object row = it.next();
      Object obj = row;

      // 获取id值
      for (int i = 0; i < key.length; i ++)
      {
        if (obj instanceof Map)
        {
          obj = ((Map)obj).get(key[i]);
        }
        else
        if (obj instanceof List)
        {
          List rowz = this.rows;
          this.rows = (List)obj;

          // 切割子键数组
          int j = key.length - i ;
          String[] keyz = new String[j];
          System.arraycopy(key, i, keyz, 0, j);

          // 往下递归一层
          this.mapped(map, keyz);

          this.rows = rowz;

          continue W;
        }
        else
        {
          continue W;
        }
      }
      if (obj == null)
      {
          continue W;
      }

      // 登记行
      if (map.containsKey(obj))
      {
        map.get(obj ).add(row);
      }
      else
      {
        List lst = new ArrayList();
        map.put(obj , lst);
        lst.add(row);
      }
    }
  }

  /**
   * 获取关联ID和行
   *
   * @param key 使用"."分割的键
   * @return
   */
  public Map<Object, List> mapped(String key)
  {
    Map<Object, List> map = new HashMap();
    this.mapped( map, key.split("\\."));
    return map;
  }

  public void extend(List<Map> rows, Map<Object, List> map, String col, String sub)
  {
    Map     row, raw;
    List    lst;
    Object  rid;

    Iterator rs = rows.iterator();
    while (rs.hasNext())
    {
      raw = ( Map  ) rs.next(   );
      rid =          raw.get(col);
      lst = ( List ) map.get(rid);

      if (lst == null)
      {
        //throw new HongsException(0x10c0, "Line nums is null");
        continue;
      }

      Iterator it = lst.iterator();
      while (it.hasNext())
      {
        row = (Map) it.next();

        if (sub != null)
        {
          Dict.setParam(row, raw, sub);
        }
        else
        {
          raw.putAll(row);
          row.putAll(raw);
        }
      }
    }
  }

  public void extend(List<Map> rows, String key, String col, String sub)
  {
    extend(rows, MergeList.this.mapped(key), col, sub);
  }

  public void append(List<Map> rows, Map<Object, List> map, String col, String sub)
  {
    Map     row, raw;
    List    lst;
    Object  rid;

    Iterator rs = rows.iterator();
    while (rs.hasNext())
    {
      raw = ( Map  ) rs.next(   );
      rid =          raw.get(col);
      lst = ( List ) map.get(rid);

      if (lst == null)
      {
        //throw new HongsException(0x10c0, "Line nums is null");
        continue;
      }

      Iterator it = lst.iterator();
      while (it.hasNext())
      {
        row = (Map) it.next();

        if (row.containsKey(sub))
        {
          (( List ) row.get(sub)).add(raw);
        }
        else
        {
          List lzt = new ArrayList();
          row.put(sub, lzt);
          lzt.add(raw);
        }
      }
    }
  }

  public void append(List<Map> rows, String key, String col, String sub)
  {
    append(rows, MergeList.this.mapped(key), col, sub);
  }

}
