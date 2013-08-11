package app.hongs.db;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import app.hongs.Core;
import app.hongs.CoreSerially;
import app.hongs.HongsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.NamedNodeMap;

/**
 * <h1>数据库配置信息解析类</h1>
 *
 * <h2>异常代码区间:</h2>
 * <b>0x1060~0x106f</b>
 *
 * <h2>异常代码说明:</h2>
 * <ul>
 * <li>0x1061  找不到数据库配置文件</li>
 * <li>0x1063  无法解析数据库配置文档</li>
 * <li>0x1065  在配置文档中找不到根节点</li>
 * <li>0x1067  无法读取XML文件</li>
 * <li>0x1069  无法读取XML流</li>
 * </ul>
 *
 * @author Hongs
 */
public class DBConfig
     extends CoreSerially
  implements Serializable
{

  /** 缓存 **/

  protected String name;

  public DBConfig(String name)
    throws HongsException
  {
    this.name = name;
    this.init("db-" + name);
  }

  @Override
  protected boolean isExpired(long time)
  {
    File xmlFile = new File(Core.CONF_PATH
                 + File.separator + "db-" + name + ".xml");
    File serFile = new File(Core.TMPS_PATH
                 + File.separator + "db-" + name + ".ser");
    return xmlFile.lastModified() > serFile.lastModified();
  }

  @Override
  protected void loadData()
    throws HongsException
  {
    DBConfig cp = DBConfig.parseByName(name);

    this.driver       = cp.driver;
    this.source       = cp.source;
    this.dbClass      = cp.dbClass;
    this.tableClass   = cp.tableClass;
    this.tablePrefix  = cp.tablePrefix;
    this.tableSuffix  = cp.tableSuffix;
    this.tableConfigs = cp.tableConfigs;
  }

  /** 数据 **/

  public String dbClass;

  public String tableClass;

  public String tablePrefix;

  public String tableSuffix;

  public Map<String, Map> tableConfigs;

  public Map<String, String> driver;

  public Map<String, String> source;

  private static Set<String> tableAttrs = new HashSet(
  Arrays.asList( new String[] {
    "name","primaryKey","class","prefix","suffix"
  }));
  private static Set<String> assocAttrs = new HashSet(
  Arrays.asList( new String[] {
    "type","join","name","realName","primaryKey","foreignKey",
    "select","where","groupBy","having","orderBy"
  }));

  /**
   * 根据名称解析配置
   *
   * @param dn
   * @return 配置对象
   * @throws app.hongs.HongsException
   */
  public static DBConfig parseByName(String dn)
    throws HongsException
  {
    File dbConfFile = new File(Core.CONF_PATH + File.separator + "db-" + dn + ".xml");
    if (!dbConfFile.exists())
    {
      throw new app.hongs.HongsException(0x1061, "Can not find DB config file 'db-"+dn+".xml'");
    }

    return DBConfig.parseByFile(dbConfFile);
  }

  /**
   * 根据文件解析配置
   *
   * @param df
   * @return 配置对象
   * @throws app.hongs.HongsException
   */
  public static DBConfig parseByFile(File df)
    throws HongsException
  {
    Document doc;
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbn = dbf.newDocumentBuilder();
      doc = dbn.parse(df);
    }
    catch (ParserConfigurationException ex)
    {
      throw new app.hongs.HongsException(0x1063, ex);
    }
    catch (SAXException ex)
    {
      throw new app.hongs.HongsException(0x1063, ex);
    }
    catch (IOException ex)
    {
      throw new app.hongs.HongsException(0x1067, ex);
    }

    return DBConfig.parseByDocument(doc);
  }

  /**
   * 根据输入流解析配置
   *
   * @param ds
   * @return 配置对象
   * @throws app.hongs.HongsException
   */
  public static DBConfig parseByStream(InputStream ds)
    throws HongsException
  {
    Document doc;
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbn = dbf.newDocumentBuilder();
      doc = dbn.parse(ds);
    }
    catch (ParserConfigurationException ex)
    {
      throw new app.hongs.HongsException(0x1063, ex);
    }
    catch (SAXException ex)
    {
      throw new app.hongs.HongsException(0x1063, ex);
    }
    catch (IOException ex)
    {
      throw new app.hongs.HongsException(0x1069, ex);
    }

    return DBConfig.parseByDocument(doc);
  }

  /**
   * 根据文档对象解析配置
   *
   * @param doc
   * @return 配置对象
   * @throws app.hongs.HongsException
   */
  public static DBConfig parseByDocument(Document doc)
    throws HongsException
  {
    return new DBConfig(doc);
  }

  public DBConfig(Document doc)
    throws HongsException
  {
    /**
     * 仅当type为BLS_TO或HAS_ONE时join可用;
     * 当type为BLS_TO时, foreignKey为基本表的外键,
     * 当type为HAS_ONE或HAS_MANY时, foreignKey为关联表的外键;
     * 关联表其他可选配置fields|where|groupBy|orderBy|limit见FetchBean说明.
     */

    Element root = doc.getDocumentElement();
    if (!root.hasChildNodes())
    {
      throw new app.hongs.HongsException(0x1065, "Can not find root element in config document.");
    }

    String link;
    this.dbClass = "";
    this.tableClass = "";
    this.tablePrefix = "";
    this.tableSuffix = "";
    this.driver = new HashMap<String, String>();
    this.source = new HashMap<String, String>();
    this.tableConfigs = new HashMap<String, Map>();

    NodeList childs = root.getChildNodes();
    for (int i = 0; i < childs.getLength(); i ++)
    {
      Node node = childs.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE)
      {
        continue;
      }

      Element element = (Element)node;
      String  tagName = element.getTagName();

      if (tagName.equals("config"))
      {
        link = getAttribute(element, "link", null);
        if (link != null)
        {
          DBConfig conf = new DBConfig(link);
          dbClass = conf.dbClass;
          tableClass = conf.tableClass;
          tablePrefix = conf.tablePrefix;
          tableSuffix = conf.tableSuffix;
        }
        else
        {
          dbClass = getAttribute(element, "dbClass", "");
          tableClass = getAttribute(element, "tableClass", "");
          tablePrefix = getAttribute(element, "tablePrefix", "");
          tableSuffix = getAttribute(element, "tableSuffix", "");
        }
      }
      else
      if (tagName.equals("driver"))
      {
        link = getAttribute(element, "link", null);
        if (link != null)
        {
          DBConfig conf = new DBConfig(link);
          this.driver = conf.driver;
        }
        else
        {
          this.driver = DBConfig.getDriver(element);
        }
      }
      else
      if (tagName.equals("source"))
      {
        link = getAttribute(element, "link", null);
        if (link != null)
        {
          DBConfig conf = new DBConfig(link);
          this.source = conf.source;
        }
        else
        {
          this.source = DBConfig.getSource(element);
        }
      }
      else
      if (tagName.equals("tables"))
      {
        this.tableConfigs = DBConfig.getTables(element);
      }
    }

    //app.hongs.util.JSON.dump(this.tableConfigs);
  }

  private static Map getDriver(Element element)
  {
    String drv = "";
    String url = "";
    Properties info = new Properties();

    NamedNodeMap atts = element.getAttributes();
    for (int i = 0; i < atts.getLength(); i ++)
    {
      Node attr = atts.item(i);
      String name = attr.getNodeName();
      String value = attr.getNodeValue();
      if ("drv".equals(name))
      {
        drv = value;
      }
      else
      if ("url".equals(name))
      {
        url = value;
      }
      else
      {
        info.setProperty(name, value);
      }
    }

    Map driver = new HashMap();
    driver.put("drv", drv);
    driver.put("url", url);
    driver.put("info", info);

    return driver;
  }

  private static Map getSource(Element element)
  {
    String name = "";
    Properties info = new Properties();

    NamedNodeMap atts = element.getAttributes();
    for (int i = 0; i < atts.getLength(); i ++)
    {
      Node attr = atts.item(i);
      String namz = attr.getNodeName();
      String value = attr.getNodeValue();

      if ("name".equals(namz))
      {
        name = value;
      }
      else
      {
        info.setProperty(namz, value);
      }
    }

    Map driver = new HashMap();
    driver.put("name", name);
    driver.put("info", info);

    return driver;
  }

  private static Map getTables(Element element)
  {
    Map tables = new LinkedHashMap();

    NodeList childs2 = element.getChildNodes();
    for (int j = 0; j < childs2.getLength(); j ++ )
    {
      Node node2 = childs2.item(j);
      if (node2.getNodeType() != Node.ELEMENT_NODE)
      {
        continue;
      }

      Element element2 = (Element)node2;
      String  tagName2 = element2.getTagName();
      if (tagName2.equals("table"))
      {
        Map table = new HashMap();

        NamedNodeMap atts = element2.getAttributes();
        for (int i = 0; i < atts.getLength(); i ++)
        {
          Node attr = atts.item(i);
          String name = attr.getNodeName();
          String value = attr.getNodeValue();

          if (tableAttrs.contains(name))
          {
            table.put(name, value);
          }
        }

        // 放入基础表中
        tables.put(table.get("name"), table);

        // 放入关联配置
        Map scossa2 = new LinkedHashMap();
        Map assocs2 = getAssocs(element2, scossa2);
        if (assocs2.isEmpty( ) == false )
        {
            table.put("assocs" , assocs2);
            table.put("scossa" , scossa2);
        }
      }
    }

    return tables;
  }

  private static Map getAssocs(Element element, Map scossa)
  {
    return getAssocs(element, scossa, new ArrayList());
  }

  private static Map getAssocs(Element element, Map scossa, List tns)
  {
    Map assocs = new LinkedHashMap();
    
    NodeList childs2 = element.getChildNodes();
    for (int j = 0; j < childs2.getLength(); j ++ )
    {
      Node node2 = childs2.item(j);
      if (node2.getNodeType() != Node.ELEMENT_NODE)
      {
        continue;
      }

      Element element2 = (Element)node2;
      String  tagName2 = element2.getTagName();
      if (tagName2.equals("assoc"))
      {
        Map assoc = new HashMap();

        NamedNodeMap atts = element2.getAttributes();
        for (int i = 0; i < atts.getLength(); i ++)
        {
          Node attr = atts.item(i);
          String name = attr.getNodeName();
          String value = attr.getNodeValue();

          if (assocAttrs.contains(name))
          {
            assoc.put(name, value);
          }
          // 关联更新键, Add by Hong on 2013/6/6
          else if ("updateKeys".contains(name))
          {
            assoc.put(name, Arrays.asList(value.split(",")));
          }
        }

        // 放入关联表中
        String tn2 = (String)assoc.get("name");
        List tns2 = new ArrayList(tns);
             tns2.add(tn2);
        assocs.put(tn2, assoc);
        scossa.put(tn2, assoc);
        if (! tns.isEmpty( ) )
        {
            assoc.put("path" , tns);
        }

        // 递归关联配置
        Map assocs2 = getAssocs(element2, scossa, tns2);
        if (assocs2.isEmpty( ) == false )
        {
            assoc.put("assocs" , assocs2);
        }
      }
    }
    
    return assocs;
  }

  private static String getAttribute(Element element, String name, String def)
  {
    String text = element.getAttribute(name);
    return text != null && text.length() != 0 ? text : def;
  }

}
