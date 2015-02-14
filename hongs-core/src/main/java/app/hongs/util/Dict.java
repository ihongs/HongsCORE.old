package app.hongs.util;

import app.hongs.HongsError;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
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
public class Dict
{

  /**
   * 设置树纵深值
   * @param obj
   * @param val
   * @param keys
   * @param snum
   */
  private static Object put(Object obj, Object val, Object[] keys, int snum)
  {
    Object key = keys[snum];

    if (key == null || key.equals(""))
    {
      Collection col = (Collection)obj;

      if (keys.length == snum + 1)
      {
        return col.add(val);
      }
      else
      {
        Object subNode;

        Object key2 = keys[snum + 1];
        if (key2 == null || key2.equals(""))
        {
          subNode = new ArrayList();
        }
        else
        {
          subNode = new LinkedHashMap();
        }
        col.add(subNode);

        return put(subNode, val, keys, snum + 1);
      }
    }
    else
    {
      Map map = (Map)obj;

      if (keys.length == snum + 1)
      {
        return map.put(key, val);
      }
      else
      {
        Object subNode;

        if (map.containsKey(key))
        {
          subNode = map.get(key);
        }
        else
        {
          Object key2 = keys[snum + 1];
          if (key2 == null || key2.equals(""))
          {
            subNode = new ArrayList();
          }
          else
          {
            subNode = new LinkedHashMap();
          }
          map.put(key, subNode);
        }

        return put(subNode, val, keys, snum + 1);
      }
    }
  }

  /**
   * 获取树纵深值
   * @param map
   * @param def
   * @param keys
   * @return 键对应的值
   */
  public static Object get(Map map, Object def, Object... keys)
  {
    if (map == null)
    {
      throw new HongsError(0x47, "`map` can not be null");
    }
    Object val = map;
    for  (Object key  :  keys)
    {
      if (val instanceof Map )
      {
        Map  obj = (Map ) val;
        if (obj.containsKey(key))
        {
          val = obj.get(key);
          continue;
        }
      }
      else
      if (val instanceof List)
      {
        List lst = (List) val;
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
   * 设置树纵深值
   * @param map
   * @param val
   * @param keys
   */
  public static Object put(Map map, Object val, Object... keys)
  {
    if (map == null)
    {
      throw new HongsError(0x48, "`map` can not be null");
    }
    if (keys.length  !=  0)
    {
      return put(map, val, keys, 0);
    }
    else
    {
      throw new HongsError(0x49, "`keys` is empty");
    }
  }

  /**
   * 将 oth 追加到 map 中
   * 与 Map.putAll 的不同在于: 本函数会将其子级的 Map 也进行合并
   * @param map
   * @param oth
   */
  public static void putAll(Map map, Map oth) {
    Iterator i = oth.entrySet().iterator();
    while (i.hasNext()) {
        Map.Entry e = (Map.Entry) i.next();
        Object k2 = e.getKey(  );
        Object v2 = e.getValue();
        Object v1 =  map.get(k2);

        if (v1 instanceof Map && v2 instanceof Map) {
            putAll((Map)v1, (Map)v2);
        }
        else {
            map.put(k2, v2);
        }
    }
  }

  /**
   * 获取树纵深值
   * @param map
   * @param keys
   * @return
   */
  public static Object getDepth(Map map, Object... keys)
  {
    return get(map, null, keys);
  }

  /**
   * 获取树纵深值
   * @param map
   * @param def
   * @param keys
   * @return 键对应的值
   */
  public static <T> T getValue(Map map, T def, Object... keys)
  {
    try {
      return Synt.declare(get(map, def , keys), def);
    } catch (HongsError er) {
      if (er.getCode()  ==  0x46) {
        er = new HongsError(0x46, "Wrong type for key '"+Arrays.toString(keys)+"'", er.getCause());
      }
      throw er;
    }
  }

  /**
   * 获取树纵深值
   * @param map
   * @param cls
   * @param keys
   * @return 键对应的值
   */
  public static <T> T getValue(Map map, Class<T> cls, Object... keys)
  {
    try {
      return Synt.declare(get(map, null, keys), cls);
    } catch (HongsError er) {
      if (er.getCode()  ==  0x46) {
        er = new HongsError(0x46, "Wrong type for key '"+Arrays.toString(keys)+"'", er.getCause());
      }
      throw er;
    }
  }

  /**
   * 获取树纵深值(以属性或键方式"a.b[c]"获取)
   * @param map
   * @param path
   * @return 键对应的值
   */
  public static Object getParam(Map map, String path)
  {
    return getDepth(map, parsePath(path));
  }

  /**
   * 获取树纵深值
   * @param map
   * @param def
   * @param path
   * @return 键对应的值
   */
  public static <T> T getParam(Map map, T def, String path)
  {
    return getValue(map, def, parsePath(path));
  }

  /**
   * 获取树纵深值(以属性或键方式"a.b[c]"获取)
   * @param map
   * @param cls
   * @param path
   * @return 键对应的值
   */
  public static <T> T getParam(Map map, Class<T> cls, String path)
  {
    return getValue(map, cls, parsePath(path));
  }

  /**
   * 设置树纵深值(put的别名)
   * @param map
   * @param val
   * @param path
   * @return 键的旧的值
   */
  public static Object setValue(Map map, Object val, Object... keys)
  {
    return put(map, val, keys);
  }

  /**
   * 设置树纵深值(以属性或键方式"a.b[c]"设置)
   * @param map
   * @param val
   * @param path
   * @return 键的旧的值
   */
  public static Object setParam(Map map, Object val, String path)
  {
    return put(map, val, splitPath(path));
  }

  /**
   * 将 oth 追加到 map.keys 中
   * 与 Map.putAll 的不同在于: 本函数会将其子级的 Map 也进行合并
   * @param map
   * @param oth
   */
  public static void setValues(Map map, Map oth, Object... keys) {
      Object sub = get(map, keys);
      if (sub == null || !(sub instanceof Map)) {
        put   ( map, oth, keys);
      } else {
        putAll((Map) sub, oth );
      }
  }

  /**
   * 将 oth 追加到 map.path 中(path会按 .|[] 拆分)
   * 与 Map.putAll 的不同在于: 本函数会将其子级的 Map 也进行合并
   * @param map
   * @param oth
   */
  public static void setParams(Map map, Map oth, String path) {
    setValues(map, oth, splitPath(path));
  }

  private static Object[] splitPath(String path) {
    return path.replaceAll("\\]\\[", ".")
               .replace("[", ".")
               .replace("]", "" )
               .split("\\.", -1 );
  }

  private static Object[] parsePath(String path) {
    return path.replaceAll("\\]\\[", ".")
               .replace("[", ".")
               .replace("]", "" )
               .replaceFirst("\\.+$", "")
               .split("\\.", -1 );
  }

}
