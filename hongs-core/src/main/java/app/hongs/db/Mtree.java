package app.hongs.db;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 树形模型
 *
 * <p>
 * 用于与树JS组件进行交互
 * </p>
 *
 * <h3>URL参数说明:</h3>
 * <pre>
 * pid          获取pid 指定的一组节点
 * id[]         获取id[]指定的全部节点
 * get_id       仅获取节点的id
 * get_path     附带节点的路径
 * </pre>
 *
 * <h3>JS请求参数组合:</h3>
 * <pre>
 * 获取一层: ?pid=xxx
 * 查找节点: ?find=xxx&get_id=1
 * 获取节点: ?id=xxx&get_id=1&get_path=1
 * 获取节点: ?id[]=xxx&get_id=1&get_path=1
 * </pre>
 *
 * @author Hong
 */
public class Mtree extends Model
{

  /**
   * 根节点id
   * 用于确认是路径深度
   */
  protected String rootId = "0";

  /**
   * 参考id参数名
   * 影响put, 用于移动节点时指定顺序
   */
  protected String bidKey = "bid";

  /**
   * 父级id字段名
   */
  protected String pidKey = "pid";

  /**
   * 名称字段名
   */
  protected String nameKey = "name";

  /**
   * 说明字段名(非必要)
   */
  protected String noteKey = null;

  /**
   * 类型字段名(非必要)
   */
  protected String typeKey = null;

  /**
   * 子数目字段名(非必要)
   */
  protected String cnumKey = null;

  /**
   * 排序号字段名(非必要)
   */
  protected String snumKey = null;

  /**
   * 构造方法
   *
   * 需指定该模型对应的表对象.
   * 如传递的newPid,bid参数名不同,
   * 或newPid,name等字段名不同,
   * 或rootId不同,
   * 可在构造时分别指定;
   * 请指定被搜索的字段.
   *
   * @param table
   * @throws app.hongs.HongsException
   */
  public Mtree(Table table)
    throws HongsException
  {
    super(table);
    
    CoreConfig conf = Core.getInstance(CoreConfig.class);
    this.bidKey = conf.getProperty("fore.tree.bid.key", "bid");
    this.rootId = conf.getProperty("fore.tree.root.id",  "0" );
  }

  //** 标准动作方法 **/

  /**
   * <b>获取树</p>
   *
   * @param rd
   * @param caze
   * @return 树列表
   */
  @Override
  public Map getList(Map rd, FetchCase caze)
    throws HongsException
  {
    if (rd == null)
    {
      rd = new HashMap();
    }
    if (caze == null)
    {
      caze = new FetchCase();
    }

    if (!caze.hasOption("ASSOCS")
    &&  !caze.hasOption("ASSOC_TYPES")
    &&  !caze.hasOption("ASSOC_JOINS"))
    {
      caze.setOption("ASSOCS", new HashSet());
    }

    String pid = (String) rd.get(this.pidKey);
    if (pid == null || pid.length() == 0)
    {
      pid =  this.rootId;
    }

    // 这些参数为约定参数
    boolean getId   = rd.containsKey("get_id"  )
                    && rd.get("get_id"  ).equals("1");
    boolean getPath = rd.containsKey("get_path")
                    && rd.get("get_path").equals("1");
    rd.remove("get_id"  );
    rd.remove("get_path");

    if (getId)
    {
      caze.select(".`" + this.table.primaryKey + "`");
    }
    else
    {
      caze.select(".`" + this.table.primaryKey + "`")
          .select(".`" + this.pidKey  + "`")
          .select(".`" + this.nameKey + "`");

      if (this.noteKey != null)
      {
        caze.select(".`" + this.noteKey + "`");
      }
      if (this.typeKey != null)
      {
        caze.select(".`" + this.typeKey + "`");
      }
      if (this.cnumKey != null)
      {
        caze.select(".`" + this.cnumKey + "`");
      }
      else
      {
        caze.select("'1' AS `"+ cnumKey + "`");
      }
      if (this.snumKey != null)
      {
        caze.select(".`" + this.snumKey + "`");
      }
      else
      {
        caze.select("'0' AS `"+ snumKey + "`");
      }
    }

    List list = this.getAll(rd, caze);

    if (getPath)
    {
      if (getId)
      {
        List path = this.getParentIds(pid);

        Iterator it = list.iterator();
        while (it.hasNext())
        {
          Map info = (Map)it.next();
          String id = (String)info.get(this.table.primaryKey);
          List subPath = new ArrayList(path);
          info.put("path", subPath);

          subPath.addAll(this.getParentIds(id, pid));
        }
      }
      else
      {
        List path = this.getParents(pid);

        Iterator it = list.iterator();
        while (it.hasNext())
        {
          Map info = (Map)it.next();
          String id = (String)info.get(this.table.primaryKey);
          List subPath = new ArrayList(path);
          info.put("path", subPath);

          subPath.addAll(this.getParents(id, pid));
        }
      }
    }

    Map data = new HashMap();
    data.put( "list", list );
    return data;
  }

