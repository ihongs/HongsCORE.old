package app.hongs.util;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <h1>树型操作工具</h1>
 * <pre>
 * 用于获取和设置树型结构(&lt;Map&lt;Map...&gt;&gt;)数据的值
 * </pre>
 *
 * @author Hongs
 */
public class Tree
{

  /**
   * 获取树纵深值
   * @param tree
   * @param keys
   * @return 键对应的值
   */
  public static Object getTreeValue(Map tree, String... keys)
  {
    Object obj = tree;
    for (int i = 0; i < keys.length; i ++)
    {
      if (obj instanceof Map)
      {
        Map map  =  (Map)obj;
        String key = keys[i];
        if (map.containsKey(key))
        {
          obj = map.get(key);
          continue;
        }
      }
      else
      if (obj instanceof List)
      {
        List lst = (List)obj;
        String key = keys[i];
        int idx = Integer.parseInt(key);
        if (idx < lst.size())
        {
          obj = lst.get(idx);
          continue;
        }
      }
      return null;
    }
    return obj;
  }

  /**
   * 获取树纵深值(类数组键方式"a[b][c]")
   * @param tree
   * @param key
   * @return 键对应的值
   */
  public static Object getArrayValue(Map tree, String key)
  {
    key = key.replaceAll("\\]\\[", ".")
             .replace("[", ".")
             .replace("]", "" )
             .replaceFirst("\\.$", "" );
    String[] keys = key.split("\\.", -1);
    return Tree.getTreeValue(tree, keys);
  }

  /**
   * 获取树纵深值(类对象属性方式"a.b.c")
   * @param tree
   * @param key
   * @return 键对应的值
   */
  public static Object getObjectValue(Map tree, String key)
  {
    String[] keys = key.split("\\.", -1);
    return Tree.getTreeValue(tree, keys);
  }

  /**
   * 设置树纵深值
   * @param value
   * @param tree
   * @param keys
   * @param pos
   */
  private static void setTreeValue(Object tree, String[] keys, Object value, int pos)
  {
    String key = keys[pos];

    if (key.length() != 0)
    {
      Map map = (Map)tree;

      if (keys.length == pos + 1)
      {
        map.put(key, value);
      }
      else
      {
        String subKey = keys[pos + 1];
        Object subNode;

        if (map.containsKey(key))
        {
          subNode = map.get(key);
        }
        else
        {
          if (subKey.length() != 0)
          {
            subNode = new HashMap();
          }
          else
          {
            subNode = new ArrayList();
          }
          map.put(key, subNode);
        }

        Tree.setTreeValue(subNode, keys, value, pos + 1);
      }
    }
    else
    {
      Collection col = (Collection)tree;

      if (keys.length == pos + 1)
      {
        col.add(value);
      }
      else
      {
        String subKey = keys[pos + 1];
        Object subNode;

        if (subKey.length() != 0)
        {
          subNode = new HashMap();
        }
        else
        {
          subNode = new ArrayList();
        }
        col.add(subNode);

        Tree.setTreeValue(subNode, keys, value, pos + 1);
      }
    }
  }

  /**
   * 设置树纵深值
   * @param tree
   * @param keys
   * @param value
   */
  public static void setTreeValue(Map tree, String[] keys, Object value)
  {
    if (keys.length == 0)
    {
      tree.put(keys[0], value);
    }
    else
    {
      Tree.setTreeValue(tree, keys, value, 0);
    }
  }

  /**
   * 设置树纵深值(类数组键方式"a[b][c]")
   * @param tree
   * @param key
   * @param value
   */
  public static void setArrayValue(Map tree, String key, Object value)
  {
    key = key.replaceAll("\\]\\[", ".")
             .replace("[", ".")
             .replace("]", "" );
    String[] keys = key.split("\\.", -1);
    Tree.setTreeValue(tree, keys, value);
  }

  /**
   * 设置树纵深值(类对象属性方式"a.b.c")
   * @param tree
   * @param key
   * @param value
   */
  public static void setObjectValue(Map tree, String key, Object value)
  {
    String[] keys = key.split("\\.", -1);
    Tree.setTreeValue(tree, keys, value);
  }

  /**
   * 将map2追加到map1中
   * @param map1
   * @param map2
   */
  public static void putAllDeep(Map map1, Map map2) {
      putAllDeep(map1, map2, new ArrayList());
  }

  /**
   * 将map2追加到map1的keys层级中
   * @param map1
   * @param map2
   * @param keys 
   */
  public static void putAllDeep(Map map1, Map map2, String[] keys) {
      putAllDeep(map1, map2, Arrays.asList(keys));
  }
  
  /**
   * 将map2追加到map1的keys层级中
   * @param map1
   * @param map2
   * @param keys 
   */
  public static void putAllDeep(Map map1, Map map2, List<String> keys) {
      Iterator it = map2.entrySet().iterator();
      while (it.hasNext()) {
          Map.Entry et = (Map.Entry) it.next();
          Object key = et.getKey(  );
          Object val = et.getValue();
          List<String> keyz = new ArrayList(keys);
                       keyz.add( key.toString() );
          if (val instanceof Map) {
              putAllDeep( map1, (Map) val, keyz );
          }
          else {
              setTreeValue(map1, keyz.toArray(new String[]{}), val);
          }
      }
  }

}
