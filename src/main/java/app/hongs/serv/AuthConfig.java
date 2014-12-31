package app.hongs.serv;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;
import app.hongs.CoreSerially;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;

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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * 结构配置.
 *
 * <p>
 * 该工具会将配置数据自动缓存, 会在构建对象时核对配置的修改时间;
 * 但无法确保其对象在反复使用中会自动重载,
 * 最好在修改配置后删除临时文件并重启应用.
 * </p>
 *
 * <h3>数据结构:</h3>
 * <pre>
 pages = {
   "href" : {
     name: 名称,
     pages : {
       子级页面...
     },
     units : [
       "unit.name1",
       "unit.name2",
       ...
     ]
   }
   ...
 }
 units = {
   "code" : {
     name: 名称,
     depends : [
       "unit.name1",
       "unit.name2",
       ...
     ],
     actions : [
       "auth.name1",
       "auth.name2",
       ...
     ]
   }
   ...
 }
 </pre>
 *
 * <h3>异常代码:</h3>
 * <pre>
 * 区间: 0x10e0~0x10ef
 * 0x10e0 配置文件不存在
 * 0x10e2 解析文件失败
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
  public Map<String, Map>  units;

  /**
   * 全部动作
   */
  public Set<String> actions;

  /**
   * 全部导入
   */
  public Set<String> imports;

  /**
   * 权限名称(会话键或会话类)
   */
  public     String  session;

  public AuthConfig(String name)
    throws HongsException
  {
    this.name = name;
    this.init("site." + name);
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
    this.units = new LinkedHashMap();
    this.actions = new HashSet();
    this.imports = new HashSet();

    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbn = dbf.newDocumentBuilder();
      Document doc = dbn.parse(df);
      Element root = doc.getDocumentElement();

      this.parse(root, this.paths, this.pages, this.units, this.imports, this.actions, new HashSet(), new ArrayList());

      NodeList nodes = root.getElementsByTagName("session");
      if (nodes.getLength() > 0)
      {
        this.session = nodes.item(0).getTextContent();
      }
      else
      {
        CoreConfig c = CoreConfig.getInstance();
        this.session = c.getProperty("core.default.auth.session", "actions");
      }
    }
    catch (IOException ex)
    {
      throw new HongsException(0x10e1, ex);
    }
    catch (SAXException ex)
    {
      throw new HongsException(0x10e1, ex);
    }
    catch (ParserConfigurationException ex)
    {
      throw new HongsException(0x10e1, ex);
    }
  }

  private void parse(Element element, Map paths, Map pages, Map units, Set imports, Set actions, Set depends, List path)
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
        String href = element2.getAttribute("href");
        if (href == null) href = "";
        Map page2 = new HashMap();
        pages.put( href , page2);

        String data = element2.getAttribute("data");
        if (data == null) data = "";
        Map data2 = new HashMap();
        page2.put("data", data2);

        String disp = element2.getAttribute("disp");
        if (disp == null) disp = "";
        page2.put("disp", disp);

        for(String s : data.split(";")) {
            s = s.trim();
            if (s.length() == 0) {
                continue;
            }
            String[] a  = s.split(":", 2);
            String   v;
            if (a.length   == 1) {
                v = a[0].trim();
            } else {
                v = a[1].trim();
            }
            try {
                data2.put(a[1] , URLDecoder.decode( v , "UTF-8" ) );
            } catch (UnsupportedEncodingException ex) {
                throw new HongsException(HongsException.COMMON, ex);
            }
        }

        List path2 = new ArrayList(path);
        path2.add(page2);
        paths.put(href, path2);

        Map pages2 = new LinkedHashMap();
        Map units2 = new LinkedHashMap();

        // 获取下级页面和分组
        this.parse(element2, paths, pages2, units2, imports, actions, depends, path2);

        if (!pages2.isEmpty())
        {
          page2.put("pages", pages2);
        }
        if (!units2.isEmpty())
        {
          page2.put("units", new LinkedHashSet(units2.keySet()));
          units.putAll(units2);
        }
      }
      else
      if ("unit".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map unit2 = new HashMap();
        units.put(namz, unit2);

        String disp = element2.getAttribute("disp");
        if (disp == null) disp = "";
        unit2.put("disp", disp);

        Set actions2 = new HashSet();
        Set depends2 = new HashSet();

        // 获取下级动作
        this.parse(element2, null, null, null, null, actions2, depends2, null);

        if (!actions2.isEmpty())
        {
          unit2.put("actions", actions2);
          actions.addAll(actions2);
        }
        if (!depends2.isEmpty())
        {
          unit2.put("depends", depends2);
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
      if ("import".equals(tagName2))
      {
        String impart = element2.getTextContent();
        AuthConfig conf = new AuthConfig(impart );
        paths.putAll(conf.paths);
        pages.putAll(conf.pages);
        units.putAll(conf.units);
        actions.addAll(conf.actions);
        imports.addAll(conf.imports);
      }
    }
  }

  /**
   * 获取页面信息
   * @param href
   * @return 找不到则返回null
   */
  public Map getPage(String href)
  {
    List path  = this.paths.get(href);
    if ( path == null) return null;
    int  last  = path.size() - 1;
    return (Map) path.get (last);
  }

  /**
   * 获取页面单元
   * @param hrefs
   * @return 单元字典
   */
  public Map<String, Map> getPageUnits(String... hrefs)
  {
    Map<String, Map> unitz = new HashMap();

    for (String herf : hrefs) {
        Map page = getPage(herf);
        Map dict;

        dict = (Map)page.get("units");
        if (dict != null && !dict.isEmpty()) {
            unitz.putAll(getUnits((String[])dict.keySet().toArray(new String[0])));
        }

        dict = (Map)page.get("pages");
        if (dict != null && !dict.isEmpty()) {
            unitz.putAll(getUnits((String[])dict.keySet().toArray(new String[0])));
        }
    }

    return unitz;
  }

  /**
   * 获取页面权限
   * @param hrefs
   * @return 单元字典
   */
  public Set<String> getPageAuths(String... hrefs)
  {
    Set<String> authz = new HashSet();

    for (String herf : hrefs) {
        Map page = getPage(herf);
        Map dict;

        dict = (Map)page.get("units");
        if (dict != null && !dict.isEmpty()) {
            authz.addAll(getAuths((String[])dict.keySet().toArray(new String[0])));
        }

        dict = (Map)page.get("pages");
        if (dict != null && !dict.isEmpty()) {
            authz.addAll(getAuths((String[])dict.keySet().toArray(new String[0])));
        }
    }

    return authz;
  }

  /**
   * 获取单元信息
   * @param name
   * @return 找不到则返回null
   */
  public Map getUnit(String name)
  {
    return this.units.get(name);
  }

  /**
   * 获取更多单元
   * @param names
   * @return 单元字典
   */
  public Map<String, Map> getUnits(String... names)
  {
    Map<String, Map> ds = new HashMap();
    this.getUnitAuths(ds, new HashSet(), names);
    return ds;
  }

  /**
   * 获取单元动作
   * @param names
   * @return 全部动作名
   */
  public Set<String> getAuths(String... names)
  {
    Set<String> as = new HashSet();
    this.getUnitAuths(new HashMap(), as, names);
    return as;
  }

  /**
   * 获取单元和动作
   * @param units
   * @param auths
   * @param names
   */
  public void getUnitAuths(Map units, Set auths, String... names)
  {
    for (String key : names)
    {
      Map unit = this.units.get(key);
      if (unit == null || units.containsKey(key))
      {
        continue;
      }

      units.put(key, unit);

      if (unit.containsKey("actions"))
      {
        Set<String> actionsSet = (Set<String>)unit.get("actions");
        auths.addAll(actionsSet);
      }
      if (unit.containsKey("depends"))
      {
        Set<String> dependsSet = (Set<String>)unit.get("depends");
        String[]    dependsArr = dependsSet.toArray(new String[0]);
        this.getUnitAuths(units, auths, dependsArr);
      }
    }
  }

  /**
   * 获取动作权限集合表
   * @return
   */
  public Set<String> getAuthSet() {
      if (session.contains(".")) {
          return (Set)Core.getInstance (session);
      } else {
          ActionHelper help = (ActionHelper)
            Core.getInstance(ActionHelper.class);
          return (Set)help.getSessValue(session);
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
   * @param href
   * @return 可访问则为true
   */
  public Boolean chkAuth(String href) {
      Set<String> authset = getAuthSet();
      if (authset == null) {
          return false;
      }
      if (authset.size(  ) == 1  &&  authset.contains(null)) {
          return false;
      }
      if (actions.contains(href) && !authset.contains(href)) {
          return false;
      }
      return true;
  }

  /**
   * 检查页面权限
   * @param href
   * @return 有一个动作可访问即返回true
   */
  public Boolean chkPage(String href) {
      Set<String> authz = getPageAuths(href);
      for(String  auth  : authz) {
          if (chkAuth(auth)) {
              return  true;
          }
      }
      return false;
  }

  public List getMenu(int level, int depth) {
      CoreLanguage lang = CoreLanguage.getInstance(this.name).clone();
      for ( String namz : imports) {
          lang.load( namz);
      }
      return getMenu(level, depth, 0, pages, lang);
  }

  private List getMenu(int level, int depth, int i, Map<String, Map> pages, CoreLanguage lang) {
      List list = new ArrayList();

      if (i >= level + depth || pages == null) {
          return list;
      }

      for(Map.Entry item : pages.entrySet( ) ) {
          Map  v = (Map) item.getValue();
          Map  p = (Map) v.get( "pages");
          List a = getMenu(level, depth, i + 1, p, lang);
          if (i >= level) {
              String u = (String) item.getKey();
              String n = (String) v.get("disp");
              if (n == null || "".equals(n)) {
                  n = "core.page."+ u ;
              }
              Map page = new HashMap();
              page.put("href", u);
              page.put("list", a);
              page.put("auth", chkPage(u));
              page.put("name", lang.translate(n));
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