  /**
   * 获取树
   *
   * @param rd
   * @return 树列表
   */
  @Override
  public Map getList(Map rd)
    throws HongsException
  {
    return this.getList(rd, null);
  }

  //** 标准模型方法 **/

  /**
   * 添加节点
   *
   * @param data
   * @return 节点ID
   * @throws app.hongs.HongsException
   */
  @Override
  public String add(Map data)
    throws HongsException
  {
    if (data == null)
    {
      data = new HashMap();
    }

    String pid = (String)data.get(this.pidKey);

    // 默认加到根节点下
    if (pid == null || pid.length() == 0)
    {
      pid = this.rootId;
      data.put(this.pidKey, pid);
    }

    // 默认添加到末尾
    if (!data.containsKey(this.snumKey))
    {
      int num = this.getLastSerialNum(pid);
      data.put(this.snumKey, num + 1);
    }

    // 默认没有子节点
    if (!data.containsKey(this.cnumKey))
    {
      data.put(this.cnumKey, "0");
    }

    String  id = super.add(data);

    // 将父节点的子节点数量加1
    this.setChildsOffset(pid, 1);

    return  id;
  }

  /**
   * 更新节点
   *
   * @param id
   * @param data
   * @return 节点ID
   * @throws app.hongs.HongsException
   */
  @Override
  public int put(String id, FetchCase caze, Map data)
    throws HongsException
  {
    if (data == null)
    {
      data = new HashMap();
    }

    /**
     * 如有指定bid(BeforeID)
     * 则将新的pid(ParentID)重设为其pid
     */
    String bid = (String)data.get(this.bidKey);
    if (null != bid && !"".equals(bid))
    {
      data.put(this.pidKey,this.getParentId(bid));
    }

    String newPid = (String)data.get(this.pidKey);
    String oldPid = this.getParentId (id);
    int    ordNum = this.getSerialNum(id);

    int i = super.put(id, caze, data);

    /**
     * 如果有指定新的pid且不同于旧的pid, 则
     * 将其新的父级子节点数目加1
     * 将其旧的父级子节点数目减1
     * 将其置于新父级列表的末尾
     * 并将旧的弟节点序号往前加1
     */
    if(null != newPid && !"".equals(newPid) && !oldPid.equals(newPid))
    {
      if (this.cnumKey != null)
      {
        this.setChildsOffset(newPid,  1);
        this.setChildsOffset(oldPid, -1);
      }

      if (this.snumKey != null)
      {
        this.setSerialNum(id, -1);
        this.setSerialOffset(oldPid, -1, ordNum, -1);
      }
    }

    /**
     * 如果有指定bid
     * 且其位置有所改变
     * 则将节点排序置为bid前
     */
    if (this.snumKey != null)
    {
      ordNum = this.getSerialNum(id);
      int ordNum2 = -1;

      if (null != bid && !"".equals(bid))
      {
        ordNum2 = this.getSerialNum(bid);
        if (ordNum2 > ordNum)
        {
          ordNum2 -= 1;
        }
      }

      if (ordNum2 > -1 && ordNum2 != ordNum)
      {
        this.setSerialOffset(id, ordNum2 - ordNum);
      }
    }

    return i;
  }

  /**
   * 删除节点
   *
   * @param id
   * @param caze
   * @return 删除条数
   */
  @Override
  public int del(String id, FetchCase caze)
    throws HongsException
  {
    String pid = this.getParentId(id);
    int on = this.getSerialNum(id);

    int i = super.del(id, caze);

    // 父级节点子节点数目减1
    this.setChildsOffset(pid, -1);

    // 弟弟节点排序数目减1
    this.setSerialOffset(pid, -1, on, -1);

    // 删除全部子节点
    List ids = this.getChildIds(id);
    Iterator it = ids.iterator();
    while (it.hasNext()) {
      this.del((String)it.next());
    }

    return i;
  }

