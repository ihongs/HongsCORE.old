package app.hongs.db;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import app.hongs.Core;
import app.hongs.HongsException;
import java.util.Arrays;

/**
 * <h1>关联查询及更新</h1>
 *
 * <h2>异常代码:</h2>
 * <pre>
 * 区间: 0x10c0~0x10cf
 * 0x10c0 获取行号失败, 可能缺少关联字段
 * 0x10c2 无法识别的关联方式(LINK)
 * 0x10c4 无法识别的关联类型(JOIN)
 * 0x10c6 找不到指定的关联表(LINK)
 * 0x10c8 找不到指定的关联表(JOIN)
 * 0x10ca 关联数据类型必须为Map
 * 0x10cc 关联数据类型必须为Map或List
 * </pre>
 *
 * @author Hongs
 */
public class FetchJoin
{

  private List rows;

  public FetchJoin(List<Map> rows)
  {
    this.rows = rows;
  }

  /**
   * 追加关联数据
   * @param rows 要追加的数据
   * @param col  追加数据的关联键
   * @param key  目标数据的关联键
   * @param name 追加到目标的键
   */
  public void fetchJoin(List<Map> rows, String col, String key, String name)
  {
    fetchJoin(rows, name, col, key, false, false);
  }

  /**
   * 追加关联数据
   * @param rows 要追加的数据
   * @param col  追加数据的关联键
   * @param key  目标数据的关联键
   * @param name 追加到目标的键
   * @param multiAssoc 目标数据与追加数据是一对多的关系
   * @param unityAssoc 将追加数据放入目标数据的同一层下(name将无效)
   */
  public void fetchJoin(List<Map> rows, String col, String key, String name,
                        Boolean multiAssoc, Boolean unityAssoc)
  {
    // 获取id及行号
    Map<String, List> map = new HashMap();
    Set<String> ids = new HashSet();
    this.fetchIdsMap(key, ids, map);

    Iterator rs = rows.iterator(  );

    Map     row, sub;
    List    lst;
    String  id;

    if (! multiAssoc)
    {
      while (rs.hasNext())
      {
        sub = ( Map  ) rs.next(   );
        id  = (String) sub.get(col);
        lst = ( List ) map.get(id );

        if (lst == null)
        {
          //throw new HongsException(0x10c0, "Line nums is null");
          continue;
        }

        Iterator it = lst.iterator();
        while (it.hasNext())
        {
          row = (Map) it.next();

          if (! unityAssoc)
          {
            row.put(name, sub);
          }
          else
          {
            sub.putAll(row);
            row.putAll(sub);
          }
        }
      }
    }
    else
    {
      while (rs.hasNext())
      {
        sub = ( Map  ) rs.next(   );
        id  = (String) sub.get(col);
        lst = ( List ) map.get(id );

        if (lst == null)
        {
          //throw new HongsException(0x10c0, "Line nums is null");
          continue;
        }

        Iterator it = lst.iterator();
        while (it.hasNext())
        {
          row = (Map) it.next();

          if (row.containsKey(name))
          {
            (( List ) row.get(name)).add(sub);
          }
          else
          {
            List lzt = new ArrayList();
            row.put(name, lzt);
            lzt.add(sub);
          }
        }
      }
    }
  }

  /**
   * 获取关联数据
   *
   * @param table 关联表
   * @param col   关联字段
   * @param key   源表关联键
   * @throws app.hongs.HongsException
   */
  public void fetchJoin(Table table, String col, String key)
    throws HongsException
  {
    this.fetchJoin(table, col, key, new FetchMore());
  }

