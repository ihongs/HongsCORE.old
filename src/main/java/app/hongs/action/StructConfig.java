package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.CoreSerially;
import app.hongs.HongsException;
import app.hongs.util.Data;
import app.hongs.util.Tree;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
     "value" : "Label"
     ...
   }
   ...
 }
 forms = {
   "form_name" : {
     _disp: "Label",
     _href: "URI",
     units: {
       "unit_name" : {
         _disp: "Label",
         _href: "URI",
         actions: set[
           "action"
           ...
         ],
         depends: set[
           ["conf_name", "form_name", "unit_name"]
           ...
         ]
       }
     },
     items: {
       "item_name" : {
         _disp : "Label",
         _type : "form|enum|file|text|number|slider|switch|date|time|datetime|tel|url|email",
         _rule : "rule_method",
         _required : 0|1,
         _repeated : 0|1,
         "param" : "Value"
         ...
       }
       ...
     }
     ...
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
public class StructConfig
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
  public Map<String, Map> forms;

  public StructConfig(String name)
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
      throw new HongsException(0x10e8, "Form config file '"
                + Core.CONF_PATH
                + File.separator + name + ".as.xml"
                + "' is not exists");
    }

    this.datas = new HashMap();
    this.enums = new HashMap();
    this.forms = new HashMap();

    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbn = dbf.newDocumentBuilder();
      Document doc = dbn.parse(df);
      Element root = doc.getDocumentElement();

      this.parse(root, this.datas, this.enums, this.forms);
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
      if ("form".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map forms2 = new LinkedHashMap();
        Map units2 = new LinkedHashMap();
        Map items2 = new LinkedHashMap();
        this.parse(element2, units2, items2, null );
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

        forms2.put("units", units2);
        forms2.put("items", items2);
      }
      else
      if ("unit".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map units2 = new LinkedHashMap();
        Map actns2 = new LinkedHashMap();
        Map depns2 = new LinkedHashMap();
        this.parse(element2, actns2, depns2, null );
        datas.put(namz, units2);

        namz = element2.getAttribute("disp");
        if (namz != null) {
            units2.put("_disp", namz);
        } else {
            units2.put("_disp",  "" );
        }

        namz = element2.getAttribute("href");
        if (namz != null) {
            units2.put("_href", namz);
        } else {
            units2.put("_href",  "" );
        }

        units2.put("actions", new LinkedHashSet(actns2.keySet()));
        units2.put("depends", new LinkedHashSet(depns2.keySet()));
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
            optns2.put("_required", Short.parseShort(namz));
        } else {
            optns2.put("_required", (byte) 0);
        }

        namz = element2.getAttribute("repeated");
        if (namz != null && !"".equals(namz) && !"0".equals(namz)) {
            optns2.put("_repeated", Short.parseShort(namz));
        } else {
            optns2.put("_repeated", (byte) 0);
        }
      }
      else
      if ("option".equals(tagName2))
      {
        String namz = element2.getAttribute("value");
        String data = element2.getTextContent();
        datas.put(namz, data);
      }
      else
      if ("attrib".equals(tagName2))
      {
        String namz = element2.getAttribute("param");
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
        String data = element2.getTextContent();
        String[ ] a = data.split("::");
        String conf, form, unit;
        if (a.length == 1) {
            String namz = ((Element)element2.getParentNode()).getAttribute("name");
            unit = a[0];
            form = namz;
            conf = name;
        }
        else
        if (a.length == 2) {
            unit = a[1];
            form = a[0];
            conf = name;
        }
        else {
            unit = a[2];
            form = a[1];
            conf = a[0];
        }
        a = new String[] {conf, form, unit};
        datas.put( a  , null);
      }
    }
  }

  public Object getData(String name) {
    return Tree.getDepth2(datas, new String[]{name});
  }

  public <T>T getData(String name, T data) {
    return Tree.getDepth(datas, data, new String[]{name});
  }

  public Object getTree(String path) {
    return Tree.getValue2(datas, path);
  }

  public <T>T getTree(String path, T data) {
    return Tree.getValue(datas, data, path);
  }

  public Map getEnum(String name) {
    return enums.get(name);
  }

  public Map getForm(String name) {
    return forms.get(name);
  }

  public Map getEnumTranslated(String namc) {
    CoreLanguage lang = CoreLanguage.getInstance(name);
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

  public Map getItemsTranslated(String namc)
    throws HongsException
  {
    CoreLanguage lang = CoreLanguage.getInstance(name);
    Map itemz =  new LinkedHashMap();
    Map form = (Map) forms.get(namc);
    if (form == null) return itemz;
    for (Object o :((Map ) form.get("items")).entrySet()) {
      Map.Entry e = (Map.Entry) o ;
      Map       m = (Map ) e.getValue();
      String    k = (String) e.getKey();
      String    n = (String) m.get("_disp");
      if (n == null ||  "".equals(n)) {
          n = "core.item."+name+"."+namc+"."+k;
      }
      Map       u = new LinkedHashMap();
      u.putAll( m );
      u.put("_disp", lang.translate(n));
      itemz.put(k, u);
    }
    return itemz;
  }

  public Map getUnitsTranslated(String namc)
    throws HongsException
  {
    CoreLanguage lang = CoreLanguage.getInstance(name);
    Map form = (Map) forms.get(namc);
    Map unitz =  new LinkedHashMap();
    if (form == null) return unitz;
    for (Object o :((Map ) form.get("units")).entrySet()) {
      Map.Entry e = (Map.Entry) o ;
      Map       m = (Map ) e.getValue();
      String    k = (String) e.getKey();
      String    n = (String) m.get("_disp");
      if (n == null ||  "".equals(n)) {
          n = "core.unit."+name+"."+namc+"."+k;
      }
      Map       u = new LinkedHashMap();
      u.putAll( m );
      u.put("_disp", lang.translate(n));
      unitz.put(k, u);
    }
    return unitz;
  }

  public Map getFormsTranslated()
    throws HongsException
  {
    CoreLanguage lang = CoreLanguage.getInstance(name);
    Map formz =  new LinkedHashMap();
    for (Object o : forms.entrySet()) {
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

  //** 工厂方法 **/

  public static StructConfig getInstance(String name) throws HongsException {
      String key = StructConfig.class.getName() + ":" + name;
      Core core = Core.getInstance();
      StructConfig inst;
      if (core.containsKey(key)) {
          inst = (StructConfig)core.get(key);
      }
      else {
          inst = new StructConfig(name);
          core.put( key, inst );
      }
      return inst;
  }

  public static StructConfig getInstance() throws HongsException {
      return getInstance("default");
  }
}
