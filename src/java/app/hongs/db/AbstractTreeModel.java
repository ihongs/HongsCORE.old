package app.hongs.db;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import app.hongs.Core;
import app.hongs.HongsException;

/**
 * <h1>树模型</h1>
 * <pre>
 * 用于与树JS组件进行交互
 * </pre>
 *
 * <h2>URL参数说明:</h2>
 * <pre>
 *   pid          获取pid指定的一组节点
 *   ids          获取ids指定的全部节点
 *   only_id      仅获取节点的id
 *   with_sub     获取子级节点
 *   with_path    附带节点路径
 * </pre>
 *
 * <h2>JS请求参数组合:</h2>
 * <pre>
 *   获取一层: ?pid=xxx
 *   查找节点: ?find=xxx&only_id=1
 *   获取节点: ?id=xxx&only_id=1&with_path=1
 *   获取节点: ?ids[]=xxx&only_id=1&with_path=1
 * </pre>
 *
 * @author Hongs
 */
abstract public class AbstractTreeModel
{

  /**
   * 所属库对象
   */
  protected DB db;

  /**
   * 所属表对象
   */
  protected Table table;

  /**
   * 所属模型对象
   */
  protected AbstractBaseModel model;

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
   * 需指定对应的基础模型.
   * 如pid/name等参数或字段名不同,
   * 可在构造时分别指定.
   *
   * @param model
   */
  public AbstractTreeModel(AbstractBaseModel model)
  {
    this.model = model;
    this.table = model.table;
    this.db = model.db;
  }

  public AbstractTreeModel(String modelName)
  {
    this((AbstractBaseModel)Core.getInstance("app.model." + modelName));
  }

  /** 标准动作方法 **/

  /**
   * <b>获取树</p>
   *
   * @param req
   * @param fs
   * @return 树列表
   */
  public Map getTree(Map req, FetchBean fs)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (fs == null)
    {
      fs = new FetchBean();
    }

    if (!fs.hasOption("ASSOC_TABLES")
    &&  !fs.hasOption("ASSOC_TYPES")
    &&  !fs.hasOption("ASSOC_JOINS"))
    {
      fs.setOption("ASSOC_TABLES", new HashSet());
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
      fs.select(".`" + this.table.primaryKey + "` AS `id`");
    }
    else
    {
      fs.select(".`" + this.table.primaryKey + "` AS `id`")
        .select(".`" + this.pidKey  + "` AS `pid`" )
        .select(".`" + this.nameKey + "` AS `name`");

      if (this.noteKey != null)
      {
        fs.select(".`" + this.noteKey + "` AS `note`");
      }
      if (this.typeKey != null)
      {
        fs.select(".`" + this.typeKey + "` AS `type`");
      }
      if (this.cnumKey != null)
      {
        fs.select(".`" + this.cnumKey + "` AS `cnum`");
      }
      else
      {
        fs.select("'1' AS `cnum`");
      }
      if (this.snumKey != null)
      {
        fs.select(".`" + this.snumKey + "` AS `snum`");
      }
      else
      {
        fs.select("'0' AS `snum`");
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

        fs.where("."+this.pidKey+" IN (?)", pids);

        req.remove(this.pidVar);
      }
    }

    Map  data = this.getList(req , fs);
    List list = (List)data.get("list");

