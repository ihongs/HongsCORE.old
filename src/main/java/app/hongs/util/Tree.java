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
   * @throws app.hongs.HongsException
   */
  public static Object getValue(Map map, String path)
  {
    return Tree.getValue(map, path, null);
  }

  /**
   * 获取树纵深值
   * @param map
   * @param keys
   * @return 键对应的值
   */
  public static Object getByArr(Map map, Object[] keys)
  {
    return Tree.getByArr(map, keys, null);
  }

  /**
   * 获取树纵深值(以属性或键方式"a.b[c]"获取)
   * @param map
   * @param path
   * @param def
   * @return 键对应的值
   */
  public static <T> T getValue(Map map, String path, T def)
  {
    path = path.replaceAll("\\]\\[", ".")
               .replace("[", ".")
               .replace("]", "" )
               .replaceFirst("\\.+$", ""); // a[b][c][] 与 a.b.c 一样, 应用场景: 表单中多选项按 id[] 提取数据
    String[] keys = path.split("\\.", -1);
    return Tree.getByArr(map, keys, def);
  }

  /**
   * 获取树纵深值
   * @param map
   * @param keys
   * @param def
   * @return 键对应的值
   */
  public static <T> T getByArr(Map map, Object[] keys, T def)
  {
    if (map == null)
    {
      throw new HongsError(0x37, "`map` can not be null");
    }
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

    // 常规类型转换
    if (def instanceof String) {
      val = val.toString();
    } else
    if (val instanceof String) {
      if (def instanceof Integer) {
        val = Integer.parseInt((String) val);
      } else
      if (def instanceof Long) {
        val = Long.parseLong((String) val);
      } else
      if (def instanceof Float) {
        val = Float.parseFloat((String) val);
      } else
      if (def instanceof Number) {
        val = Double.parseDouble((String) val);
      }
    }

    try {
      return (T) val;
    } catch (ClassCastException ex) {
      throw new HongsError(0x36, "Wrong type for key '"+keys+"'", ex);
    }
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
    Tree.setByArr(map, keys, val);
  }

  /**
   * 设置树纵深值
   * @param map
   * @param keys
   * @param val
   */
  public static void setByArr(Map map, Object[] keys, Object val)
  {
    if (map == null)
    {
      throw new HongsError(0x38, "`map` can not be null");
    }
    if (keys.length  !=  0)
    {
      setValue(map, keys, val, 0);
    }
    else
    if (val instanceof Map)
    {
      map.clear ();
      map.putAll((Map) val);
    }
    else
    {
      throw new HongsError(0x39, "`val` is not a map AND `keys` is empty");
    }
  }

  /**
   * 设置树纵深值
   * @param obj
   * @param keys
   * @param val
   * @param idx
   */
  private static void setValue(Object obj, Object[] keys, Object val, int idx)
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
        Object subNode;

        Object key2 = keys[idx + 1];
        if (key2 == null || key2.equals(""))
        {
          subNode = new ArrayList();
        }
        else
        {
          subNode = new LinkedHashMap();
        }
        col.add(subNode);

        setValue(subNode, keys, val, idx + 1);
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
        Object subNode;

        if (map.containsKey(key))
        {
          subNode = map.get(key);
        }
        else
        {
          Object key2 = keys[idx + 1];
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

        setValue(subNode, keys, val, idx + 1);
      }
    }
  }

  /**
   * 将 oth 追加到 map 中
   * 与 Map.putAll 的不同在于: 本函数会将其子级的 Map 也进行合并
   * @param map
   * @param oth
   */
  public static void putDepth(Map map, Map oth) {
    Iterator i = oth.entrySet().iterator();
    while (i.hasNext()) {
        Map.Entry e = (Map.Entry) i.next();
        Object k2 = e.getKey(  );
        Object v2 = e.getValue();
        Object v1 =  map.get(k2);

        if (v1 instanceof Map && v2 instanceof Map) {
            putDepth((Map)v1, (Map)v2);
        }
        else {
            map.put(k2, v2);
        }
    }
  }

  /** 遍历工具 **/

  public static interface EachValue {
    public void each(Object value, String path);
  }

  public static interface EachPoint {
    public void each(Object value,  List  path);
  }

  /**
   * 遍历所有的叶子节点
   * 路径以字串形式给出(.分割层级, 如 a.b.c.)
   * @param data 要遍历的数据
   * @param loop 遍历回调接口
   */
  public static void each(Object data, EachValue loop) {
    each(data, loop, new StringBuilder());
  }

  /**
   * 遍历所有的叶子节点
   * 路径以列表形式给出
   * @param data 要遍历的数据
   * @param loop 遍历回调接口
   */
  public static void each(Object data, EachPoint loop) {
    each(data, loop, new ArrayList());
  }

  private static void each(Object data, EachValue loop, StringBuilder path) {
    if (data instanceof Map ) {
      Iterator it = ((Map) data).entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry et = (Map.Entry) it.next();
        Object k = et.getKey(  );
        Object v = et.getValue();

        StringBuilder p = new StringBuilder(path);
        p.append(".")
         .append( k );

        each(v, loop, p);
      }
    }
    else
    if (data instanceof List) {
      List a = (List) data;
      for (Object v : a) {
        StringBuilder p = new StringBuilder(path);
        p.append(".");

        each(v, loop, p);
      }
    }
    else
    if (data instanceof Object[]) {
      Object[] a = (Object[]) data;
      for (Object v : a) {
        StringBuilder p = new StringBuilder(path);
        p.append(".");

        each(v, loop, p);
      }
    }
    else
    if (path.length() > 0) {
        loop.each(data, path.substring(1));
    }
  }

  private static void each(Object data, EachPoint loop, List path) {
    if (data instanceof Map) {
      Iterator it = ((Map) data).entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry et = (Map.Entry) it.next();
        Object k = et.getKey(  );
        Object v = et.getValue();

        List p = new ArrayList(path);
        p.add(k);

        each(v, loop, p);
      }
    }
    else
    if (data instanceof List) {
      List a = (List) data;
      int k = -1;
      for (Object v : a) {
        k = k +1;

        List p = new ArrayList(path);
        p.add(k);

        each(v, loop, p);
      }
    }
    else
    if (data instanceof Object[]) {
      Object[] a = (Object[]) data;
      int k = -1;
      for (Object v : a) {
        k = k +1;

        List p = new ArrayList(path);
        p.add(k);

        each(v, loop, p);
      }
    }
    else
    if (!path.isEmpty()) {
        loop.each(data, path);
    }
  }

}