  @Override
  protected void filter(FetchCase caze, Map rd)
    throws HongsException
  {
    super.filter(caze, rd);

    if (!rd.containsKey(this.sortKey))
    {
      if (this.snumKey != null)
      {
        caze.orderBy(this.snumKey);
      }
      else if (this.cnumKey != null)
      {
        caze.orderBy("(CASE WHEN `"
             + this.cnumKey +
           "` > 0 THEN 1 END) DESC");
      }
    }
  }

  //** 树基础操作 **/

  public String getParentId(String id)
    throws HongsException
  {
    String sql = "SELECT `"
            + this.pidKey +
            "` FROM `"
            + this.table.tableName +
            "` WHERE `"
            + this.table.primaryKey +
            "` = ?";
    Map info = this.db.fetchOne(sql, id);
    return (String)info.get(this.pidKey);
  }

  public Map getParent(String id)
    throws HongsException
  {
    String sql = "SELECT * FROM `"
            + this.table.tableName +
            "` WHERE `"
            + this.table.primaryKey +
            "` = ?";
    return this.db.fetchOne(sql, id);
  }

  public List<String> getParentIds(String id, String rootId)
    throws HongsException
  {
    List<String> ids = new ArrayList<String>();
    String pid = this.getParentId(id);
    if (pid != null)
    {
      if (!pid.equals(rootId))
      {
        ids.addAll(this.getParentIds(pid, rootId));
      }
      ids.add(pid);
    }
    return ids;
  }

  public List<String> getParentIds(String id)
    throws HongsException
  {
    return this.getParentIds(id, this.rootId);
  }

  public List<Map> getParents(String id, String rootId)
    throws HongsException
  {
    List<Map> list = new ArrayList<Map>();
    Map info = this.getParent(id);
    if (info != null)
    {
      String pid = (String)info.get(this.pidKey);
      if (pid.equals(rootId))
      {
        list.addAll(this.getParents(pid, rootId));
      }
      list.add(info);
    }
    return list;
  }

  public List<Map> getParents(String id)
    throws HongsException
  {
    return this.getParents(id, this.rootId);
  }

  public List<String> getChildIds(String id, boolean all)
    throws HongsException
  {
    String sql;
    if (this.cnumKey == null)
    {
      sql = "SELECT `"
            + this.table.primaryKey +
            "` FROM `"
            + this.table.tableName +
            "` WHERE `"
            + this.pidKey +
            "` = ?";
    }
    else
    {
      sql = "SELECT `"
            + this.cnumKey +
            "`, `"
            + this.table.primaryKey +
            "` FROM `"
            + this.table.tableName +
            "` WHERE `"
            + this.pidKey +
            "` = ?";
    }
    List list = this.db.fetchAll(sql,id);

    FetchMore join = new FetchMore(list);
    Set        ids = join.getIds(this.table.primaryKey);
    List      cids = new ArrayList( ids);

    if (all)
    {
      Iterator it = list.iterator();
      while (it.hasNext())
      {
        Map info = (Map)it.next();
        String cid = (String)info.get(this.table.primaryKey);

        int num;
        if (this.cnumKey == null)
        {
          num = 1; // 总是尝试获取就行了 //this.getChildsNum(cid);
        }
        else
        {
          num = Integer.parseInt((String)info.get(this.cnumKey));
        }

        if (num > 0)
        {
          cids.addAll(this.getChildIds(cid, all));
        }
      }
    }

    return cids;
  }

  public List<String> getChildIds(String id)
    throws HongsException
  {
    return this.getChildIds(id, false);
  }

  public List<Map> getChilds(String id, boolean all)
    throws HongsException
  {
    String sql;
    sql = "SELECT * FROM `"
            + this.table.tableName +
            "` WHERE `"
            + this.pidKey +
            "` = ?";
    List list = this.db.fetchAll(sql,id);

    if (all)
    {
      Iterator it = list.iterator();
      while (it.hasNext())
      {
        Map info = (Map)it.next();
        String cid = (String)info.get(this.table.primaryKey);

        int num;
        if (this.cnumKey == null)
        {
          num = 1; // 总是尝试获取就行了 //this.getChildsNum(cid);
        }
        else
        {
          num = Integer.parseInt((String)info.get(this.cnumKey));
        }

        if (num > 0)
        {
          list.addAll(this.getChildIds(cid, all));
        }
      }
    }

    return list;
  }

  public List<Map> getChilds(String id)
    throws HongsException
  {
    return this.getChilds(id, false);
  }

