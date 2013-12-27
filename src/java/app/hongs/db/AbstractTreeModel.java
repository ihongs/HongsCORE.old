package app.hongs.db;

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
 *   pid          获取pid指定的一组节点
 *   ids          获取ids指定的全部节点
 *   only_id      仅获取节点的id
 *   with_sub     获取子级节点
 *   with_path    附带节点路径
 * </pre>
 *
 * <h3>JS请求参数组合:</h3>
 * <pre>
 *   获取一层: ?pid=xxx
 *   查找节点: ?find=xxx&only_id=1
 *   获取节点: ?id=xxx&only_id=1&with_path=1
 *   获取节点: ?ids[]=xxx&only_id=1&with_path=1
 * </pre>
 *
 * @author Hong
 */
public class AbstractTreeModel extends AbstractBaseModel
{

  /**
   * 根节点id
   * 用于确认是路径深度
   */
  protected String rootId = "0";

  /**
   * 父级id参数名
   * 用于getAll/getList/getTree
   */
  protected String pidVar = "pid";

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
   * 如传递的id/ids等参数名不同,
   * 可在构造时分别指定;
   * 请指定被搜索的字段.
   *
   * @param table
   */
  public AbstractTreeModel(Table table)
    throws HongsException
  {
    super(table);
  }
  public AbstractTreeModel(String tableName)
    throws HongsException
  {
    this("default", tableName);
  }
  public AbstractTreeModel(String dbName, String tableName)
    throws HongsException
  {
    this(DB.getInstance(dbName).getTable(tableName));
  }

  //** 标准动作方法 **/

  /**
   * <b>获取树</p>
   *
   * @param req
   * @param more
   * @return 树列表
   */
  public Map getTree(Map req, FetchMore more)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (more == null)
    {
      more = new FetchMore();
    }

    if (!more.hasOption("ASSOC_TABLES")
    &&  !more.hasOption("ASSOC_TYPES")
    &&  !more.hasOption("ASSOC_JOINS"))
    {
      more.setOption("ASSOC_TABLES", new HashSet());
    }

    boolean onlyId = req.containsKey("only_id")
                  && req.get("only_id").equals("1");
    boolean withSub = req.containsKey("with_sub")
                   && req.get("with_sub").equals("1");
    boolean withPath = req.containsKey("with_path")
                    && req.get("with_path").equals("1");
    req.remove("only_id");
    req.remove("with_sub");
    req.remove("with_path");

    if (onlyId)
    {
      more.select(".`" + this.table.primaryKey + "` AS `id`");
    }
    else
    {
      more.select(".`" + this.table.primaryKey + "` AS `id`")
        .select(".`" + this.pidKey  + "` AS `pid`" )
        .select(".`" + this.nameKey + "` AS `name`");

      if (this.noteKey != null)
      {
        more.select(".`" + this.noteKey + "` AS `note`");
      }
      if (this.typeKey != null)
      {
        more.select(".`" + this.typeKey + "` AS `type`");
      }
      if (this.cnumKey != null)
      {
        more.select(".`" + this.cnumKey + "` AS `cnum`");
      }
      else
      {
        more.select("'1' AS `cnum`");
      }
      if (this.snumKey != null)
      {
        more.select(".`" + this.snumKey + "` AS `snum`");
      }
      else
      {
        more.select("'0' AS `snum`");
      }
    }

    String pid = (String)req.get(this.pidVar);
    if (pid == null || pid.length() == 0)
        pid =  this.rootId;

    if (withSub)
    {
      if (!pid.equals(this.rootId))
      {
        List pids = this.getChildIds(pid, true);
        pids.add(0, pid);

        more.where("."+this.pidKey+" IN (?)", pids);

        req.remove(this.pidVar);
      }
    }

    Map  data = this.getList(req , more);
    List list = (List)data.get("list");

    if (withPath)
    {
      if (onlyId)
      {
        List path = this.getParentIds(pid);

        Iterator it = list.iterator();
        while (it.hasNext())
        {
          Map info = (Map)it.next();
          String id = (String)info.get("id");
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
          String id = (String)info.get("id");
          List subPath = new ArrayList(path);
          info.put("path", subPath);

          subPath.addAll(this.getParents(id, pid));
        }
      }
    }

