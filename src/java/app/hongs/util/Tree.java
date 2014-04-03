package app.hongs.util;

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
 * 用于获取和设置树型结构(&lt;Map&lt;Map...&gt;&gt;)数据的值
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
  public static Object getArray(Map map, String[] keys)
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
               .replaceFirst("\\.$", "" );
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
  public static Object getArray(Map map, String[] keys, Object def)
  {
    Object val = map;
    for (int i = 0; i < keys.length; i ++)
    {
      if (val instanceof Map)
      {
        Map obj  =  (Map)val;
        String key = keys[i];
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
        String key = keys[i];
        int idx = Integer.parseInt(key);
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
  public static void setArray(Map map, String[] keys, Object val)
  {
    assert keys.length > 0 : "keys can not be empty";
    setArray(map, keys, val, 0);
  }

  /**
   * 设置树纵深值
   * @param obj
   * @param keys
   * @param val
   * @param idx
   */
  private static void setArray(Object obj, String[] keys, Object val, int idx)
  {
    String key = keys[idx];

    if (key.length() != 0)
    {
      Map map = (Map)obj;

      if (keys.length == idx + 1)
      {
        map.put(key, val);
      }
      else
      {
        String subKey = keys[idx + 1];
        Object subNode;

        if (map.containsKey(key))
        {
          subNode = map.get(key);
        }
        else
        {
          if (subKey.length() != 0)
          {
            subNode = new LinkedHashMap();
          }
          else
          {
            subNode = new ArrayList();
          }
          map.put(key, subNode);
        }

        setArray(subNode, keys, val, idx + 1);
      }
    }
    else
    {
      Collection col = (Collection)obj;

      if (keys.length == idx + 1)
      {
        col.add(val);
      }
      else
      {
        String subKey = keys[idx + 1];
        Object subNode;

        if (subKey.length() != 0)
        {
          subNode = new LinkedHashMap();
        }
        else
        {
          subNode = new ArrayList();
        }
        col.add(subNode);

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
    assert map != null && oth != null : "map1 or map2 can not be null";
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