  //** 子数目相关 **/

  public int getChildsNum(String id)
    throws HongsException
  {
    if (this.cnumKey == null)
    {
      return this.getRealChildsNum(id, null);
    }

    String sql = "SELECT `"
            + this.cnumKey +
            "` FROM `"
            + this.table.tableName +
            "` WHERE `"
            + this.table.primaryKey +
            "` = ?";
    List params = new ArrayList();
    params.add(id);

    Map info = this.db.fetchOne(sql, params.toArray());
    if (info.isEmpty())
    {
      return 0;
    }

    Object cn = info.get(this.cnumKey);
    return Integer.parseInt(cn.toString());
  }

  public int getRealChildsNum(String id, List excludeIds)
    throws HongsException
  {
    String sql = "SELECT COUNT(`"
            + this.table.primaryKey +
            "`) AS __count__ FROM `"
            + this.table.tableName +
            "` WHERE `"
            + this.pidKey +
            "` = ?";
    List params = new ArrayList();
    params.add(id);

    if (excludeIds != null)
    {
      sql += " `" + this.table.primaryKey + "` NOT IN (?)";
      params.add(excludeIds);
    }

    Map info = this.db.fetchOne(sql, params.toArray());
    if (info.isEmpty())
    {
      return 0;
    }

    Object cn = info.get("__count__");
    return Integer.parseInt(cn.toString());
  }

  public int getRealChildsNum(String id)
    throws HongsException
  {
    return this.getRealChildsNum(id, null);
  }

  public void setChildsNum(String id, int num)
    throws HongsException
  {
    if (this.cnumKey == null)
    {
      return;
    }

    if (num < 0)
    {
      num = this.getRealChildsNum(id) + Math.abs(num);
    }

    String sql = "UPDATE `"
            + this.table.tableName +
            "` SET `"
            + this.cnumKey +
            "` = ?" +
            " WHERE `"
            + this.table.primaryKey
            + "` = ?";
    List params = new ArrayList();
    params.add(num);
    params.add(id);
    this.db.execute(sql, params);
  }

  public void setChildsOffset(String id, int offset)
    throws HongsException
  {
    if (this.cnumKey == null || offset == 0)
    {
      return;
    }

    String sql = "UPDATE `"
            + this.table.tableName +
            "` SET `"
            + this.cnumKey +
            "` = `"
            + this.cnumKey +
            "` "
            + (offset > 0 ? "+ "+offset : "- "+Math.abs(offset)) +
            " WHERE `"
            + this.table.primaryKey +
            "` = ?";
    List params = new ArrayList();
    params.add(id);
    this.db.execute(sql, params);
  }

  //** 排序号相关 **/

  public int getSerialNum(String id)
    throws HongsException
  {
    if (this.snumKey == null)
    {
      return 0;
    }

    String sql = "SELECT `"
            + this.snumKey +
            "` FROM `"
            + this.table.tableName +
            "` WHERE `"
            + this.table.primaryKey +
            "` = ?";

    Map info = this.db.fetchOne(sql, id);
    if (info.isEmpty())
    {
      return 0;
    }

    Object on = info.get(this.snumKey);
    return Integer.parseInt(on.toString());
  }

  public int getLastSerialNum(String pid, List excludeIds)
    throws HongsException
  {
    if (this.snumKey == null)
    {
      return this.getRealChildsNum(pid, excludeIds);
    }

    String sql = "SELECT `"
            + this.snumKey +
            "` FROM `"
            + this.table.tableName +
            "` WHERE `"
            + this.pidKey +
            "` = ?";
    List params = new ArrayList();
    params.add(pid);

    if (excludeIds != null)
    {
      sql += " AND `" + this.table.primaryKey + "` NOT IN (?)";
      params.add(excludeIds);
    }

    sql += " ORDER BY `" + this.snumKey + "` DESC";

    Map info = this.db.fetchOne(sql, params.toArray());
    if (info.isEmpty())
    {
      return 0;
    }

    Object on = info.get(this.snumKey);
    return Integer.parseInt(on.toString());
  }

  public int getLastSerialNum(String pid)
    throws HongsException
  {
    return this.getLastSerialNum(pid, null);
  }

