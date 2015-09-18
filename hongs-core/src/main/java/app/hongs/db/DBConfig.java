package app.hongs.db;

import app.hongs.Core;
import app.hongs.CoreSerial;
import app.hongs.HongsException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 数据库配置信息解析类
 *
 * <h3>异常代码:</h3>
 * <pre>
 * 区间: 0x1060~0x106f
 * 0x1061  找不到数据库配置文件
 * 0x1063  无法解析数据库配置文档
 * 0x1065  在配置文档中找不到根节点
 * 0x1067  无法读取XML文件
 * 0x1069  无法读取XML流
 * </pre>
 *
 * @author Hongs
 */
public class DBConfig
     extends CoreSerial
  implements Serializable
{

  //** 缓存 **/

  protected String name;

  public DBConfig(String name)
    throws HongsException
  {
    this.name = name;
    this.init(name+".db");
  }

  @Override
  protected boolean expired(long time)
  {
    File xmlFile = new File(Core.CONF_PATH
                 + File.separator + name + ".db.xml");
    File serFile = new File(Core.DATA_PATH
                 + File.separator + "serial"
                 + File.separator + name + ".db.ser");
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
        fn = Core.CONF_PATH + File.separator + name + ".db.xml";
        is = new FileInputStream(fn);
    }
    catch (FileNotFoundException ex)
    {
        fn = name.contains("/") ? name + ".db.xml" : "app/hongs/config/" + name + ".db.xml";
        is = this.getClass().getClassLoader().getResourceAsStream(fn);
        if (  is  ==  null )
        {
            throw new app.hongs.HongsError(0x2a, "Can not find the source config file '" + name + ".db.xml'.");
        }
    }

    DBConfig cp = parseByStream(is);

    this.source       = cp.source;
    this.origin       = cp.origin;
    this.dbClass      = cp.dbClass;
    this.tableClass   = cp.tableClass;
    this.modelClass   = cp.modelClass;
    this.tablePrefix  = cp.tablePrefix;
    this.tableSuffix  = cp.tableSuffix;
    this.tableConfigs = cp.tableConfigs;
  }

  //** 数据 **/

  public String dbClass;

  public String tableClass;

  public String modelClass;

  public String tablePrefix;

  public String tableSuffix;

  public Map<String, Map> tableConfigs;

  public Map<String, String> source;

  public Map<String, String> origin;

  private static Set<String> tableAttrs = new HashSet(
  Arrays.asList( new String[] {
    "name","tableName","primaryKey","class","model"
  }));
  private static Set<String> assocAttrs = new HashSet(
  Arrays.asList( new String[] {
    "type","join","name","tableName","primaryKey","foreignKey",
    "select","where","groupBy","havin","orderBy"
  }));

  public DBConfig(Document doc)
    throws HongsException
  {
    /**
     * 仅当type为BLS_TO或HAS_ONE时join可用;
     * 当type为BLS_TO时, foreignKey为基本表的外键,
     * 当type为HAS_ONE或HAS_MANY时, foreignKey为关联表的外键;
     * 关联表其他可选配置fields|where|groupBy|orderBy|limit见FetchCase说明.
     */

    Element root = doc.getDocumentElement();
    if (!root.hasChildNodes())
    {
      throw new app.hongs.HongsException(0x1065, "Can not find root element in config document.");
    }

    String link;
    this.dbClass = "";
    this.tableClass = "";
    this.modelClass = "";
    this.tablePrefix = "";
    this.tableSuffix = "";
    this.source = new HashMap<String, String>();
    this.origin = new HashMap<String, String>();
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
          modelClass = conf.modelClass;
          tablePrefix = conf.tablePrefix;
          tableSuffix = conf.tableSuffix;
        }
        else
        {
          dbClass = getAttribute(element, "dbClass", "");
          tableClass = getAttribute(element, "tableClass", "");
          modelClass = getAttribute(element, "modelClass", "");
          tablePrefix = getAttribute(element, "tablePrefix", "");
          tableSuffix = getAttribute(element, "tableSuffix", "");
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
      if (tagName.equals("origin"))
      {
        link = getAttribute(element, "link", null);
        if (link != null)
        {
          DBConfig conf = new DBConfig(link);
          this.origin = conf.origin;
        }
        else
        {
          this.origin = DBConfig.getOrigin(element);
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

    return new DBConfig(doc);
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

    return new DBConfig(doc);
  }

  /**
   * 根据输入流解析配置
   *
   * @param ds
   * @return 配置对象
   * @throws app.hongs.HongsException
   */
  public static DBConfig parseBySource(InputSource ds)
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

    return new DBConfig(doc);
  }

  private static Map getSource(Element element)
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

    Map source = new HashMap();
    source.put("drv", drv);
    source.put("url", url);
    source.put("info", info);

    return source;
  }

  private static Map getOrigin(Element element)
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

    Map origin = new HashMap();
    origin.put("name", name);
    origin.put("info", info);

    return origin;
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
        Map relats2 = new LinkedHashMap();
        Map assocs2 = getAssocs(element2, relats2);
        if (assocs2.isEmpty( ) == false )
        {
            table.put("assocs" , assocs2);
            table.put("relats" , relats2);
        }
      }
    }

    return tables;
  }

  private static Map getAssocs(Element element, Map relats)
  {
    return getAssocs(element, relats, new ArrayList());
  }

  private static Map getAssocs(Element element, Map relats, List tns)
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
        relats.put(tn2, assoc);
        if (! tns.isEmpty( ) )
        {
            assoc.put("path" , tns);
        }

        // 递归关联配置
        Map assocs2 = getAssocs(element2, relats, tns2);
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

  /** 源 **/

  public static class DBSource {

  }

  public static class DBOrigin {

  }

  /** 表 **/

  public static class TableConfig {

  }

  public static class AssocConfig {

  }

}
