package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;
import app.hongs.CoreSerially;
import app.hongs.HongsException;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * 动作配置
 *
 * <p>
 * 该工具会将配置数据自动缓存, 会在构建对象时核对配置的修改时间;
 * 但无法确保其对象在反复使用中会自动重载,
 * 最好在修改配置后删除临时文件并重启应用.
 * </p>
 *
 * <h3>配置规范:</h3>
 * <pre>
 * page(页面)   有uri和name(名称)两个属性, 相同的uri可以对应不同的name,
 *              可包含多个group(分组), 可包含下级page(页面);
 * group(分组)  有key和name(名称)两个属性, 相同的key只能对应相同的name,
 *              可包含多个action(动作), 可包含下级group(分组);
 * actoin(动作) 仅能包含一个动作串(为动作uri);
 * depend(依赖) 仅能包含一个分组键(为分组key).
 * </pre>
 *
 * <h3>数据结构:</h3>
 * <pre>
 * pages = Map{
 *   "uri" : Map{
 *     uri : "uri",
 *     name : "name",
 *     pages : Map{
 *       子级页面...
 *     },
 *     groups : Set[
 *       "group.key1",
 *       "group.key2",
 *       ...
 *     ]
 *   }
 *   ...
 * }
 * groups = Map{
 *   "key" : Map{
 *     key : "key",
 *     name : "name",
 *     depends : Set[
 *       "group.key1",
 *       "group.key2",
 *       ...
 *     ],
 *     actions : Set[
 *       "action.uri1",
 *       "action.uri2",
 *       ...
 *     ]
 *   }
 *   ...
 * }
 * </pre>
 *
 * <h3>异常代码:</h3>
 * <pre>
 * 区间: 0x10e0~0x10ef
 * 0x10e0 动作配置文件不存在
 * 0x10e2 解析配置文件失败
 * </pre>
 *
 * @author Hongs
 */
