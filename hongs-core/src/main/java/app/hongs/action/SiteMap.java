package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;
import app.hongs.CoreSerially;
import app.hongs.HongsError;
import app.hongs.HongsException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
     roles : [
       "role.name1",
       "role.name2",
       ...
     ]
   }
   ...
 }
 roles = {
   "code" : {
     name: 名称,
     depends : [
       "fole.name1",
       "role.name2",
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
public class SiteMap
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
  public Map<String, Map>  roles;

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

  public SiteMap(String name)
    throws HongsException
  {
    this.name = name;
    this.init(name + ".as");
  }

  @Override
  protected boolean expired(long time)
  {
    File xmlFile = new File(Core.CONF_PATH
                + File.separator + name + ".as.xml");
    File serFile = new File(Core.SERS_PATH
                + File.separator + name + ".as.ser");
    if (xmlFile.exists())
    {
      return xmlFile.lastModified() > serFile.lastModified();
    }
    else
    {
      return false;
    }
  }

  @Override
  protected void imports()
    throws HongsException
  {
    InputStream is;
    String      fn;

    try
    {
        fn = Core.CONF_PATH + File.separator + name + ".as.xml";
        is = new FileInputStream(fn);
    }
    catch (FileNotFoundException ex)
    {
        fn = name.contains("/") ? name : "app/hongs/config/" + name + ".as.xml";
        is = this.getClass().getClassLoader().getResourceAsStream(fn);
        if (  is  ==  null )
        {
            throw new app.hongs.HongsError(0x2a, "Can not find the sitemap config file '" + fn + "'.");
        }
    }

    Element root;
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbn = dbf.newDocumentBuilder();
      Document  doc = dbn.parse( is );
      root = doc.getDocumentElement();

      NodeList nodes = root.getElementsByTagName("ssname");
      if (nodes.getLength() > 0)
      {
        this.session = nodes.item(0).getTextContent();
      }
      else
      {
        CoreConfig c = CoreConfig.getInstance();
        this.session = c.getProperty("core.default.auth.session", "roles");
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

    this.paths = new HashMap();
    this.pages = new LinkedHashMap();
    this.roles = new LinkedHashMap();
    this.actions = new HashSet();
    this.imports = new HashSet();

    this.parse(root, this.paths, this.pages, this.roles, this.imports, this.actions, new HashSet(), new ArrayList());
  }

  private void parse(Element element, Map paths, Map pages, Map roles, Set imports, Set actions, Set depends, List path)
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
        Map roles2 = new LinkedHashMap();

        // 获取下级页面和分组
        this.parse(element2, paths, pages2, roles2, imports, actions, depends, path2);

        if (!pages2.isEmpty())
        {
          page2.put("pages", pages2);
        }
        if (!roles2.isEmpty())
        {
          page2.put("roles", new LinkedHashSet(roles2.keySet()));
          roles.putAll(roles2);
        }
      }
      else
      if ("role".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map role2 = new HashMap();
        roles.put(namz, role2);

        String disp = element2.getAttribute("disp");
        if (disp == null) disp = "";
        role2.put("disp", disp);

        Set actions2 = new HashSet();
        Set depends2 = new HashSet();

        // 获取下级动作
        this.parse(element2, null, null, null, null, actions2, depends2, null);

        if (!actions2.isEmpty())
        {
          role2.put("actions", actions2);
          actions.addAll(actions2);
        }
        if (!depends2.isEmpty())
        {
          role2.put("depends", depends2);
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
        SiteMap conf = new SiteMap(impart );
        paths.putAll(conf.paths);
        pages.putAll(conf.pages);
        roles.putAll(conf.roles);
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
  public Map<String, Map> getPageRoles(String... hrefs)
  {
    Map<String, Map> rolez = new HashMap();

    for (String herf : hrefs) {
        Map page = getPage(herf);
        Map dict;

        dict = (Map)page.get("roles");
        if (dict != null && !dict.isEmpty()) {
            rolez.putAll(getMoreRoles((String[])dict.keySet().toArray(new String[0])));
        }

        dict = (Map)page.get("pages");
        if (dict != null && !dict.isEmpty()) {
            rolez.putAll(getMoreRoles((String[])dict.keySet().toArray(new String[0])));
        }
    }

    return rolez;
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

        dict = (Map)page.get("foles");
        if (dict != null && !dict.isEmpty()) {
            authz.addAll(SiteMap.this.getRoleAuths((String[])dict.keySet().toArray(new String[0])));
        }

        dict = (Map)page.get("pages");
        if (dict != null && !dict.isEmpty()) {
            authz.addAll(SiteMap.this.getRoleAuths((String[])dict.keySet().toArray(new String[0])));
        }
    }

    return authz;
  }

  /**
   * 获取单元信息
   * @param name
   * @return 找不到则返回null
   */
  public Map getRole(String name)
  {
    return this.roles.get(name);
  }

  /**
   * 获取更多单元
   * @param names
   * @return 单元字典
   */
  public Map<String, Map> getMoreRoles(String... names)
  {
    Map<String, Map> ds = new HashMap();
    this.getRoleAuths(ds, new HashSet(), names);
    return ds;
  }

  /**
   * 获取单元动作
   * @param names
   * @return 全部动作名
   */
  public Set<String> getRoleAuths(String... names)
  {
    Set<String> as = new HashSet();
    this.getRoleAuths(new HashMap(), as, names);
    return as;
  }

  /**
   * 获取单元和动作
   * @param roles
   * @param auths
   * @param names
   */
  public void getRoleAuths(Map roles, Set auths, String... names)
  {
    for (String key : names)
    {
      Map role = this.roles.get(key);
      if (role == null || roles.containsKey(key))
      {
        continue;
      }

      roles.put(key, role);

      if (role.containsKey("actions"))
      {
        Set<String> actionsSet = (Set<String>)role.get("actions");
        auths.addAll(actionsSet);
      }
      if (role.containsKey("depends"))
      {
        Set<String> dependsSet = (Set<String>)role.get("depends");
        String[]    dependsArr = dependsSet.toArray(new String[0]);
        this.getRoleAuths(roles, auths, dependsArr);
      }
    }
  }

  /**
   * 获取动作角色集合表(与当前请求相关)
   * @return
   */
  public Set<String> getRoleSet() {
      if (session.contains(".")) {
          return (Set)Core.getInstance (session);
      } else {
          ActionHelper help = (ActionHelper)
            Core.getInstance(ActionHelper.class);
          return (Set)help.getSessValue(session);
      }
  }

  /**
   * 获取动作权限集合表(与当前请求相关)
   * @return
   */
  public Set<String> getAuthSet() {
      Set<String> roles = getRoleSet();
      return getRoleAuths(roles.toArray(new String[0]));
  }

  /**
   * 获取动作权限对照表(与当前请求相关)
   * @return
   */
  public Map<String, Boolean> getAuthMap() {
      Set<String> authset = getAuthSet();
      if (authset == null || (authset.size() == 1 && authset.contains(null))) {
          return null;
      }
      Map<String, Boolean> map = new HashMap();
      for (String act : actions) {
          map.put(act , authset.contains(act));
      }
      return map;
  }

  /**
   * 检查角色权限(与当前请求相关)
   * @param role
   * @return 可访问则为true
   */
  public Boolean chkRole(String role) {
      Set<String> authset = getRoleSet();
      return authset.contains(role) || !roles.containsKey(role);
  }

  /**
   * 检查动作权限(与当前请求相关)
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
   * 检查页面权限(与当前请求相关)
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

  /**
   * 获取菜单列表(与当前请求相关)
   * @param level
   * @param depth
   * @return
   */
  public  List<Map> getMenu(int level, int depth) {
      CoreLanguage lang;
      try {
          lang = CoreLanguage.getInstance(this.name).clone();
      } catch (HongsError er) {
          // 语言文件不存在则拿一个空对象代替
          if (er.getCode() == 0x2a) {
              lang = new CoreLanguage(null);
          } else {
              throw er;
          }
      }
      for(String namz : imports) {
          lang.loadIgnrFNF(namz);
      }

      return getMenu(level, depth, 0, pages, lang);
  }

  private List<Map> getMenu(int level, int depth, int i, Map<String, Map> pages, CoreLanguage lang) {
      List<Map> list = new ArrayList();

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
              page.put("menu", a);
              page.put("auth", chkPage(u));
              page.put("disp", lang.translate(n));
              list.add(page);
          } else {
              list.addAll(a);
          }
      }

      return list;
  }

  //** 工厂方法 **/

  public static SiteMap getInstance(String name) throws HongsException {
      String key = SiteMap.class.getName() + ":" + name;
      Core core = Core.getInstance();
      SiteMap inst;
      if (core.containsKey(key)) {
          inst = (SiteMap)core.get(key);
      }
      else {
          inst = new SiteMap(name);
          core.put( key, inst );
      }
      return inst;
  }

  public static SiteMap getInstance() throws HongsException {
      return getInstance("default");
  }
}
