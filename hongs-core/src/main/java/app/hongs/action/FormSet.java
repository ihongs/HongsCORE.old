package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.CoreSerially;
import app.hongs.HongsException;
import app.hongs.util.Synt;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    forms = {
        "form_name" : {
            "item_name" : {
                __disp__ : "Label",
                __type__ : "string|number|date|file|enum|form",
                __rule__ : "Rule Func Name",
                __required__ : yes|no,
                __repeated__ : yes}no,
                "name" : "Value"
                ...
            }
            ...
        }
        ...
    }
    enums = {
        "enum_name" : {
            "code" : "Label"
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
public class FormSet
  extends CoreSerially
{

  private String name;

  /**
   * 表单集合
   */
  public Map<String, Map> forms;

  /**
   * 枚举集合
   */
  public Map<String, Map> enums;

  public FormSet(String name)
    throws HongsException
  {
    this.name = name;
    this.init(name + ".form");
  }

  @Override
  protected boolean expired(long time)
  {
    File xmlFile = new File(Core.CONF_PATH
                + File.separator + name + ".form.xml");
    File serFile = new File(Core.SERS_PATH
                + File.separator + name + ".form.ser");
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
        fn = Core.CONF_PATH + File.separator + name + ".form.xml";
        is = new FileInputStream(fn);
    }
    catch (FileNotFoundException ex)
    {
        fn = name.contains("/") ? name + ".form.xml" : "app/hongs/config/" + name + ".form.xml";
        is = this.getClass().getClassLoader().getResourceAsStream(fn);
        if (  is  ==  null )
        {
            throw new app.hongs.HongsError(0x2a, "Can not find the formset config file '" + name + ".form.xml'.");
        }
    }

    Element root;
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbn = dbf.newDocumentBuilder();
      Document  doc = dbn.parse( is );
      root = doc.getDocumentElement();
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

    this.forms = new HashMap();
    this.enums = new HashMap();
    this.parse(root, this.forms, this.enums);
  }

  private void parse(Element element, Map forms, Map enums)
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

      if ("form".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map items = new LinkedHashMap();
        this.parse(element2, items, null);
        forms.put(namz, items);
      }
      if ("enum".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map items = new LinkedHashMap();
        this.parse(element2, null, items);
        enums.put(namz, items);
      }
      if ("field".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        if (namz == null) namz = "";
        Map items = new LinkedHashMap();
        this.parse(element2, items, null);
        forms.put(namz, items);

        namz = element2.getAttribute("disp");
        items.put("__disp__", Synt.declare(namz, ""));

        namz = element2.getAttribute("type");
        items.put("__type__", Synt.declare(namz, ""));

        namz = element2.getAttribute("rule");
        items.put("__rule__", Synt.declare(namz, ""));

        namz = element2.getAttribute("required");
        items.put("__required__", Synt.declare(namz, false));

        namz = element2.getAttribute("repeated");
        items.put("__repeated__", Synt.declare(namz, false));
      }
      else
      if ("param".equals(tagName2))
      {
        String namz = element2.getAttribute("name");
        String text = element2.getTextContent();
        forms.put(namz, text);
      }
      else
      if ("value".equals(tagName2))
      {
        String namz = element2.getAttribute("code");
        String text = element2.getTextContent();
        enums.put(namz, text);
      }
    }
  }

  public Map getEnum(String name) {
    return enums.get(name);
  }

  public Map getForm(String name) {
    return forms.get(name);
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
    Map anum = getEnum(name);
    Map data = new LinkedHashMap();
    if (anum == null) return data ;
    data.putAll(anum);
    for (Object o : data.entrySet()) {
      Map.Entry e = (Map.Entry) o ;
      String    n = (String) e.getValue();
      if (n == null || "".equals(n)) {
          n = "fore.enum."+name+"."+namc+"."+(String) e.getKey();
      }
      e.setValue( lang.translate(n));
    }
    return data;
  }

  public Map getFormTranslated(String namc)
    throws HongsException
  {
    Map itemz = new LinkedHashMap();
    Map items = getForm(namc);
    if (items == null) return itemz;
    CoreLanguage lang = getCurrTranslator();
    for(Object o : items.entrySet()) {
      Map.Entry e = (Map.Entry) o;
      Map       m = (Map ) e.getValue();
      String    k = (String) e.getKey();
      String    n = (String) m.get("__disp__");
      if (n == null || "".equals(n)) {
          n = "fore.form."+name+"."+namc+"."+k;
      }
      Map       u = new LinkedHashMap(  );
      u.putAll( m );
      u.put("__disp__",lang.translate(n));
      itemz.put(k, u);
    }
    return itemz;
  }

  //** 工厂方法 **/

  public static FormSet getInstance(String name) throws HongsException {
      String key = FormSet.class.getName() + ":" + name;
      Core core = Core.getInstance();
      FormSet inst;
      if (core.containsKey(key)) {
          inst = (FormSet)core.get(key);
      }
      else {
          inst = new FormSet(name);
          core.put( key, inst );
      }
      return inst;
  }

  public static FormSet getInstance() throws HongsException {
      return getInstance("default");
  }
}
