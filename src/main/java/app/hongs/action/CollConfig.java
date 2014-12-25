package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.CoreSerially;
import app.hongs.HongsException;
import app.hongs.util.Data;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
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
     "option_name" : OptionStr
     ...
   }
   ...
 }
 forms = {
   "form_name" : {
     _extr : "recode_engine",
     "item_name" : {
       _extr : "verify_method",
       _type : "string|bigstr|number|slider|switch|date|time|datetime|password|file|enum|form|exists|unique",
       _required : 0|1|2,
       _repeated : 0|1,
       "attrib_name" : AttribStr
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
public class CollConfig
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

  public CollConfig(String name)
    throws HongsException
  {
    this.name = name;
    this.init("form." + name);
  }

  @Override
  protected boolean expired(long time)
  {
    File xmlFile = new File(Core.CONF_PATH
                + File.separator + name + ".coll.xml");
    File serFile = new File(Core.SERS_PATH
                + File.separator + name + ".coll.ser");
    return xmlFile.lastModified() > serFile.lastModified();
  }

  @Override
  protected void imports()
    throws HongsException
  {
    File df = new File(Core.CONF_PATH
                + File.separator + name + ".coll.xml");
    if (!df.exists())
    {
      throw new HongsException(0x10e8, "Form config file '"
                + Core.CONF_PATH
                + File.separator + name + ".coll.xml"
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

      this.parse(root, this.datas, this.enums, this.forms, null);
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

  private void parse(Element element, Map datas, Map enums, Map forms, Map optns)
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
        this.parse(element2, null, null, null, optns2);
        enums.put(namz, optns2);
      }
      else
      if ("form".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map items2 = new LinkedHashMap();
        this.parse(element2, null, items2, null, null);
        forms.put(namz, items2);
        
        namz = element2.getAttribute("extr");
        if (namz != null) {
            items2.put("_extr", namz);
        } else {
            items2.put("_extr",  "" );
        }
      }
      else
      if ("item".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map optns2 = new LinkedHashMap();
        this.parse(element2, null, null, null, optns2);
        enums.put(namz, optns2);

        namz = element2.getAttribute("extr");
        if (namz != null) {
            optns2.put("_extr", namz);
        } else {
            optns2.put("_extr",  "" );
        }

        namz = element2.getAttribute("type");
        if (namz != null) {
            optns2.put("_type",  namz );
        } else {
            optns2.put("_type", "text");
        }

        namz = element2.getAttribute("required");
        if (namz != null && !"".equals(namz) && !"0".equals(namz)) {
            optns2.put("_required", namz);
        } else {
            optns2.put("_required", "0" );
        }

        namz = element2.getAttribute("repeated");
        if (namz != null && !"".equals(namz) && !"0".equals(namz)) {
            optns2.put("_repeated", namz);
        } else {
            optns2.put("_repeated", "0" );
        }
      }
      else
      if ("option".equals(tagName2) || "attrib".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        String data = element2.getTextContent();
        optns.put(namz, data);
      }
    }
  }

  public Map getEnum(String name)
  {
    return enums.get(name);
  }

  public Map getForm(String name)
  {
    return forms.get(name);
  }

  public Map getForm(String name, boolean withReadonlyItem, boolean withRepeatedForm)
    throws HongsException
  {
    return CollConfig.this.getForm(name, withReadonlyItem, withRepeatedForm, "");
  }

  private Map getForm(String name, boolean withReadonlyItem, boolean withRepeatedForm, String pref)
    throws HongsException
  {
    CoreLanguage lang = CoreLanguage.getInstance(this.name);
    Map  form = CollConfig.this.getForm( name );
    Map  dict = new HashMap(  );
    List list = new ArrayList();

    Iterator it = form.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry et = (Map.Entry) it.next();
        String n = (String) et.getKey();
        Map    m = (Map)  et.getValue();
        String t = (String) m.get("type");
        Integer rq = (Integer) m.get("required");
        Integer rp = (Integer) m.get("repeated");

        if (!withReadonlyItem && 2 == rq) {
            continue;
        }
        if (!withRepeatedForm && 0 != rp && "form".equals(t)) {
            continue;
        }

        Map item = new HashMap();
        list.add(item);
        item.putAll(m);

        if ("form".equals(t)) {
            String  n2 = (String) m.get("form");
            String[] a = n2.split ( "\\." , 2 );
            CollConfig c2;
            if (a.length > 1) {
                c2 = getInstance(a[0]);
                n2 = a[1];
            } else {
                c2 = this;
            }
            String  p2 = pref + n2 +  ".";
            if ( 0 != rp ) p2 = p2 + "#.";
            item.putAll(c2.getForm(n2, withRepeatedForm, withReadonlyItem, p2));
            continue;
        }

        item.put("code", pref + n);
        item.put("name", lang.translate("core.item." + this.name + name + "." + n));
        item.put("help", lang.translate("core.help." + this.name + name + "." + n));

        if ("enum".equals(t)) {
            String n2 = (String) m.get("enum");
            Map enum2 = getEnum(n2);
            item.put("enum", enum2);
        }
    }

    name = lang.translate("core.form."+this.name+"."+name);
    dict.put("list", list);
    dict.put("name", name);
    return dict;
  }

  //** 工厂方法 **/

  public static CollConfig getInstance(String name) throws HongsException {
      String key = CollConfig.class.getName() + ":" + name;
      Core core = Core.getInstance();
      CollConfig inst;
      if (core.containsKey(key)) {
          inst = (CollConfig)core.get(key);
      }
      else {
          inst = new CollConfig(name);
          core.put( key, inst );
      }
      return inst;
  }

  public static CollConfig getInstance() throws HongsException {
      return getInstance("default");
  }
}