public class AuthConfig
  extends CoreSerially
{

  private String name;

  /**
   * 页面路径信息
   */
  public Map<String, List> paths;

  /**
   * 页面层级信息
   */
  public Map<String, Map>  pages;

  /**
   * 全部分组信息
   */
  public Map<String, Map> groups;

  /**
   * 全部动作
   */
  public Set<String>     actions;

  /**
   * 权限名称(会话键或会话类)
   */
  public     String      session;

  public AuthConfig(String name)
    throws HongsException
  {
    this.name = name;
    this.init("auth." + name);
  }

  @Override
  protected boolean expired(long time)
  {
    File xmlFile = new File(Core.CONF_PATH
                + File.separator + name + ".auth.xml");
    File serFile = new File(Core.SERS_PATH
                + File.separator + name + ".auth.ser");
    return xmlFile.lastModified() > serFile.lastModified();
  }

  @Override
  protected void imports()
    throws HongsException
  {
    File df = new File(Core.CONF_PATH
                + File.separator + name + ".auth.xml");
    if (!df.exists())
    {
      throw new HongsException(0x10e0, "Auth config file '"
                + Core.CONF_PATH
                + File.separator + name + ".auth.xml"
                + "' is not exists");
    }

    this.paths = new HashMap();
    this.pages = new LinkedHashMap();
    this.groups = new LinkedHashMap();
    this.actions = new HashSet();

    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbn = dbf.newDocumentBuilder();
      Document doc = dbn.parse(df);
      Element root = doc.getDocumentElement();

      this.parseActionTree(root,
        new ArrayList(), this.paths, this.pages, this.groups, this.actions, new HashSet());

      /**
       * 提取会话名(类)
       * Add by Hongs, 2013/12/25
       */
      CoreConfig c = CoreConfig.getInstance();
      this.session = c.getProperty("core.default.auth.session", "actions");
      NodeList nodes = root.getElementsByTagName("config");
      if (nodes.getLength( ) > 0) {
          Element e = ( Element ) nodes.item(0);
          String  s = e.getAttribute("session");
          if (s != null && !"".equals(s)) this.session = s;
      }

      // 测试
      /*
      app.hongs.util.Dump.dump("Paths", paths);
      app.hongs.util.Dump.dump("Pages", pages);
      app.hongs.util.Dump.dump("Groups", groups);
      app.hongs.util.Dump.dump("Actions", actions);
      */
    }
    catch (IOException ex)
    {
      throw new HongsException(0x10e2, ex);
    }
    catch (SAXException ex)
    {
      throw new HongsException(0x10e2, ex);
    }
    catch (ParserConfigurationException ex)
    {
      throw new HongsException(0x10e2, ex);
    }
  }

  private void parseActionTree(Element element, List path, Map paths, Map pages, Map groups, Set actions, Set depends)
    throws HongsException
  {
    if (!element.hasChildNodes())
    {
      return;
    }

    NodeList nodes = element.getChildNodes();

    for (int i = 0; i < nodes.getLength(); i ++)
    {
      Node node = nodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE)
      {
        continue;
      }

      Element element2 = (Element)node;
      String  tagName2 = element2.getTagName();

      if (path == null
      && !"action".equals(tagName2)
      && !"depend".equals(tagName2)
      )
      {
        continue;
      }

      if ("page".equals(tagName2))
      {
        String uri = element2.getAttribute("uri");
        String namz = element2.getAttribute("name");
        if (uri == null) uri = "";
        if (namz == null) namz = "";
        Map page2 = new HashMap();
        page2.put("uri", uri);
        page2.put("name", namz);
        pages.put(uri, page2);

        List path2 = new ArrayList(path);
        path2.add(page2);
        paths.put(uri, path2);

        Map pages2 = new LinkedHashMap();
        Map groups2 = new LinkedHashMap();

        // 获取下级页面和分组
        this.parseActionTree(element2, path2, paths, pages2, groups2, actions, depends);

        if (!pages2.isEmpty())
        {
          page2.put("pages", pages2);
        }
        if (!groups2.isEmpty())
        {
          page2.put("groups", new LinkedHashSet(groups2.keySet()));
          groups.putAll(groups2);
        }
      }
      else
      if ("group".equals(tagName2))
      {
        String key = element2.getAttribute("key");
        String namz = element2.getAttribute("name");
        if (key == null) key = "";
        if (namz == null) namz = "";
        Map group2 = new HashMap();
        group2.put("key", key);
        group2.put("name", namz);
        groups.put(key, group2);

        Set actions2 = new HashSet();
        Set depends2 = new HashSet();

        // 获取下级动作
        this.parseActionTree(element2, null, null, null, null, actions2, depends2);

        if (!actions2.isEmpty())
        {
          group2.put("actions", actions2);
          actions.addAll(actions2);
        }
        if (!depends2.isEmpty())
        {
          group2.put("depends", depends2);
          depends.addAll(depends2);
        }
      }
      else
      if ("action".equals(tagName2))
      {
        String action = element2.getTextContent();
        actions.add(action);
      }
      else
      if ("depend".equals(tagName2))
      {
        String depend = element2.getTextContent();
        depends.add(depend);
      }
      else
      if ("include".equals(tagName2))
      {
        String    include = element2.getTextContent();
        AuthConfig conf = new AuthConfig(include);
        paths.putAll(conf.paths);
        pages.putAll(conf.pages);
        groups.putAll(conf.groups);
        actions.addAll(conf.actions);
      }
    }
  }

  /**
   * 获取页面信息
   * @param uri
   * @return 找不到则返回null
   */
  public Map getPage(String uri)
  {
    if (this.paths.containsKey(uri))
    {
      List path = this.paths.get(uri);
      int last = path.size() - 1;
      return (Map)path.get(last);
    }
    else
    {
      return null;
    }
  }

  /**
   * 获取页面路径
   * @param uri
   * @return 找不到则返回null
   */
  public List getPath(String uri)
  {
    return this.paths.get(uri);
  }

  /**
   * 获取分组信息
   * @param key
   * @return 找不到则返回null
   */
  public Map getGroup(String key)
  {
    return this.groups.get(key);
  }

  /**
   * 获取页面分组
   * @param urls
   * @return 页面分组信息
   */
  public Map<String, Map> getPageGroups(String... urls)
  {
    Map<String, Map> groupz = new HashMap();

    for (String url : urls) {
        Map page = getPage(url);

        Map gs = (Map)page.get("groups");
        if (gs != null && !gs.isEmpty()) {
            gs = getTotalGroups((String[])gs.keySet().toArray(new String[0]));
            groupz.putAll(gs);
        }

        Map ps = (Map)page.get( "pages");
        if (ps != null && !ps.isEmpty()) {
            gs =  getPageGroups((String[])ps.keySet().toArray(new String[0]));
            groupz.putAll(gs);
        }
    }

    return groupz;
  }

  /**
   * 获取全部分组
   * @param keys
   * @return 全部分组信息
   */
  public Map<String, Map> getTotalGroups(String... keys)
  {
    Map<String, Map> gs = new HashMap();
    this.getGroupsActions(gs, new HashSet(), keys);
    return gs;
  }

  /**
   * 获取全部动作
   * @param keys
   * @return 全部动作名
   */
  public Set<String> getGroupActions(String... keys)
  {
    Set<String> as = new HashSet();
    this.getGroupsActions(new HashMap(), as, keys);
    return as;
  }

  /**
   * 获取全部分组和动作
   * @param grps
   * @param acts
   * @param keys
   */
  public void getGroupsActions(Map grps, Set acts, String... keys)
  {
    for (String key : keys)
    {
      Map group = this.groups.get(key);
      if (group == null || grps.containsKey(key))
      {
        continue;
      }

      grps.put(key, group);

      if (group.containsKey("actions"))
      {
        Set<String> actionsSet = (Set<String>)group.get("actions");
        acts.addAll(actionsSet);
      }
      if (group.containsKey("depends"))
      {
        Set<String> dependsSet = (Set<String>)group.get("depends");
        String[]    dependsArr = dependsSet.toArray(new String[0]);
        this.getGroupsActions(grps, acts, dependsArr);
      }
    }
  }

  /**
   * 获取动作权限集合表
   * @return 
   */
  public Set<String> getAuthSet() {
      if (! session.contains(".")) {
          ActionHelper help = (ActionHelper)
            Core.getInstance(ActionHelper.class);
          return (Set) help.getSession (session);
      }
      else {
          return (Set) Core.getInstance(session);
      }
  }

  /**
   * 获取动作权限对照表
   * @return 
   */
  public Map<String, Boolean> getAuthMap() {
      Set<String> authset = getAuthSet();
      if (authset == null || (authset.size() == 1 && authset.contains(null))) {
          return null;
      }
      Map<String, Boolean> map = new HashMap();
      for (String act : actions) {
          map.put(act , false);
      }
      for (String act : authset) {
          map.put(act , true );
      }
      return map;
  }

  /**
   * 检查动作权限
   * @param url
   * @return 可访问则为true
   */
  public Boolean chkAuth(String url) {
      Set<String> authset = getAuthSet();
      if (authset == null) {
          return false;
      }
      if (authset.size(  ) == 1 && authset.contains(null)) {
          return false;
      }
      if (actions.contains(url) && !authset.contains(url)) {
          return false;
      }
      return true;
  }

  /**
   * 检查页面权限
   * @param url
   * @return 有一个动作可访问即返回true
   */
  public Boolean chkPage(String url) {
      Map<String, Map> grps = getPageGroups(url);
      if (grps == null || grps.isEmpty()) {
          return  true;
      }
      Set<String> acts = getGroupActions((String[])grps.keySet().toArray(new String[0]));
      if (acts == null || acts.isEmpty()) {
          return  true;
      }
      for (String act : acts) {
          if (! chkAuth(act)) {
              continue;
          }
          return  true;
      }
      return false;
  }

    public List getNavList(CoreLanguage lang, int level, int depth)
    throws HongsException
    {
        return getNavList(lang, pages, level, depth, 0);
    }

    private List getNavList(CoreLanguage lang, Map<String, Map> pages, int level, int depth, int i) {
        List list = new ArrayList();

        if (i >= level + depth || pages == null) {
            return list;
        }

        for (Map.Entry item : pages.entrySet( )) {
            Map  v = (Map)item.getValue();
            Map  p = (Map) v.get("pages");
            List a = getNavList(lang, p, level, depth, i + 1);
            if (i >= level) {
                String u = (String)item.getKey();
                String n = (String)v.get("name");
                Map page = new HashMap();
                n = lang.translate(n);
                page.put("uri" , u);
                page.put("name", n);
                page.put("list", a);
                page.put("auth", chkPage(u));
                list.add(page);
            } else {
                list.addAll(a);
            }
        }

        return list;
    }

  //** 工厂方法 **/

  public static AuthConfig getInstance(String name) throws HongsException {
      String key = AuthConfig.class.getName() + ":" + name;
      Core core = Core.getInstance();
      AuthConfig inst;
      if (core.containsKey(key)) {
          inst = (AuthConfig)core.get(key);
      }
      else {
          inst = new AuthConfig(name);
          core.put( key, inst );
      }
      return inst;
  }

  public static AuthConfig getInstance() throws HongsException {
      return getInstance("default");
  }
}
