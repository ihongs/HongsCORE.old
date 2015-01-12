package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.CoreSerially;
import app.hongs.HongsException;
import app.hongs.util.Data;
import app.hongs.util.Dict;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 表单配置.
 *
 * <p>
 * 该工具会将配置数据自动缓存, 会在构建对象时核对配置的修改时间;
 * 但无法确保其对象在反复使用中会自动重载,
 * 最好在修改配置后删除临时文件并重启应用.
 * </p>
 *
 * <h3>数据结构:</h3>
 * <pre>
    datas = {
        "data_name" : Object
    }
    enums = {
        "enum_name" : {
            "code" : "Label"
            ...
        }
        ...
    }
    roles = {
        "role_name" : {
            _disp: "Label",
            _href: "URI",
            roles: {
                "role_name" : {
                    _disp: "Label",
                    _href: "URI",
                    actions: set[
                        "action"
                        ...
                    ],
                    depends: set[
                        ["conf_name", "auth_name", "role_name"]
                        ...
                    ]
                }
                ...
            }
            items: {
                "item_name" : {
                    _disp : "Label",
                    _type : "role|enum|file|string(text|hidden|textarea)|number(slider|switch)|date|time|datetime|tel|url|email",
                    _rule : "rule_func",
                    _required : 0|1,
                    _repeated : 0|1,
                    "name" : "Value"
                    ...
                }
                ...
            }
            ...
        }
        ...
    }
 * </pre>
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
public class SourceConfig
  extends CoreSerially
{

  private String name;

  /**
   * 数据集合
   */
  public Map<String, Map> datas;

  /**
   * 枚举集合
   */
  public Map<String, Map> enums;

  /**
   * 表单集合
   */
  public Map<String, Map> units;

  public SourceConfig(String name)
    throws HongsException
  {
    this.name = name;
    this.init(name+".as");
  }

  @Override
  protected boolean expired(long time)
  {
    File xmlFile = new File(Core.CONF_PATH
                + File.separator + name + ".as.xml");
    File serFile = new File(Core.SERS_PATH
                + File.separator + name + ".as.ser");
    return xmlFile.lastModified() > serFile.lastModified();
  }

  @Override
  protected void imports()
    throws HongsException
  {
    File df = new File(Core.CONF_PATH
                + File.separator + name + ".as.xml");
    if (!df.exists())
    {
      throw new HongsException(0x10e8, "Source config file '"
                + Core.CONF_PATH
                + File.separator + name + ".as.xml"
                + "' is not exists");
    }

    this.datas = new HashMap();
    this.enums = new HashMap();
    this.units = new HashMap();

    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbn = dbf.newDocumentBuilder();
      Document doc = dbn.parse(df);
      Element root = doc.getDocumentElement();

      this.parse(root, this.datas, this.enums, this.units);
    }
    catch ( IOException ex)
    {
      throw new HongsException(0x10e9 , ex);
    }
    catch (SAXException ex)
    {
      throw new HongsException(0x10e9 , ex);
    }
    catch (ParserConfigurationException ex)
    {
      throw new HongsException(0x10e9 , ex);
    }
  }

  private void parse(Element element, Map datas, Map enums, Map forms)
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

      if ("data".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Object data2 = Data.toObject(element2.getTextContent());
        datas.put(namz, data2);
      }
      else
      if ("enum".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map optns2 = new LinkedHashMap();
        this.parse(element2, optns2 , null , null );
        enums.put(namz, optns2);
      }
      else
      if ("unit".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map forms2 = new LinkedHashMap();
        Map roles2 = new LinkedHashMap();
        Map items2 = new LinkedHashMap();
        this.parse(element2, roles2, items2, null );
        forms.put(namz, forms2);

        namz = element2.getAttribute("disp");
        if (namz != null) {
            forms2.put("_disp", namz);
        } else {
            forms2.put("_disp",  "" );
        }

        namz = element2.getAttribute("href");
        if (namz != null) {
            forms2.put("_href", namz);
        } else {
            forms2.put("_href",  "" );
        }

        forms2.put("roles", roles2);
        forms2.put("items", items2);
      }
      else
      if ("role".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map roles2 = new LinkedHashMap();
        Map actns2 = new LinkedHashMap();
        Map depns2 = new LinkedHashMap();
        this.parse(element2, actns2, depns2, null );
        datas.put(namz, roles2);

        namz = element2.getAttribute("disp");
        if (namz != null) {
            roles2.put("_disp", namz);
        } else {
            roles2.put("_disp",  "" );
        }

        namz = element2.getAttribute("href");
        if (namz != null) {
            roles2.put("_href", namz);
        } else {
            roles2.put("_href",  "" );
        }

        roles2.put("actions", new LinkedHashSet(actns2.keySet()));
        roles2.put("depends", new LinkedHashSet(depns2.keySet()));
      }
      else
      if ("item".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map optns2 = new LinkedHashMap();
        this.parse(element2, optns2 , null , null );
        enums.put(namz, optns2);

        namz = element2.getAttribute("disp");
        if (namz != null) {
            optns2.put("_disp", namz);
        } else {
            optns2.put("_disp",  "" );
        }

        namz = element2.getAttribute("type");
        if (namz != null && !"".equals(namz)) {
            optns2.put("_type", namz);
        } else {
            optns2.put("_type", "");
        }

        namz = element2.getAttribute("rule");
        if (namz != null && !"".equals(namz)) {
            optns2.put("_rule", namz);
        } else {
            optns2.put("_rule", "");
        }

        namz = element2.getAttribute("required");
        if (namz != null && !"".equals(namz) && !"0".equals(namz)) {
            optns2.put("_required", Byte.parseByte(namz));
        } else {
            optns2.put("_required", (byte) 0);
        }

        namz = element2.getAttribute("repeated");
        if (namz != null && !"".equals(namz) && !"0".equals(namz)) {
            optns2.put("_repeated", Byte.parseByte(namz));
        } else {
            optns2.put("_repeated", (byte) 0);
        }
      }
      else
      if ("option".equals(tagName2))
      {
        String namz = element2.getAttribute("code");
        String data = element2.getTextContent();
        datas.put(namz, data);
      }
      else
      if ("attrib".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        String data = element2.getTextContent();
        datas.put(namz, data);
      }
      else
      if ("action".equals(tagName2))
      {
        String data = element2.getTextContent();
        datas.put(data, null);
      }
      else
      if ("depend".equals(tagName2))
      {
        String[] data = new String[3];
        data[1] = element2.getAttribute("unit");
        data[0] = element2.getAttribute("conf");
        data[2] = element2.getTextContent();
        if (data[0] == null) {
            data[0] = name;
        }
        if (data[1] == null) {
            data[1] = ((Element)element2.getParentNode()).getAttribute("name");
        }
        datas.put(data, null);
      }
    }
  }

  public Object getData(String name) {
    return datas.get(name);
  }

  public < T >T getData(String name, T def) {
    return Dict.deem4Def(getData(name), def);
  }

  public < T >T getData(String name, Class<T> cls) {
    return Dict.deem4Cls(getData(name), cls);
  }

  public Map getEnum(String name) {
    return enums.get(name);
  }

  public Map getUnit(String name) {
    return units.get(name);
  }

  public Map getItems(String name) {
    return Dict.getP4Cls(getUnit(name), Map.class, "items");
  }

  public Map getRoles(String name) {
    return Dict.getP4Cls(getUnit(name), Map.class, "roles");
  }

  public CoreLanguage getCurrTranslator() {
    try {
      return CoreLanguage.getInstance(name);
    }
    catch (app.hongs.HongsError e) {
      if  (  e.getCode( ) != 0x2a) {
        throw e;
      }
      return CoreLanguage.getInstance("default");
    }
  }

  public Map getEnumTranslated(String namc) {
    CoreLanguage lang = getCurrTranslator();
    Map anum = enums.get(  namc  );
    Map data = new LinkedHashMap();
    if (anum == null) return data ;
    data.putAll(anum);
    for (Object o : data.entrySet()) {
      Map.Entry e = (Map.Entry) o ;
      String    n = (String) e.getValue();
      if (n == null || "".equals(n)) {
          n = "core.enum."+name+"."+namc+"."+(String) e.getKey();
      }
      e.setValue( lang.translate(n));
    }
    return data;
  }

  public Map getUnitsTranslated()
    throws HongsException
  {
    CoreLanguage lang = getCurrTranslator();
    Map formz =  new LinkedHashMap();
    for (Object o : units.entrySet()) {
      Map.Entry e = (Map.Entry) o ;
      Map       m = (Map ) e.getValue();
      String    k = (String) e.getKey();
      String    n = (String) m.get("_disp");
      if (n == null || "".equals(n)) {
          n = "core.form."+name+"."+k;
      }
      Map       u = new LinkedHashMap();
      u.putAll( m );
      u.put("_disp", lang.translate(n));
      formz.put(k, u);
    }
    return formz;
  }

  public Map getItemsTranslated(String namc)
    throws HongsException
  {
    Map itemz = new LinkedHashMap();
    Map items = getItems(namc);
    if (items == null) return itemz;
    CoreLanguage lang = getCurrTranslator();
    for(Object o : items.entrySet()) {
      Map.Entry e = (Map.Entry) o;
      Map       m = (Map ) e.getValue();
      String    k = (String) e.getKey();
      String    n = (String) m.get("_disp");
      if (n == null || "".equals(n)) {
          n = "core.item."+name+"."+namc+"."+k;
      }
      Map       u = new LinkedHashMap();
      u.putAll( m );
      u.put("_disp", lang.translate(n));
      itemz.put(k, u);
    }
    return itemz;
  }

  public Map getRolesTranslated(String namc)
    throws HongsException
  {
    Map rolez = new LinkedHashMap();
    Map roles = getRoles(namc);
    if (roles == null) return rolez;
    CoreLanguage lang = getCurrTranslator();
    for(Object o : roles.entrySet()) {
      Map.Entry e = (Map.Entry) o;
      Map       m = (Map ) e.getValue();
      String    k = (String) e.getKey();
      String    n = (String) m.get("_disp");
      if (n == null || "".equals(n)) {
          n = "core.role."+name+"."+namc+"."+k;
      }
      Map       u = new LinkedHashMap();
      u.putAll( m );
      u.put("_disp", lang.translate(n));
      rolez.put(k, u);
    }
    return rolez;
  }

  //** 工厂方法 **/

  public static SourceConfig getInstance(String name) throws HongsException {
      String key = SourceConfig.class.getName() + ":" + name;
      Core core = Core.getInstance();
      SourceConfig inst;
      if (core.containsKey(key)) {
          inst = (SourceConfig)core.get(key);
      }
      else {
          inst = new SourceConfig(name);
          core.put( key, inst );
      }
      return inst;
  }

  public static SourceConfig getInstance() throws HongsException {
      return getInstance("default");
  }
}
