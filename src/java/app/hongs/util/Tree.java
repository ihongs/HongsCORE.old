package app.hongs.util;

import app.hongs.HongsError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 树形操作工具
 *
 * <p>
 * 用于获取和设置树型结构
 * (&lt;Object,Map&lt;Object,Map&lt;...&gt;&gt;&gt;)
 * 数据的值;
 * 切记：keys
 * </p>
 *
 * @author Hongs
 */
public class Tree
{

  /**
   * 获取树纵深值(以属性或键方式"a.b[c]"获取)
   * @param map
   * @param path
   * @return 键对应的值
   */
  public static Object getValue(Map map, String path)
  {
    return getValue(map, path, null);
  }

  /**
   * 获取树纵深值
   * @param map
   * @param keys
   * @return 键对应的值
   */
  public static Object getArray(Map map, Object[] keys)
  {
    return getArray(map, keys, null);
  }

  /**
   * 获取树纵深值(以属性或键方式"a.b[c]"获取)
   * @param map
   * @param path
   * @param def
   * @return 键对应的值
   */
  public static Object getValue(Map map, String path, Object def)
  {
    path = path.replaceAll("\\]\\[", ".")
               .replace("[", ".")
               .replace("]", "" )
               .replaceFirst("\\.+$", ""); // a[b][c][] 与 a.b.c 一样, 应用场景: 表单中多选项按 id[] 提取数据
    String[] keys = path.split("\\.", -1);
    return getArray(map, keys, def);
  }

  /**
   * 获取树纵深值
   * @param map
   * @param keys
   * @param def
   * @return 键对应的值
   */
  public static Object getArray(Map map, Object[] keys, Object def)
  {
    Object val = map;
    for (int i = 0; i < keys.length; i ++)
    {
      if (val instanceof Map)
      {
        Map obj  =  (Map)val;
        Object key = keys[i];
        if (obj.containsKey(key))
        {
          val = obj.get(key);
          continue;
        }
      }
      else
      if (val instanceof List)
      {
        List lst = (List)val;
        Object key = keys[i];
        int idx = key instanceof Integer ? (Integer)key
                : Integer.parseInt ( key.toString( ) );
        if (idx < lst.size())
        {
          val = lst.get(idx);
          continue;
        }
      }
      return def;
    }
    return val;
  }

  /**
   * 设置树纵深值(以属性或键方式"a.b[c]"设置)
   * @param map
   * @param path
   * @param val
   */
  public static void setValue(Map map, String path, Object val)
  {
    path = path.replaceAll("\\]\\[", ".")
             .replace("[", ".")
             .replace("]", "" );
    String[] keys = path.split("\\.", -1);
    setArray(map, keys, val);
  }

  /**
   * 设置树纵深值
   * @param map
   * @param keys
   * @param val
   */
  public static void setArray(Map map, Object[] keys, Object val)
  {
    if ( keys.length != 0 )
    {
      setArray(map, keys, val, 0);
    }
    else
    if (val instanceof Map)
    {
      map.clear ();
      map.putAll((Map) val);
    }
    else
    {
      throw new HongsError(0x10, "Can not set obj to map with empty keys");
    }
  }

  /**
   * 设置树纵深值
   * @param obj
   * @param keys
   * @param val
   * @param idx
   */
  private static void setArray(Object obj, Object[] keys, Object val, int idx)
  {
    Object key = keys[idx];

    if (key == null || key.equals(""))
    {
      Collection col = (Collection)obj;

      if (keys.length == idx + 1)
      {
        col.add(val);
      }
      else
      {
        Object subKey = keys[idx + 1];
        Object subNode;

        if (key == null || key.equals(""))
        {
          subNode = new ArrayList();
        }
        else
        {
          subNode = new LinkedHashMap();
        }
        col.add(subNode);

        setArray(subNode, keys, val, idx + 1);
      }
    }
    else
    {
      Map map = (Map)obj;

      if (keys.length == idx + 1)
      {
        map.put(key, val);
      }
      else
      {
        Object subKey = keys[idx + 1];
        Object subNode;

        if (map.containsKey(key))
        {
          subNode = map.get(key);
        }
        else
        {
          if (key == null || key.equals(""))
          {
            subNode = new ArrayList();
          }
          else
          {
            subNode = new LinkedHashMap();
          }
          map.put(key, subNode);
        }

        setArray(subNode, keys, val, idx + 1);
      }
    }
  }

  /**
   * 将其他oth追加到map中
   * @param map
   * @param oth
   */
  public static void setDepth(Map map, Map oth) {
    Iterator i = oth.entrySet().iterator();
    while (i.hasNext()) {
        Map.Entry e = (Map.Entry) i.next();
        Object k2 = e.getKey(  );
        Object v2 = e.getValue();
        Object v1 =  map.get(k2);

        if (v1 instanceof Map && v2 instanceof Map) {
            setDepth((Map)v1, (Map)v2);
        }
        else {
            map.put(k2, v2);
        }
    }
  }

}