  /**
   * 获取关联数据
   *
   * @param table 关联表
   * @param col   关联字段
   * @param key   源表关联键
   * @param fs    限制查询结构
   * @throws app.hongs.HongsException
   */
  public void fetchJoin(Table table, String col, String key, FetchMore fs)
    throws HongsException
  {
    if (this.rows.isEmpty())
    {
      return;
    }

    DB db = table.db;
    String       name   = fs.name;
    String  tableName   = table.tableName;
    boolean multiAssoc  = fs.getOption("MULTI_ASSOC", false);
    boolean unityAssoc  = fs.getOption("UNITY_ASSOC", false);

    if (name == null || name.length() == 0)
    {
        name = table.name;
    }

    // 获取id及行号
    Map<String, List> map = new HashMap();
    Set<String> ids = new HashSet();
    this.fetchIdsMap(key, ids, map);

    if (ids.isEmpty() || map.isEmpty())
    {
      //throw new HongsException(0x10c0, "Ids map is null");
      return;
    }

    // 识别字段别名
    String col2 = col;
    if (!table.getColumns().containsKey(col))
    {
      Pattern pattern = Pattern.compile(
             "^(.+?)\\s+(?:AS\\s+)?`?(.+?)`?$",
                     Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(col);
      if (matcher.find())
      {
        col  = matcher.group(1);
        col2 = matcher.group(2);
      }
    }
    else
    {
      col = ".`" + col + "`";
    }

    // 构建查询结构
    fs.from (tableName, name)
      .where(col+" IN (?)", ids);

    /**
     * 根据 id 获取关联数据,
     * 并根据之前的 id=>行 关系以表名为键放入列表中
     */

    FetchNext rs = db.query(fs.getSQL(), fs.getParams());

    Map     row, sub;
    List    lst;
    String  id;

    if (! multiAssoc)
    {
      while ((sub = rs.fetch()) != null)
      {
        id  = (String) sub.get(col2);
        lst = ( List ) map.get( id );

        if (lst == null)
        {
          //throw new HongsException(0x10c0, "Line nums is null");
          continue;
        }

        Iterator it = lst.iterator();
        while (it.hasNext())
        {
          row = (Map) it.next();

          if (! unityAssoc)
          {
            row.put(name, sub);
          }
          else
          {
            sub.putAll(row);
            row.putAll(sub);
          }
        }
      }
    }
    else
    {
      while ((sub = rs.fetch()) != null)
      {
        id  = (String) sub.get(col2);
        lst = ( List ) map.get( id );

        if (lst == null)
        {
          //throw new HongsException(0x10c0, "Line nums is null");
          continue;
        }

        Iterator it = lst.iterator();
        while (it.hasNext())
        {
          row = (Map) it.next();

          if (row.containsKey(name))
          {
            (( List ) row.get(name)).add(sub);
          }
          else
          {
            List lzt = new ArrayList();
            row.put(name, lzt);
            lzt.add(sub);
          }
        }
      }
    }
  }

  /**
   * 获取关联ID和行
   *
   * @param key 使用"."分割的键
   * @param ids
   * @param map
   */
  public void fetchIdsMap(String key, Set<String> ids, Map<String, List> map)
  {
    if (key.startsWith( ":" ))
    {
        key = key.substring(1);
    }
    this.fetchIdsMap(key.split("\\."), ids, map);
  }

  /**
   * 获取关联ID和行
   *
   * @param keys
   * @param ids
   * @param map
   */
  private void fetchIdsMap(String[] keys, Set<String> ids, Map<String, List> map)
  {
    Iterator it = this.rows.iterator();
    W:while (it.hasNext())
    {
      Object row = it.next();
      Object obj = row;

      // 获取id值
      for (int i = 0; i < keys.length; i ++)
      {
        if (obj instanceof Map)
        {
          obj = ((Map)obj).get(keys[i]);
        }
        else
        if (obj instanceof List)
        {
          List rowz = this.rows;
          this.rows = (List)obj;

          // 切割子键数组
          int j = keys.length - i ;
          String[] keyz = new String[j];
          System.arraycopy(keys, i, keyz, 0, j);

          // 往下递归一层
          this.fetchIdsMap(keyz, ids, map);

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

      // 登记ID
      String  id = obj.toString();
      ids.add(id);

      // 登记行
      if (map.containsKey(id))
      {
        map.get(id).add(row);
      }
      else
      {
        List lst = new ArrayList();
        map.put(id, lst);
        lst.add(row);
      }
    }
  }

  /**
   * 获取关联ID
   *
   * @param key 使用"."分割的键
   * @param ids
   */
  public void fetchIds(String key, Set<String> ids)
  {
    Map map = new HashMap();
    this.fetchIdsMap(key.split("\\."), ids, map);
  }

  /**
   * 获取关联行
   *
   * @param key 使用"."分割的键
   * @param map
   */
  public void fetchMap(String key, Map<String, List> map)
  {
    Set ids = new HashSet();
    this.fetchIdsMap(key.split("\\."), ids, map);
  }

  /** 静态方法 **/

  /**
   * 关联查询
   * @param table 主表
   * @param assocs 关联配置
   * @param bean 查询体
   * @return 结果列表
   * @throws HongsException
   */
  public static List assocSelect
    (Table table, Map assocs, FetchMore bean)
  throws HongsException {
    if ( assocs == null ) assocs = new HashMap();

    List<Map> lnks = new ArrayList();

    assocSelect(table, assocs, bean, lnks);
    List rows = table.db.fetchMore( bean );
    assocSelect(table, assocs, bean, lnks, rows);

    return rows;
  }

  private static void assocSelect
    (Table table, Map assocs, FetchMore bean, List lnks)
  throws HongsException {
    Set tps = (Set)bean.getOption("ASSOC_TYPES" );
    Set jns = (Set)bean.getOption("ASSOC_JOINS" );
    Set tns = (Set)bean.getOption("ASSOC_TABLES");
    String tn = bean.name;
    if (tn == null || tn.length() == 0)
           tn = bean.tableName;

    for(Map.Entry et : (Set<Map.Entry>)assocs.entrySet()) {
        Map assoc = (Map) et.getValue();
        String tp = (String)assoc.get("type");
        String jn = (String)assoc.get("join");
        String an = (String)assoc.get("name");
        String rn = (String)assoc.get("realName");
        if (rn == null || rn.length() == 0) rn = an;

        // 检查是否许可关联
        if (tns != null && !tns.contains(rn) && !tns.contains(an)) {
            continue;
        }
        if (tps != null && !tps.contains(tp)) {
            continue;
        }
        if (jn  != null &&  jn.length() != 0) {
        if (jns != null && !jns.contains(jn)) {
            continue;
        }}  else {
            // 非JOIN表先放到一边
            assoc.put("linkName", tn);
            lnks .add(assoc);
            continue;
        }

        Map  assocs2 = (Map) assoc.get("assocs");
        Table table2 = table.db.getTable(  rn  );
        FetchMore bean2 = bean.join(an)
                              .from(table2.tableName);
        String fk = (String)assoc.get("foreignKey");
        String pk = (String)assoc.get("primaryKey");

        // 建立关联关系
        if ("BLS_TO".equals(tp)) {
            // 上级外键连接下级主键, 交换主外键
            String xk = fk; fk = pk; pk = xk;
            if (fk == null) fk = table2.primaryKey;
        }
        else if ("HAS_ONE".equals(tp)) {
            // 上级主键连接下级外键
            if (pk == null) pk = table .primaryKey;
        }
        else if ("HAS_MANY".equals(tp)) {
            throw new HongsException(0x10c2, "Unsupported assoc type '"+tp+"'");
        }
        else {
            throw new HongsException(0x10c2, "Unrecognized assoc type '"+tp+"'");
        }
        if (tn == null || tn.length() == 0) {
            fk =         ":`"+fk+"`";
        } else {
            fk = "`"+tn+"`.`"+fk+"`";
        }   pk = "`"+an+"`.`"+pk+"`";

        // 转化关联类型
        short ji;
        if ("INNER".equals(jn)) {
            ji = FetchMore.INNER;
        }
        else if ("LEFT".equals(jn)) {
            ji = FetchMore.LEFT;
        }
        else if ("RIGHT".equals(jn)) {
            ji = FetchMore.RIGHT;
        }
        else if ("FULL".equals(jn)) {
            ji = FetchMore.FULL;
        }
        else if ("CROSS".equals(jn)) {
            throw new HongsException(0x10c4, "Unsupported assoc join '"+jn+"'");
        }
        else {
            throw new HongsException(0x10c4, "Unrecognized assoc join '"+jn+"'");
        }

        // 设置关联关系
        bean2.setJoin(pk+"="+fk, ji);

        setBean(bean2,assoc);

        if (assocs2 != null) {
            assocSelect(table2, assocs2, bean2, lnks);
        }
    }
  }

  private static void assocSelect
    (Table table, Map assocs, FetchMore bean, List lnks, List rows)
  throws HongsException {
    Set tps = (Set)bean.getOption("ASSOC_TYPES" );
    Set tns = (Set)bean.getOption("ASSOC_TABLES");
    FetchJoin more = new FetchJoin(rows);

    while (!lnks.isEmpty()) {
        List lnks2 = new ArrayList();
    for (Map assoc : ( List<Map> ) lnks) {
        String tp = (String)assoc.get("type");
        String an = (String)assoc.get("name");
        String rn = (String)assoc.get("realName");
        String tn = (String)assoc.get("linkName");
        if (rn == null || rn.length() == 0) rn = an;

        // 检查是否许可关联
        if (tns != null && !tns.contains(rn) && !tns.contains(an)) {
            continue;
        }
        if (tps != null && !tps.contains(tp)) {
            continue;
        }

        Map  assocs2 = (Map) assoc.get("assocs");
        Table table2 = table.db.getTable(  rn  );
        FetchMore bean2 = bean.join(an)
                              .from(table2.tableName);
        String fk = (String)assoc.get("foreignKey");
        String pk = (String)assoc.get("primaryKey");

        // 准备关联关系
        if ("BLS_TO".equals(tp)) {
            // 上级外键连接下级主键, 交换主外键
            String xk = fk; fk = pk; pk = xk;
            if (fk == null) fk = table2.primaryKey;
        }
        else if ("HAS_ONE".equals(tp)) {
            // 上级主键连接下级外键
            if (pk == null) pk = table .primaryKey;
            bean2.setOption("MULTI_ASSOC", false);
        }
        else if ("HAS_MANY".equals(tp)) {
            // 上级主键连接下级外键
            if (pk == null) pk = table .primaryKey;
            bean2.setOption("MULTI_ASSOC", true );
        }
        else {
            throw new HongsException(0x10c2, "Unrecognized assoc type '"+tp+"'");
        }
        if (tn != null && tn.length() != 0 && !tn.equals(bean.name)) {
            pk = tn+"."+pk;
        }

        setBean(bean2,assoc);

        if (assocs2 != null) {
            assocSelect(table2, assocs2, bean2, lnks2);
        }

        more.fetchJoin (table2, fk , pk, bean2);
    }
        lnks = lnks2;
    }
  }

  private static void setBean(FetchMore bean, Map assoc) {
    String str;
    str = (String)assoc.get("select");
    if (str != null && str.length() != 0) {
        bean.select(str);
    }
    str = (String)assoc.get("where");
    if (str != null && str.length() != 0) {
        bean.where(str);
    }
    str = (String)assoc.get("groupBy");
    if (str != null && str.length() != 0) {
        bean.groupBy(str);
    }
    str = (String)assoc.get("having");
    if (str != null && str.length() != 0) {
        bean.having(str);
    }
    str = (String)assoc.get("orderBy");
    if (str != null && str.length() != 0) {
        bean.orderBy(str);
    }
  }

    /**
     * 关联更新
     *
     * @param table 主表
     * @param rows 要插入的数据
     * @param keys 判断改变的键
     * @param where 更新/删除范围
     * @param params where 的参数
     * @throws HongsException
     */
    public static void assocUpdate(
        Table           table,
        List<Map>       rows,
        List<String>    keys,
        String          where,
        Object...       params
    )   throws HongsException
    {
        List<Object> params1 = Arrays.asList(params);
        List<Object> params2;
        Object[]     params3;

        StringBuilder where2 = new StringBuilder(where);
        String        where3;
        for (String k : keys)
        {
            where2.append(" AND `"+k+"`=?" );
        }
        where3 = where2.toString();

        List ids = new ArrayList();

        for (Map row : rows)
        {
            params2 = new ArrayList(params1);
            for (String k : keys)
            {
                params2.add(row.get(k));
            }
            params3 = params2.toArray();

            String sql = "SELECT `"+table.primaryKey+"` FROM `"+table.tableName+"` WHERE "+where2;
            Map<String, Object> one = table.db.fetchOne( sql , params3 );
            if (!one.isEmpty())
            {
                //  有则更新
                if (!row.containsKey(table.primaryKey) || "".equals(row.get(table.primaryKey)))
                    row.put(table.primaryKey, one.get(table.primaryKey));
                table.update(row, where3, params3);
            }
            else
            {
                // 没则插入
                if (!row.containsKey(table.primaryKey) || "".equals(row.get(table.primaryKey)))
                    row.put(table.primaryKey, Core.getUniqueId());
                table.insert(row);
            }

            ids.add(row.get(table.primaryKey));
        }

        // 删除多余
        where2 = new StringBuilder(where);
        where2.append(" AND `"+table.primaryKey+"` NOT IN (?)");
        params2 = new ArrayList(params1 );
        params2.add(ids);
        table .delete( where2.toString( ), params2.toArray( ) );
    }

  /**
   * 关联插入
   *
   * 关联配置中有指定 updateKeys 的话, 会调用 assocUpdate 进行更新
   *
   * @param table 主表
   * @param assocs 关联配置
   * @param values 要插入的数据
   * @throws app.hongs.HongsException
   */
  protected static void assocInsert(Table table, Map assocs, Map values)
    throws HongsException
  {
    if ( assocs == null || assocs.isEmpty() ) return;

    String id = (String)values.get(table.primaryKey);

    Iterator it = assocs.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry)it.next();
      Map config = (Map)entry.getValue();

      String type = (String)config.get("type");
      String name = (String)config.get("name");
      String realName = (String)config.get("realName");
      String foreignKey = (String)config.get("foreignKey");

      if (!values.containsKey(name))
      {
        continue;
      }
      if (!type.equals("HAS_ONE") && !type.equals("HAS_MANY"))
      {
        continue;
      }
      if (realName == null || realName.length() == 0)
      {
          realName =  name;
      }

      Table tb = table.db.getTable(realName);
      List  pa = new ArrayList();
            pa.add(id);

      // 都整理成List方便处理
      Object subValues = values.get(name);
      List subValues2 = new ArrayList();
      if ("HAS_ONE".equals(type))
      {
        if (subValues instanceof Map)
        {
          subValues2.add( subValues );
        }
        else
        {
          throw new HongsException(0x10ca,
          "Sub data type for table '"+tb.name+"' must be Map");
        }
      }
      else
      {
        if (subValues instanceof List)
        {
          subValues2.addAll((List)subValues);
        }
        else
        if (subValues instanceof Map)
        {
          subValues2.addAll(((Map)subValues).values());
        }
        else
        {
          throw new HongsException(0x10cc,
          "Sub data type for table '"+tb.name+"' must be Map or List");
        }
      }

      /**
       * Add by Hong on 2013/6/6
       * 有时候子数据会被其他数据引用, 如果更新子数据, 子数据的ID就会改变.
       * 通常这种情况存在以下规则: 如果某些字段值没发生改变则不要重新插入.
       * 所以当有指定updateKeys时, 使用assocUpdate方法更新数据, 其原理为:
       * 找出没改变的数据并更新, 然后插入新增数据, 最后删除更新和新增之外的数据.
       */
      List<String> updateKeys = (List<String>) config.get( "updateKeys" );
      if (updateKeys != null && !updateKeys.isEmpty())
      {
        // 填充外键
        Iterator it2 = subValues2.iterator();
        while (it2.hasNext())
        {
          Map subValues3=(Map)it2.next();
          subValues3.put(foreignKey, id);
        }

        assocUpdate(tb, subValues2, updateKeys, "`"+foreignKey+"`=?", pa);
      }
      else
      {
        // 先删除旧数据
        tb.delete("`"+foreignKey+"`=?" , pa);

        // 再插入新数据
        Iterator it2 = subValues2.iterator();
        while (it2.hasNext())
        {
          Map subValues3=(Map)it2.next();
          subValues3.put(foreignKey, id);

          // 如果存在主键而没给定主键值,则帮其添加上唯一ID
          if (tb.primaryKey != null && tb.primaryKey.length() != 0
          &&  ! subValues3.containsKey(tb.primaryKey)) {
            subValues3.put(tb.primaryKey, Core.getUniqueId());
          }

          tb.insert(subValues3);
        }
      }
    }
  }

  /**
   * 关联删除
   *
   * @param table 主表
   * @param assocs 关联配置
   * @param id 要删除的外键
   * @throws app.hongs.HongsException
   */
  protected static void assocDelete(Table table, Map assocs, String id)
    throws HongsException
  {
    if (assocs.isEmpty())
    {
      return;
    }

    Iterator it = assocs.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry)it.next();
      Map config = (Map)entry.getValue();

      String type = (String)config.get("type");
      String name = (String)config.get("name");
      String realName = (String)config.get("realName");
      String foreignKey = (String)config.get("foreignKey");

      if (!type.equals("HAS_ONE") && !type.equals("HAS_MANY"))
      {
        continue;
      }
      if (realName == null || realName.length() == 0)
      {
          realName =  name;
      }

      Table tb = table.db.getTable(realName);
      List  pa = new ArrayList();
            pa.add(id);

      // 直接删除数据
      tb.delete("`"+foreignKey+"`=?", pa);
    }
  }

}