    return data;
  }

  /**
   * 获取树
   *
   * @param req
   * @return 树列表
   */
  public Map getTree(Map req)
    throws HongsException
  {
    return this.getTree(req, null);
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

    String id = super.add(data);

    // 将父节点的子节点数量加1
    this.setChildsOffset(pid, 1);

    return id;
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
  public String put(String id, Map data)
    throws HongsException
  {
    if (data == null)
    {
      data = new HashMap();
    }

    /**
     * 如果有指定aid(AfterID)或bid(BeforeID)
     * 则将新的pid(ParentID)重设为其pid
     */
    String aid = (String)data.get("aid");
    String bid = (String)data.get("bid");
    if (null != aid && !"".equals(aid))
    {
      data.put(this.pidKey, this.getParentId(aid));
    }
    else
    if (null != bid && !"".equals(bid))
    {
      data.put(this.pidKey, this.getParentId(bid));
    }

    String newPid = (String)data.get(this.pidKey);
    String oldPid = this.getParentId(id);
    int ordNum = this.getSerialNum(id);

    id = super.put(id, data);

    /**
     * 如果有指定新的pid且不同于旧的pid
     * 则将其新的父级子节点数目加1
     * 将其旧的父级子节点数目减1
     * 同时将排序设为末尾
     * 并将旧的弟弟节点均往前移动1位
     */
    if(null != newPid && !"".equals(newPid) && !oldPid.equals(newPid))
    {
      if (this.cnumKey != null)
      {
        this.setChildsOffset(newPid, 1);
        this.setChildsOffset(oldPid, -1);
      }

      if (this.snumKey != null)
      {
        this.setSerialNum(id, -1);
        this.setSerialOffset(oldPid, -1, ordNum, -1);
      }
    }

    /**
     * 如果有指定aid或bid
     * 且其位置有所改变
     * 则将改节点排序置为aid后或bid前
     */
    if (this.snumKey != null)
    {
      ordNum = this.getSerialNum(id);
      int ordNum2 = -1;

      if (null != aid && !"".equals(aid))
      {
        ordNum2 = this.getSerialNum(aid);
        if (ordNum2 < ordNum)
        {
          ordNum2 += 1;
        }
      }
      else
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

    return id;
  }

  /**
   * 删除节点
   *
   * @param id
   * @param more
   * @return 删除条数
   */
  @Override
  public int del(String id, FetchMore more)
    throws HongsException
  {
    String pid = this.getParentId(id);
    int on = this.getSerialNum(id);

    int i = super.del(id, more);

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
  protected void getFilter(Map req, FetchMore more)
    throws HongsException
  {
    super.getFilter(req, more);

    if (!this.pidKey.equals(this.pidVar))
    {
      if (req.containsKey(this.pidVar))
      {
        more.where(".`" + this.pidKey + "` = ?",
                req.get(this.pidVar).toString());
      }
      else
      if (req.containsKey(this.pidKey+"!"))
      {
        more.where(".`" + this.pidKey + "` != ?",
                  req.get(this.pidVar).toString());
      }
    }

    if (!req.containsKey(this.sortVar))
    {
      if (this.snumKey != null)
      {
        more.orderBy(this.snumKey);
      }
      else if (this.cnumKey != null)
      {
        more.orderBy("(CASE WHEN `"
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

    Set        ids = new  HashSet ();
    FetchJoin join = new FetchJoin(list);
    join.fetchIds(this.table.primaryKey,ids);
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
    List list = this.db.fetchAll(sql, id);

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

  public int getChildsNum(String id, List excludeIds)
    throws HongsException
  {
    if (this.cnumKey == null)
    {
      return this.getRealChildsNum(id, excludeIds);
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

    Object cn = info.get(this.cnumKey);
    return Integer.parseInt(cn.toString());
  }

  public int getChildsNum(String id)
    throws HongsException
  {
    return this.getChildsNum(id, null);
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
      return this.getChildsNum(pid, excludeIds);
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