    if (withPath)
    {
      if (onlyId)
      {
        List path = this.getPathIds(pid);

        Iterator it = list.iterator();
        while (it.hasNext())
        {
          Map info = (Map)it.next();
          String id = (String)info.get("id");
          List subPath = new ArrayList(path);
          info.put("path", subPath);

          subPath.addAll(this.getPathIds(id, pid));
        }
      }
      else
      {
        List path = this.getPath(pid);

        Iterator it = list.iterator();
        while (it.hasNext())
        {
          Map info = (Map)it.next();
          String id = (String)info.get("id");
          List subPath = new ArrayList(path);
          info.put("path", subPath);

          subPath.addAll(this.getPath(id, pid));
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

  /**
   * 获取列表
   *
   * @param req
   * @param fs
   * @return 单页节点列表
   */
  public Map getPage(Map req, FetchBean fs)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (fs == null)
    {
      fs = new FetchBean();
    }
    this.getFilter(req, fs);

    return this.model.getPage(req, fs);
  }

  /**
   * 获取列表(无查询结构)
   *
   * @param req
   * @return 单页节点列表
   */
  public Map getPage(Map req)
    throws HongsException
  {
    return this.getPage(req, null);
  }

  /**
   * 获取全部节点
   *
   * @param req
   * @param fs
   * @return 全部节点列表
   */
  public Map getList(Map req, FetchBean fs)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (fs == null)
    {
      fs = new FetchBean();
    }
    this.getFilter(req, fs);

    return this.model.getList(req, fs);
  }

  /**
   * 获取全部节点(无查询结构)
   *
   * @param req
   * @return 全部节点列表
   */
  public Map getList(Map req)
    throws HongsException
  {
    return this.getList(req, null);
  }

  /**
   * 获取单个节点(同model.getInfo)
   *
   * @param req
   * @param fs
   * @return 节点信息
   * @throws app.hongs.HongsException
   */
  public Map getInfo(Map req, FetchBean fs)
    throws HongsException
  {
    return this.model.getInfo(req, fs);
  }

  /**
   * 获取单个节点(无查询结构)
   *
   * @param req
   * @return 节点信息
   * @throws app.hongs.HongsException
   */
  public Map getInfo(Map req)
    throws HongsException
  {
    return this.getInfo(req, null);
  }

  /**
   * 添加/修改节点
   *
   * @param req
   * @return 节点ID
   * @throws app.hongs.HongsException
   */
  public String save(Map<String, Object> req)
    throws HongsException
  {
    String id = (String)req.get(this.table.primaryKey);
    if (id != null && id.length() != 0)
    {
      return this.put(id, req);
    }
    else
    {
      return this.add(req);
    }
  }

  /**
   * 更新节点
   *
   * @param req
   * @param fs
   * @return 更新条数
   * @throws app.hongs.HongsException
   */
  public int update(Map req, FetchBean fs)
    throws HongsException
  {
    List<String> ids = new ArrayList();
    if (req.containsKey(this.model.idVar)) {
        Object obj = req.get(this.model.idVar);
        if (obj instanceof List) {
            ids.addAll((List<String>)obj);
        }
        else {
            ids.add(obj.toString());
        }
    }
    if (ids.isEmpty()) this.put("", null);

    int i = 0;
    String pk = this.table.primaryKey;
    fs = fs.clone();
    fs.setSelect(pk).where(pk+" IN (?)", ids);
    List<Map> rows = this.table.fetchMore(fs);
    for (Map  row  : rows)
    {
      this.put(row.get("id").toString(), req);
      i += 1;
    }
    return i;
  }

  public int update(Map req)
    throws HongsException
  {
    return this.update(req, null);
  }

  /**
   * 删除节点
   *
   * @param req
   * @return 删除条数
   * @throws app.hongs.HongsException
   */
  public int remove(Map req, FetchBean fs)
    throws HongsException
  {
    if (req == null)
    {
      req = new HashMap();
    }
    if (fs == null)
    {
      fs = new FetchBean();
    }

    List<String> ids = new ArrayList();
    if (req.containsKey(this.model.idVar)) {
        Object obj = req.get(this.model.idVar);
        if (obj instanceof List) {
            ids.addAll((List<String>)obj);
        }
        else {
            ids.add(obj.toString());
        }
    }
    if (ids.isEmpty()) this.del("", null);

    int i = 0;
    String pk = this.table.primaryKey;
    fs = fs.clone();
    fs.setSelect(".`"+pk+"`").where(".`"+pk+"` IN (?)", ids);
    List<Map> rows = this.table.fetchMore(fs);
    for (Map  row  : rows)
    {
        i += this.del(row.get(pk).toString());
    }
    return i;
  }

  public int remove(Map req)
    throws HongsException
  {
    return this.remove(req, null);
  }

  /**
   * 检查是否存在(同BaseModel.exists)
   *
   * @param req
   * @param fs
   * @return 存在为true, 反之为false
   * @throws app.hongs.HongsException
   */
  public boolean exists(Map req, FetchBean fs)
    throws HongsException
  {
    return this.model.exists(req, fs);
  }

  public boolean exists(Map req)
    throws HongsException
  {
    return this.exists(req, null);
  }

  /** 标准模型方法 **/

  /**
   * 添加节点
   *
   * @param data
   * @return 节点ID
   * @throws app.hongs.HongsException
   */
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

    String id = this.model.add(data);

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

    id = this.model.put(id, data);

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
   * 删除指定节点
   *
   * @param id
   * @return 删除条数
   */
  public int del(String id, FetchBean fs)
    throws HongsException
  {
    String pid = this.getParentId(id);
    int on = this.getSerialNum(id);

    int i = this.model.del(id, fs);

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

  public int del(String id)
    throws HongsException
  {
    return this.del(id, null);
  }

  /**
   * 获取指定节点(同BaseModel.get)
   *
   * @param id
   * @return 节点数据
   * @throws app.hongs.HongsException
   */
  public Map get(String id, FetchBean fs)
    throws HongsException
  {
    return this.model.get(id, fs);
  }

  /**
   * 获取指定节点(无查询结构)
   *
   * @param id
   * @return 节点数据
   * @throws app.hongs.HongsException
   */
  public Map get(String id)
    throws HongsException
  {
    return this.get(id, null);
  }

  /** 私有过滤器 **/

  private void getFilter(Map req, FetchBean fs)
    throws HongsException
  {
    if (!this.pidKey.equals(this.pidVar))
    {
      if (req.containsKey(this.pidVar))
      {
        fs.where(".`" + this.pidKey + "` = ?",
                req.get(this.pidVar).toString());
      }
      else
      if (req.containsKey(this.pidKey+"!"))
      {
        fs.where(".`" + this.pidKey + "` != ?",
                req.get(this.pidVar).toString());
      }
    }

    if (!req.containsKey(this.model.sortVar))
    {
      if (this.snumKey != null)
      {
        fs.orderBy(this.snumKey);
      }
      else if (this.cnumKey != null)
      {
        fs.orderBy("(CASE WHEN `"
                      + this.cnumKey +
                      "` > 0 THEN 1 END) DESC");
      }
    }
  }

  /** 树基础操作 **/

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

  public List<String> getChildIds(String id, boolean all)
    throws HongsException
  {
    String sql;
    if (all && this.cnumKey == null)
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
            + this.table.primaryKey +
            "`, `"
            + this.cnumKey +
            "` FROM `"
            + this.table.tableName +
            "` WHERE `"
            + this.pidKey +
            "` = ?";
    }
    List list = this.db.fetchAll(sql, id);

    Set ids = new HashSet();
    FetchMore fa = new FetchMore(list);
    fa.fetchIds(this.table.primaryKey, ids);
    List cids = new ArrayList(ids);

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
          num = this.getChildsNum(cid);
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
          num = this.getChildsNum(cid);
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

  public List<String> getPathIds(String id, String rootId)
    throws HongsException
  {
    List<String> ids = new ArrayList<String>();
    String pid = this.getParentId(id);
    if (pid != null)
    {
      if (!pid.equals(rootId))
      {
        ids.addAll(this.getPathIds(pid, rootId));
      }
      ids.add(pid);
    }
    return ids;
  }

  public List<String> getPathIds(String id)
    throws HongsException
  {
    return this.getPathIds(id, this.rootId);
  }

  public List<Map> getPath(String id, String rootId)
    throws HongsException
  {
    List<Map> list = new ArrayList<Map>();
    Map info = this.getParent(id);
    if (info != null)
    {
      String pid = (String)info.get(this.pidKey);
      if (pid.equals(rootId))
      {
        list.addAll(this.getPath(pid, rootId));
      }
      list.add(info);
    }
    return list;
  }

  public List<Map> getPath(String id)
    throws HongsException
  {
    return this.getPath(id, this.rootId);
  }

  /** 子数目相关 **/

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

  /** 排序号相关 **/

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

  /** 检查及修复 **/

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