  public void setSerialNum(String id, int num)
    throws HongsException
  {
    if (this.snumKey == null)
    {
      return;
    }

    if (num < 0)
    {
      String pid = this.getParentId(id);
      List ids = new ArrayList();
      ids.add(id);
      num = this.getLastSerialNum(pid, ids) + Math.abs(num);
    }

    String sql = "UPDATE `"
            + this.table.tableName +
            "` SET `"
            + this.snumKey +
            "` = ?" +
            " WHERE `"
            + this.table.primaryKey +
            "` = ?";
    List params = new ArrayList();
    params.add(num);
    params.add(id);
    this.db.execute(sql, params);
  }

  public void setSerialOffset(String id, int offset)
    throws HongsException
  {
    if (this.snumKey == null || offset == 0)
    {
      return;
    }

    String pid = this.getParentId(id);
    int oldNum = this.getSerialNum(id);
    int newNum = oldNum + offset;
    if (offset < 0)
    {
      this.setSerialOffset(pid, +1, newNum, oldNum - 1);
    }
    else
    {
      this.setSerialOffset(pid, -1, oldNum + 1, newNum);
    }

    String sql = "UPDATE `"
            + this.table.tableName +
            "` SET `"
            + this.snumKey +
            "` = `"
            + this.snumKey +
            "` "
            + (offset > 0 ? "+ "+offset : "- "+Math.abs(offset)) +
            " WHERE `"
            + this.table.primaryKey +
            "` = ?";
    List params = new ArrayList();
    params.add(id);
    this.db.execute(sql, params);
  }

  public void setSerialOffset(String pid, int offset, int pos1, int pos2)
    throws HongsException
  {
    if (this.snumKey == null || offset == 0 || (pos1 < 0 && pos2 < 0))
    {
      return;
    }

    if (pos1 > pos2 && pos1 >= 0 && pos2 >= 0)
    {
      int pos3 = pos1;
      pos1 = pos2;
      pos2 = pos3;
    }

    String sqlAnd = "";
    if (pos1 > -1)
    {
      sqlAnd += " AND `"+this.snumKey+"` >= "+pos1;
    }
    if (pos2 > -1)
    {
      sqlAnd += " AND `"+this.snumKey+"` <= "+pos2;
    }

    String sql = "UPDATE `"
            + this.table.tableName +
            "` SET `"
            + this.snumKey +
            "` = `"
            + this.snumKey +
            "` "
            + (offset > 0 ? "+ "+offset : "- "+Math.abs(offset)) +
            " WHERE `"
            + this.pidKey +
            "` = ?"
            + sqlAnd;
    List params = new ArrayList();
    params.add(pid);
    this.db.execute(sql, params);
  }

  //** 检查及修复 **/

  public void checkAndRepair(String pid)
    throws HongsException
  {
    if (this.cnumKey != null)
    {
      String sql = "SELECT COUNT(`"+this.table.primaryKey+"`) AS __count__" +
              " FROM `"+this.table.tableName+"`" +
              " WHERE `"+this.pidKey+"` = '"+pid+"'" +
              " GROUP BY `"+this.pidKey+"`" +
              " LIMIT 1";
      Map row = this.db.fetchOne(sql);
      if (!row.isEmpty())
      {
        String sql2 = "UPDATE `"+this.table.tableName+"`" +
                " SET `"+this.cnumKey+"` = '"+row.get("__count__").toString()+"'" +
                " WHERE `"+this.table.primaryKey+"` = '"+pid+"'" +
                " LIMIT 1";
        this.db.execute(sql2);
      }
    }

    if (this.snumKey != null)
    {
      String sql = "SELECT `"+this.table.primaryKey+"`" +
              " FROM `"+this.table.tableName+"`" +
              " WHERE `"+this.pidKey+"` = '"+pid+"'" +
              " ORDER BY `"+this.snumKey+"` ASC";
      List rows = this.db.fetchAll(sql);
      if (!rows.isEmpty())
      {
        Iterator it = rows.iterator();
        int i = 1;
        while (it.hasNext())
        {
          Map row = (Map)it.next();
          String sql2 = "UPDATE `"+this.table.tableName+"`" +
                " SET `"+this.snumKey+"` = '"+i+"'" +
                " WHERE `"+this.table.primaryKey+"` = '"+row.get(this.table.primaryKey).toString()+"'" +
                " LIMIT 1";
          this.db.execute(sql2);
          i ++;
        }
      }
    }

    List cids = this.getChildIds(pid);
    Iterator it = cids.iterator();
    while (it.hasNext())
    {
      String cid = (String)it.next();
      this.checkAndRepair(cid);
    }
  }

  public void checkAndRepair()
    throws HongsException
  {
    this.checkAndRepair(this.rootId);
  }

}