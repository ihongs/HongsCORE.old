package app.hongs.util;

import app.hongs.HongsError;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  private static void setDepth(Object obj, Object val, Object[] keys, int snum)
  {
    Object key = keys[snum];

    if (key == null || key.equals(""))
    {
      Collection col = (Collection)obj;

      if (keys.length == snum + 1)
      {
        col.add(val);
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

        setDepth(subNode, val, keys, snum + 1);
      }
    }
    else
    {
      Map map = (Map)obj;

      if (keys.length == snum + 1)
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

        setDepth(subNode, val, keys, snum + 1);
      }
    }
  }

  /**
   * 设置树纵深值
   * @param map
   * @param keys
   * @param val
   */
  public static void setPoint(Map map, Object val, Object... keys)
  {
    if (map == null)
    {
      throw new HongsError(0x48, "`map` can not be null");
    }
    if (keys.length  !=  0)
    {
      setDepth(map, val, keys, 0);
    }
    else
    if (val instanceof Map)
    {
      map.clear ();
      map.putAll((Map) val);
    }
    else
    {
      throw new HongsError(0x49, "`val` is not a map AND `keys` is empty");
    }
  }

  /**
   * 设置树纵深值(以属性或键方式"a.b[c]"设置)
   * @param map
   * @param path
   * @param val
   */
  public static void setValue(Map map, Object val, String path)
  {
    Dict.setPoint(map, val, convPath(path));
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
            Dict.putDepth((Map)v1, (Map)v2);
        }
        else {
            map.put(k2, v2);
        }
    }
  }

  public static void putPoint(Map map, Map oth, Object... keys) {
      Object sub = getPoint(map, keys);
      if (sub == null || !(sub instanceof Map)) {
          sub =  new LinkedHashMap();
            Dict.setPoint( map, oth, keys );
      } else {
            Dict.putDepth((Map) sub, oth  );
      }
  }

  public static void putValue(Map map, Map oth, String path) {
      Object sub = getValue(map, path);
      if (sub == null || !(sub instanceof Map)) {
          sub =  new LinkedHashMap();
          setValue( map, oth, path );
      } else {
            Dict.putDepth((Map) sub, oth  );
      }
  }

  /**
   * 获取树纵深值
   * @param map
   * @param keys
   * @param def
   * @return 键对应的值
   */
  public static Object getPoint(Map map, Object... keys)
  {
    if (map == null)
    {
      throw new HongsError(0x47, "`map` can not be null");
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

      return  null;
    }

    return val;
  }

  /**
   * 获取树纵深值(以属性或键方式"a.b[c]"获取)
   * @param map
   * @param path
   * @return 键对应的值
   */
  public static Object getValue(Map map, String path)
  {
    return Dict.getPoint(map, convPath(path));
  }

  /**
   * 获取树纵深值
   * @param map
   * @param cls
   * @param keys
   * @return 键对应的值
   */
  public static <T> T getP2Cls(Map map, Class<T> cls, Object... keys)
  {
    try {
      return conv2Cls(Dict.getPoint(map, keys), cls);
    } catch (HongsError er) {
      if (er.getCode()  ==  0x46) {
        er = new HongsError(0x46, "Wrong type for key '"+keys+"'", er.getCause());
      }
      throw er;
    }
  }

  /**
   * 获取树纵深值(以属性或键方式"a.b[c]"获取)
   * @param map
   * @param cls
   * @param path
   * @return 键对应的值
   */
  public static <T> T getV2Cls(Map map, Class<T> cls, String path)
  {
    return Dict.getP2Cls(map, cls, convPath(path));
  }

  /**
   * 获取树纵深值
   * @param map
   * @param def
   * @param keys
   * @return 键对应的值
   */
  public static <T> T getP4Def(Map map, T def, Object... keys)
  {
    try {
      return conv4Def(Dict.getPoint(map, keys), def);
    } catch (HongsError er) {
      if (er.getCode()  ==  0x46) {
        er = new HongsError(0x46, "Wrong type for key '"+keys+"'", er.getCause());
      }
      throw er;
    }
  }

  /**
   * 获取树纵深值
   * @param map
   * @param def
   * @param keys
   * @return 键对应的值
   */
  public static <T> T getV4Def(Map map, T def, String path)
  {
    return Dict.getP4Def(map, def, convPath(path));
  }

  public static <T> T conv2Cls(Object val, Class<T> cls) {
    if (val == null) {
        return null;
    }

    if (cls.isAssignableFrom(String.class)) {
      val = val.toString();
    } else
    if (cls.isAssignableFrom(List.class)) {
      if (val instanceof Map) {
        val = ((Map) val).values();
      }
    } else
    if (cls.isAssignableFrom(Set.class)) {
      if (val instanceof List) {
        val = new LinkedHashSet((List) val);
      } else
      if (val instanceof Map) {
        val = new LinkedHashSet(((Map) val).values());
      }
    } else
    if (val instanceof Number) {
      if (cls.isAssignableFrom(Integer.class)) {
        val = ((Number) val).intValue();
      } else
      if (cls.isAssignableFrom(Byte.class)) {
        val = ((Number) val).byteValue();
      } else
      if (cls.isAssignableFrom(Short.class)) {
        val = ((Number) val).shortValue();
      } else
      if (cls.isAssignableFrom(Long.class)) {
        val = ((Number) val).longValue();
      } else
      if (cls.isAssignableFrom(Float.class)) {
        val = ((Number) val).floatValue();
      } else
      if (cls.isAssignableFrom(Number.class)) {
        val = ((Number) val).doubleValue();
      } else
      if (cls.isAssignableFrom(Boolean.class)) {
        val = 0 != ((Number) val).shortValue();
      }
    } else
    if (val instanceof String) {
      if (cls.isAssignableFrom(Integer.class)) {
        val = Integer.parseInt((String) val);
      } else
      if (cls.isAssignableFrom(Byte.class)) {
        val = Byte.parseByte((String) val);
      } else
      if (cls.isAssignableFrom(Short.class)) {
        val = Short.parseShort((String) val);
      } else
      if (cls.isAssignableFrom(Long.class)) {
        val = Long.parseLong((String) val);
      } else
      if (cls.isAssignableFrom(Float.class)) {
        val = Float.parseFloat((String) val);
      } else
      if (cls.isAssignableFrom(Number.class)) {
        val = Double.parseDouble((String) val);
      } else
      if (cls.isAssignableFrom(Boolean.class)) {
        val = Boolean.parseBoolean((String) val)
         || (!"0".equals(((String) val).trim( ))
         && !"no".equalsIgnoreCase(((String)val).trim())
         &&!"not".equalsIgnoreCase(((String)val).trim()));
      }
    }

    try {
      return (T) val;
    } catch (ClassCastException  ex) {
      throw new HongsError(0x46, ex);
    }
  }

  public static <T> T conv4Def(Object val, T def) {
      T vai = (T) conv2Cls(val, def.getClass());
      if (vai != null) {
          return vai;
      } else {
          return def;
      }
  }

  public static Object[] convPath(String path) {
    path = path.replaceAll("\\]\\[", ".")
               .replace("[", ".")
               .replace("]", "" )
               .replaceFirst("\\.+$", ""); // a[b][c][] 与 a.b.c 一样, 应用场景: 表单中多选项按 id[] 提取数据
    Object[] keys = path.split("\\.", -1);
    return keys;
  }

  public static Object convNode(Object data) {
    if (data instanceof Map ) {
      Map data2 = new LinkedHashMap();
      for (Object o : ((Map) data).entrySet()) {
          Map.Entry e = (Map.Entry) o;
          String k = e.getKey(  ).toString(  );
          Object v = e.getValue();
          data2.put(k, convNode(v));
      }
      data = data2;
    } else
    if (data instanceof Set ) {
      Set data2 = new LinkedHashSet();
      for (Object v : ((Set) data)) {
          data2.add(convNode(v));
      }
    } else
    if (data instanceof List) {
      List data2 = new ArrayList( );
      for (Object v : ((List)data)) {
          data2.add(convNode(v));
      }
    } else
    if (data instanceof Object[]) {
      List data2 = new ArrayList( );
      for (Object v : ((Object[]) data)) {
          data2.add(convNode(v));
      }
      data = data2.toArray();
    } else {
      if (data == null) {
        data = "";
      } else
      if (data instanceof Boolean) {
        data = (Boolean) data ? "1" : "";
      } else {
        data = data.toString();
      }
    }
    return data;
  }

}
