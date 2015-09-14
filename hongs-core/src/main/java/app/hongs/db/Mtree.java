package app.hongs.db;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsException;
import app.hongs.util.Synt;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
 * with-path    附带路径, 1信息, 2仅ID
 * </pre>
 *
 * <h3>JS请求参数组合:</h3>
 * <pre>
 * 获取层级: ?pid=xxx
 * 获取节点: ?id=xxx&with-path=1
 * 查找节点: ?wd=xxx&with-path=2
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
    this.rootId = conf.getProperty("fore.tree.root.id",  "0" );
    this.pidKey = conf.getProperty("fore.tree.pid.key", "pid");
    this.bidKey = conf.getProperty("fore.tree.bid.key", "bid");
  }

  //** 标准动作方法 **/

  /**
   * 获取列表
   *
   * 与 Model.getList 不同
   * 不给 colsKey 参数时仅获取基础字段
   * 不给 rowsKey 参数时不进行分页查询
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

    //** 默认字段 **/

    if (!caze.hasSelect() && !rd.containsKey(this.colsKey))
    {
      if (!caze.hasOption("ASSOCS")
      &&  !caze.hasOption("ASSOC_TYPES")
      &&  !caze.hasOption("ASSOC_JOINS"))
      {
        caze.setOption("ASSOCS", new HashSet());
      }

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

    //** 查询列表 **/

    Map  data = new HashMap();
    List list;
    if (!rd.containsKey(this.pageKey)
    &&  !rd.containsKey(this.pagsKey)
    &&  !rd.containsKey(this.rowsKey))
    {
      list = super.getAll (rd , caze);
      data.put("list", list);
    }
    else
    {
      data = super.getList(rd , caze);
      list = (List) data.get( "list");
    }

    //** 附带路径 **/

    int    pth = Synt.declare(rd.get("with-path"), 0 );
    String pid = Synt.declare(rd.get(this.pidKey), "");
    if (pid.length() == 0)
    {
        pid = this.rootId;
    }

    if (pth == 2)
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
    if (pth == 1)
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
    this.chgChildsNum(pid, 1);

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
  public int put(Map data, String id, FetchCase caze)
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
    int    i = super.put (data, id, caze);

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
        this.chgChildsNum(newPid,  1);
        this.chgChildsNum(oldPid, -1);
      }

      if (this.snumKey != null)
      {
        this.setSerialNum(id, -1);
        this.chgSerialNum(oldPid, -1, ordNum, -1);
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
        this.chgSerialNum(id, ordNum2 - ordNum);
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
    this.chgChildsNum(pid, -1);

    // 弟弟节点排序数目减1
    this.chgSerialNum(pid, -1, on, -1);

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
      ids.add( pid );
      if (!pid.equals(rootId))
      {
        ids.addAll(this.getParentIds(pid, rootId));
      }
    }
    return ids;
  }

  public List<Map> getParents(String id, String rootId)
    throws HongsException
  {
    List<Map> nds = new ArrayList<Map>();
    Map pnd  = this.getParent(id);
    if (pnd != null)
    {
      nds.add( pnd );
      String pid = (String) pnd.get(this.pidKey);
      if (!pid.equals(rootId))
      {
        nds.addAll(this.getParents(pid, rootId));
      }
    }
    return nds;
  }

  public List<String> getParentIds(String id)
    throws HongsException
  {
    return this.getParentIds(id, this.rootId);
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
    List      cids = new ArrayList(join.mapped(this.table.primaryKey).keySet());

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

  public List<String> getChildIds(String id)
    throws HongsException
  {
    return this.getChildIds(id, false);
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

  public int getRealChildsNum(String id, Collection excludeIds)
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

  public void chgChildsNum(String id, int off)
    throws HongsException
  {
    if (this.cnumKey == null || off == 0)
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
            + (off > 0 ? "+ "+off : "- "+Math.abs(off)) +
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

  public int getLastSerialNum(String pid, Collection excludeIds)
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
      Set ids = new HashSet();
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

  public void chgSerialNum(String id, int off)
    throws HongsException
  {
    if (this.snumKey == null || off == 0)
    {
      return;
    }

    String pid = this.getParentId(id);
    int oldNum = this.getSerialNum(id);
    int newNum = oldNum + off;
    if (off < 0)
    {
      this.chgSerialNum(pid, +1, newNum, oldNum - 1);
    }
    else
    {
      this.chgSerialNum(pid, -1, oldNum + 1, newNum);
    }

    String sql = "UPDATE `"
            + this.table.tableName +
            "` SET `"
            + this.snumKey +
            "` = `"
            + this.snumKey +
            "` "
            + (off > 0 ? "+ "+off : "- "+Math.abs(off)) +
            " WHERE `"
            + this.table.primaryKey +
            "` = ?";
    List params = new ArrayList();
    params.add(id);
    this.db.execute(sql, params);
  }

  public void chgSerialNum(String pid, int off, int pos1, int pos2)
    throws HongsException
  {
    if (this.snumKey == null || off == 0 || (pos1 < 0 && pos2 < 0))
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
            + (off > 0 ? "+ "+off : "- "+Math.abs(off)) +
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
    String sql;
    String cid;
    int    num = 0;

    if (this.cnumKey != null)
    {
      sql = "SELECT COUNT(`"+this.table.primaryKey  +"`) AS _count_"+
            " FROM  `"      +this.table.tableName   +"`"+
            " WHERE `"      +this.pidKey            +"` = '"+pid+"'"+
            " GROUP BY `"   +this.pidKey            +"`";
      Map row = this.db.fetchOne(sql);
      if (!row.isEmpty())
      {
        num = Synt.declare(row.get("_count_") , num);
        sql = "UPDATE `"    +this.table.tableName   +"`"+
              "  SET  `"    +this.cnumKey           +"` = '"+num+"'"+
              " WHERE `"    +this.table.primaryKey  +"` = '"+pid+"'";
        this.db.execute(sql);
      }
    }

    if (this.snumKey != null)
    {
      sql = "SELECT `"      +this.table.primaryKey  +"`"+
            " FROM  `"      +this.table.tableName   +"`"+
            " WHERE `"      +this.pidKey            +"` = '"+pid+"'"+
            " ORDER BY `"   +this.snumKey           +"`";
      List rows = this.db.fetchAll(sql);
      if (!rows.isEmpty())
      {
        Iterator it = rows.iterator();
        while (it.hasNext())
        {
          Map row = (Map)it.next();
          cid = row.get(this.table.primaryKey).toString();
          sql = "UPDATE `"  +this.table.tableName   +"`"+
                "  SET  `"  +this.snumKey           +"` = '"+num+"'"+
                " WHERE `"  +this.table.primaryKey  +"` = '"+cid+"'";
          this.db.execute(/**/sql);
          this.checkAndRepair(cid);
          num ++;
        }
      }
    }
    else
    {
      List cids = this.getChildIds(pid);
      if (!cids.isEmpty())
      {
        Iterator it = cids.iterator();
        while (it.hasNext())
        {
          cid = (String) it.next();
          this.checkAndRepair(cid);
        }
      }
    }
  }

  public void checkAndRepair()
    throws HongsException
  {
    this.checkAndRepair(this.rootId);
  }

}
