package app.hongs.action;

import app.hongs.Cnst;
import app.hongs.Core;
import app.hongs.CoreLocale;
import app.hongs.CoreSerial;
import app.hongs.HongsException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
 * 导航配置.
 *
 * <p>
 * 该工具会将配置数据自动缓存, 会在构建对象时核对配置的修改时间;
 * 但无法确保其对象在反复使用中会自动重载,
 * 最好在修改配置后删除临时文件并重启应用.
 * </p>
 *
 * <h3>数据结构:</h3>
 * <pre>
    menus = {
      "href" : {
        hrel: 页面,
        icon: 数据,
        disp: 名称,
        menus : {
          子级菜单...
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
      "name" : {
        disp: 名称,
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
 * 区间: 0x10e0~0x10e7
 * 0x10e0 配置文件不存在
 * 0x10e1 解析文件失败
 * 0x10e2 角色无法获取
 * </pre>
 *
 * <h3>特别注意:</h3>
 * <p>
 * 由于 rsname 拥有引用作用,
 * 配置不当会导致 getRoleSet 无限循环,
 * 规定 rsname 只可引用上级.
 * </p>
 * @author Hongs
 */
public class NaviMap
  extends CoreSerial
{

  private final String name;

  /**
   * 页面路径信息
   */
  public Map<String, List> paths;

  /**
   * 页面层级信息
   */
  public Map<String, Map> menus;

  /**
   * 全部分组信息
   */
  public Map<String, Map> roles;

  /**
   * 全部动作
   */
  public Set<String> actions = null;

  /**
   * 全部导入
   */
  public Set<String> imports = null;

  /**
   * 会话名称
   */
  public /**/String  session = null;

  public NaviMap(String name)
    throws HongsException
  {
    this.name = name;
    this.init(name + Cnst.NAVI_EXT);
  }

  protected boolean expired(String name)
  {
    File xmlFile = new File(Core.CONF_PATH
                 + File.separator + name + Cnst.NAVI_EXT + ".xml");
    File serFile = new File(Core.DATA_PATH
                 + File.separator + "serial"
                 + File.separator + name + Cnst.NAVI_EXT + ".ser");
    return xmlFile.exists() && xmlFile.lastModified() > serFile.lastModified();
  }

  @Override
  protected boolean expired(long time)
  throws HongsException
  {
    // 检查当前文件
    if (expired(name))
    {
        return  true ;
    }

    File serFile = new File(Core.DATA_PATH
                 + File.separator + "serial"
                 + File.separator + name + Cnst.NAVI_EXT + ".ser");
    load(serFile);

    // 检查引入文件
    for(String  namz : imports)
    {
    if (expired(namz))
    {
        return  true ;
    }
    }   return  false;
  }

  @Override
  protected void imports()
    throws HongsException
  {
    InputStream is;
    String      fn;

    try
    {
        fn = Core.CONF_PATH + File.separator + name + Cnst.NAVI_EXT + ".xml";
        is = new FileInputStream(fn);
    }
    catch (FileNotFoundException ex)
    {
        fn = name.contains("/")
           ? name + Cnst.NAVI_EXT + ".xml"
           : "app/hongs/config/" + name + Cnst.NAVI_EXT + ".xml";
        is = this.getClass().getClassLoader().getResourceAsStream(fn);
        if (  is  ==  null )
        {
            throw new app.hongs.HongsException(0x10e0,
                "Can not find the config file '" + name + Cnst.NAVI_EXT + ".xml'.");
        }
    }

    Element root;
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbn = dbf.newDocumentBuilder();
      Document  doc = dbn.parse( is );
      root = doc.getDocumentElement();

      // 角色会话名称
      NodeList list = root.getElementsByTagName("rsname");
      if (list.getLength() != 0)
      {
          session = list.item(0).getTextContent();
      }
    }
    catch ( IOException ex)
    {
      throw new HongsException(0x10e1, "Read '" +name+Cnst.NAVI_EXT+".xml error'", ex);
    }
    catch (SAXException ex)
    {
      throw new HongsException(0x10e1, "Parse '"+name+Cnst.NAVI_EXT+".xml error'", ex);
    }
    catch (ParserConfigurationException ex)
    {
      throw new HongsException(0x10e1, "Parse '"+name+Cnst.NAVI_EXT+".xml error'", ex);
    }

    this.paths = new HashMap();
    this.menus = new LinkedHashMap();
    this.roles = new LinkedHashMap();
    this.actions = new HashSet();
    this.imports = new HashSet();

    this.parse(root, this.paths, this.menus, this.roles, this.imports, this.actions, new HashSet(), new ArrayList());
  }

  private void parse(Element element, Map paths, Map menus, Map roles, Set imports, Set actions, Set depends, List path)
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

      if ("menu".equals(tagName2))
      {
        Map menu2 = new HashMap();

        String href = element2.getAttribute("href");
        if (href == null) href = "";
        menus.put(href , menu2);

        String hrel = element2.getAttribute("hrel");
        if (hrel == null) hrel = "";
        menu2.put("hrel", hrel );

        String icon = element2.getAttribute("icon");
        if (icon == null) icon = "";
        menu2.put("icon", icon );

        String disp = element2.getAttribute("disp");
        if (disp == null) disp = "";
        menu2.put("disp", disp );

        List path2 = new ArrayList(path);
        path2.add(menu2);
        paths.put(href, path2);

        Map menus2 = new LinkedHashMap();
        Set roles2 = new LinkedHashSet();

        // 获取下级页面和权限
        this.parse(element2, paths, menus2, roles, imports, actions, roles2, path2);

        if (!menus2.isEmpty())
        {
          menu2.put("menus", menus2);
        }
        if (!roles2.isEmpty())
        {
          menu2.put("roles", roles2);
        }
      }
      else
      if ("role".equals(tagName2))
      {
        Map role2 = new HashMap();

        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        roles.put( namz , role2);

        String disp = element2.getAttribute("disp");
        if (disp == null) disp = "";
        role2.put("disp", disp );

        Set actions2 = new HashSet();
        Set depends2 = new HashSet();

        // 获取下级动作和依赖
        this.parse(element2, null, null, null, null, actions2, depends2, null);

        if (!actions2.isEmpty())
        {
          role2.put("actions", actions2);
          actions.addAll(actions2);
        }
        if (!depends2.isEmpty())
        {
          role2.put("depends", depends2);
//        depends.addAll(depends2);
        }

        // 上级页面依赖的权限
        depends.add( namz );
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
        NaviMap  conf = new NaviMap(impart);
        paths.putAll(conf.paths);
        menus.putAll(conf.menus);
        roles.putAll(conf.roles);
        actions.addAll(conf.actions);
        imports.add   (/**/ impart );
        imports.addAll(conf.imports);
      }
    }
  }

  public String getName() {
      return  this.name;
  }

  /**
   * 获取页面信息
   * @param name
   * @return 找不到则返回null
   */
  public Map getMenu(String name)
  {
    List path  = this.paths.get(name);
    if ( path == null) return null;
    int  last  = path.size() - 1;
    return (Map) path.get (last);
  }

  /**
   * 获取页面单元
   * @param names
   * @return 单元字典
   */
  public Map<String, Map> getMenuRoles(String... names)
  {
    Map<String, Map> rolez = new HashMap();

    for (String namz : names) {
        Map menu = getMenu(namz);

        Set set = (Set) menu.get("roles");
        if (set != null && !set.isEmpty()) {
            rolez.putAll(getMoreRoles((String[]) set.toArray(new String[0])));
        }

        Map map = (Map) menu.get("menus");
        if (map != null && !map.isEmpty()) {
            rolez.putAll(getMoreRoles((String[]) map.keySet().toArray(new String[0])));
        }
    }

    return rolez;
  }

  /**
   * 获取页面权限
   * @param names
   * @return 单元字典
   */
  public Set<String> getMenuAuths(String... names)
  {
    Set<String> authz = new HashSet();

    for (String namz : names) {
        Map menu = getMenu(namz);

        Set set = (Set) menu.get("roles");
        if (set != null && !set.isEmpty()) {
            authz.addAll(NaviMap.this.getRoleAuths((String[]) set.toArray(new String[0])));
        }

        Map map = (Map) menu.get("menus");
        if (map != null && !map.isEmpty()) {
            authz.addAll(NaviMap.this.getRoleAuths((String[]) map.keySet().toArray(new String[0])));
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
    Set<String>  as = new HashSet();
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
    for (String n : names)
    {
      Map role = this.roles.get(n);
      if (role == null || roles.containsKey(n))
      {
        continue;
      }

      roles.put(n, role);

      if (role.containsKey("actions"))
      {
        Set<String> actionsSet = (Set<String>) role.get("actions");
        auths.addAll(actionsSet);
      }
      if (role.containsKey("depends"))
      {
        Set<String> dependsSet = (Set<String>) role.get("depends");
        String[ ]   dependsArr = dependsSet.toArray(new String[0]);
        this.getRoleAuths(roles, auths, dependsArr);
      }
    }
  }

  /**
   * 获取角色集合(与当前请求相关)
   * @return session 为空则返回 null
   * @throws app.hongs.HongsException
   */
  public Set<String> getRoleSet() throws HongsException {
      if (session == null || session.length() == 0) {
//        throw new HongsException(0x10e2, "Can not get roles for menu: "+name);
          return null;
      }
      if (session.startsWith("@")) {
          return getInstance(session.substring( 1 )).getRoleSet( );
      }
      if (! session.contains(".")) {
          ActionHelper hlpr = Core.getInstance(ActionHelper.class);
          return (Set) hlpr.getSessibute(session);
      } else
      {
          return (Set) Core.getInstance (session);
      }
  }

  /**
   * 获取权限集合(与当前请求相关)
   * @return session 为空则返回 null
   * @throws app.hongs.HongsException
   */
  public Set<String> getAuthSet() throws HongsException {
      Set<String> roleset = getRoleSet();
      if (null == roleset)  return null ;
      return getRoleAuths(roleset.toArray(new String[0]));
  }

  /**
   * 检查角色权限(与当前请求相关)
   * @param role
   * @return 可访问则为true
   * @throws app.hongs.HongsException
   */
  public boolean chkRole(String role) throws HongsException {
      Set<String> roleset = getRoleSet( );
      if (null == roleset) {
          return !roles.containsKey(role);
      }
      return roleset.contains(role) || !roles.containsKey(role);
  }

  /**
   * 检查动作权限(与当前请求相关)
   * @param auth
   * @return 可访问则为true
   * @throws app.hongs.HongsException
   */
  public boolean chkAuth(String auth) throws HongsException {
      Set<String> authset = getAuthSet( );
      if (null == authset) {
          return !actions.contains (auth);
      }
      return authset.contains(auth) || !actions.contains (auth);
  }

  /**
   * 检查页面权限(与当前请求相关)
   * @param name
   * @return 有一个动作可访问即返回true
   * @throws app.hongs.HongsException
   */
  public Boolean chkMenu(String name) throws HongsException {
      Set<String> rolez = getMenuRoles(name).keySet();
      if (null == rolez || rolez.isEmpty( )) {
          return  true ;
      }

      Set<String> rolex = getRoleSet( /**/ );
      if (null == rolex || rolex.isEmpty( )) {
          return  false;
      }

      for(String  role : rolez) {
      if (rolex.contains(role)) {
          return  true ;
      }
      }

      /**/return  false;
  }

  /**
   * 获取翻译对象(与当前请求相关)
   * @return
   */
  public CoreLocale getCurrTranslator() {
    CoreLocale lang;
    try {
      lang = CoreLocale.getInstance(name).clone();
    }
    catch (app.hongs.HongsError e ) {
      if  (   e.getCode() != 0x2a ) {
        throw e;
      }
      // 语言文件不存在则拿一个空代替
      lang = new CoreLocale(null);
    }
    for(String namz : imports) {
        lang.loadIgnrFNF(namz);
    }
    return lang;
  }

  /**
   * 获取权限列表(与当前权限无关)
   * @return
   * @throws app.hongs.HongsException
   */
  public  List<Map> getRoleTranslates()
  throws HongsException {
      return getRoleTranslated( null  );
  }

  /**
   * 获取权限列表(与当前权限有关)
   * @return
   * @throws app.hongs.HongsException
   */
  public  List<Map> getRoleTranslated()
  throws HongsException {
      Set<String> rolez = getRoleSet( );
      if (null == rolez)  rolez = new HashSet();
      return getRoleTranslated( rolez );
  }

  /**
   * 获取权限列表(与指定权限有关)
   * @param rolez 指定权限
   * @return
   */
  public  List<Map> getRoleTranslated(Set<String> rolez) {
      CoreLocale lang = getCurrTranslator();
      return getRoleTranslated(rolez, menus, lang);
  }

  private List<Map> getRoleTranslated(Set<String> rolez, Map<String, Map> menus, CoreLocale lang) {
      List<Map> list = new ArrayList( );

      for(Map.Entry item : menus.entrySet() ) {
          Map v = (Map) item.getValue();
          Map m = (Map) v.get ("menus");
          Set r = (Set) v.get ("roles");

          if (r != null && !r.isEmpty()) {
            List<Map> lizt = new ArrayList( );

            String h = (String) item.getKey();
            String s = (String) v.get("disp");
            String z; Set<String> x;

            for( String n : (Set<String>) r ) {
                if (rolez != null && !rolez.contains(n)) {
                    continue;
                }

                v = getRole(n);
                z = (String) v.get ("disp");
                x = (Set<String>) v.get("depends");

                // 没有指定 disp 则用 name 获取
                if ("".equals(z)) {
                    z = "core.role." + n ;
                }   z = lang.translate(z);

                Map role = new HashMap( );
                role.put("name", n);
                role.put("disp", z);
                role.put("rels", x);
                lizt.add( role );
            }

            if (! lizt.isEmpty()) {
                // 没有指定 disp 则用 href 获取
                if ("".equals(s)) {
                    s = "core.role." + h ;
                }   s = lang.translate(s);

                Map menu = new HashMap( );
                menu.put("list", lizt);
                menu.put("href", h);
                menu.put("disp", s);
                list.add( menu );
            }
          }

          // 拉平菜单
          if (m != null && !m.isEmpty()) {
            list.addAll(getRoleTranslated(rolez, m, lang));
          }
      }

      return list;
  }

  /**
   * 获取菜单列表(与当前请求无关)
   * @param level
   * @param depth
   * @return
   * @throws app.hongs.HongsException
   */
  public  List<Map> getMenuTranslates(int level, int depth)
  throws HongsException {
      return getMenuTranslated(level, depth, null );
  }

  /**
   * 获取菜单列表(与当前请求相关)
   * @param level
   * @param depth
   * @return
   * @throws app.hongs.HongsException
   */
  public  List<Map> getMenuTranslated(int level, int depth)
  throws HongsException {
      Set<String> rolez = getRoleSet();
      if (null == rolez)  rolez = new HashSet(/**/);
      return getMenuTranslated(level, depth, rolez);
  }

  /**
   * 获取菜单列表(与当前请求相关)
   * @param level
   * @param depth
   * @param rolez
   * @return
   */
  public  List<Map> getMenuTranslated(int level, int depth, Set<String> rolez) {
      CoreLocale lang = getCurrTranslator();
      return getMenuTranslated(level, depth, rolez, menus, lang, 0);
  }

  private List<Map> getMenuTranslated(int level, int depth, Set<String> rolez, Map<String, Map> menus, CoreLocale lang, int i) {
      List <Map> list = new ArrayList( );

      if (  null == menus || level + depth <= i ) {
          return list;
      }

      FOR:for (Map.Entry item : menus.entrySet()) {
          Map  v = (Map) item.getValue();
          Map  m = (Map) v.get ("menus");
          List l = getMenuTranslated(level, depth, rolez, m, lang, i + 1);

          if ( level  <=  i ) {
              String h = (String) item.getKey ( );

              // 页面下的任意一个动作有权限即认为是可访问的
              if (null != rolez) {
                  Set<String> z = getMenuRoles(h).keySet();
                  if ( ! z.isEmpty(  ) ) {
                      boolean e = true ;
                      for (String x : z) {
                          if (rolez.contains(x) ) {
                              e = false;
                                  break;
                          }
                      }
                      if ( e ) continue;
                  }
              }

              String p = (String) v.get("hrel");
              String d = (String) v.get("icon");
              String s = (String) v.get("disp");

              // 没有指定 disp 则用 href 获取
              if ("".equals(s)) {
                  s = "core.menu." + h ;
              }   s = lang.translate(s);

              Map menu = new HashMap();
              menu.put("href", h);
              menu.put("hrel", p);
              menu.put("icon", d);
              menu.put("disp", s);
              menu.put("list", l);

              list.add(menu);
          } else {
              list.addAll(l);
          }
      }

      return list;
  }

  //** 工厂方法 **/

  public static NaviMap getInstance(String name) throws HongsException {
      String cn = NaviMap.class.getName() + ":" + name;
      Core core = Core.getInstance();
      NaviMap inst;
      if (core.containsKey(cn)) {
          inst = (NaviMap) core.get( cn );
      } else {
          inst = new NaviMap( name );
          core.put( cn , inst );
      }
      return inst;
  }

  public static NaviMap getInstance() throws HongsException {
      return getInstance("default");
  }

}
