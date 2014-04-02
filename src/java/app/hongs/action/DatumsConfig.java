package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreSerially;
import app.hongs.HongsException;
import app.hongs.util.JSON;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 数据配置
 *
 * <p>
 * 在 Java 7 之前写 List,Set,Map 很麻烦, 不能像写 JSON 那样方便;<br/>
 * 有些数据需要方便修改, 而修改代码比较麻烦;<br/>
 * 需要统一管理一些数据, 如状态/类型/选项等.<br/>
 * 故编写此类用于解决以上问题, 可从 XML 中读取结构数据或 JSON 数据.<br/>
 * XML 的结构请参考 WEB-INF/conf 中的 datums.xsd 和 datums-default.xml
 * </p>
 *
 * @author Hongs
 */
public class DatumsConfig
  extends CoreSerially
{

  private String name;

  public Map<String, Object> datas;
  public Map<String, Set<String[]>> reqDatas;
  public Map<String, Set<String[]>> rspDatas;

  public DatumsConfig(String name)
    throws HongsException
  {
    this.name = name;
    this.init("dat-" + name);
  }

  @Override
  protected boolean isExpired(long time)
  {
    File xmlFile = new File(Core.CONF_PATH
                + File.separator + "datums." + name + ".xml");
    File serFile = new File(Core.TMPS_PATH
                + File.separator + "datums." + name + ".ser");
    return xmlFile.lastModified() > serFile.lastModified();
  }

  @Override
  protected void loadData()
    throws HongsException
  {
    File df = new File(Core.CONF_PATH
                + File.separator + "datums." + name + ".xml");
    if (!df.exists())
    {
      throw new HongsException(0x10e4, "Datums config file '"
                + Core.CONF_PATH
                + File.separator + "datums." + name + ".xml"
                + "' is not exists");
    }

    this.datas = new HashMap();
    this.reqDatas = new HashMap();
    this.rspDatas = new HashMap();

    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbn = dbf.newDocumentBuilder();
      Document doc = dbn.parse(df);

      this.parseData(doc.getDocumentElement(),
        this.datas, this.reqDatas, this.rspDatas, null);

      // 测试
      /*
      app.hongs.util.JSON.print(datas);
      app.hongs.util.JSON.print(reqDatas);
      app.hongs.util.JSON.print(refDatas);
      */
    }
    catch (IOException ex)
    {
      throw new HongsException(0x10e6, ex);
    }
    catch (SAXException ex)
    {
      throw new HongsException(0x10e6, ex);
    }
    catch (ParserConfigurationException ex)
    {
      throw new HongsException(0x10e6, ex);
    }
  }

  private void parseData(Element element, Object datas,
    Map reqDatas, Map rspDatas, Set links)
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
        String type = element2.getAttribute("type");
        String key = element2.getAttribute("key");

        Object data2;
        if ("map".equals(type))
        {
          data2 = new HashMap();
        }
        else
        if ("set".equals(type))
        {
          data2 = new HashSet();
        }
        else
        if ("list".equals(type))
        {
          data2 = new ArrayList();
        }
        else
        {
          data2 = this.parseData(
            element2.getTextContent()
                    .toString(),type);
        }

        if (datas instanceof List)
        {
          ((List)datas).add(data2);
        }
        else
        if (datas instanceof Set)
        {
          ((Set)datas).add(data2);
        }
        else
        if (datas instanceof Map)
        {
          ((Map)datas).put(key, data2);
        }

        this.parseData(element2, data2, null, null, null);
      }
      else
      if ("link".equals(tagName2))
      {
        String key  = element2.getAttribute("key");
        String link = element2.getAttribute("data");

        links.add(new String[] {key, link});
      }
      else
      if ("req".equals(tagName2)
      ||  "rsp".equals(tagName2))
      {
        Map data2 = new HashMap();
        Set link2 = new HashSet();

        this.parseData(element2, data2, null, null, link2);

        String uri = element2.getAttribute("uri");

        Iterator it = data2.entrySet().iterator();
        while (it.hasNext())
        {
          Map.Entry et = (Map.Entry)it.next();
          String key = (String)et.getKey();
          Object value = et.getValue();

          String uriKey = uri+"."+key;
          ((Map) datas).put(uriKey, value);
          link2.add(new String[] {key, uriKey});
        }

        if ("rsp".equals(tagName2))
        {
          rspDatas.put(uri, link2);
        }
        else
        {
          reqDatas.put(uri, link2);
        }
      }
    }
  }

  private Object parseData(String text, String type)
    throws HongsException
  {
    Object data = text;

    if ("json".equals(type))
    {
      data = JSON.parse(text);
    }
    else
    if ("number".equals(type))
    {
      data = Double.valueOf(text);
    }
    else
    if ("boolean".equals(type))
    {
      data = Boolean.valueOf(text);
    }
    else
    if ("instance".equals(type))
    {
        try {
            data = Class.forName(text);
        } catch (ClassNotFoundException ex) {
            throw new HongsException(0x10e8, "Can not found class: "+text);
        }
    }

    return data;
  }

  private void checkData(Object data) {
      if (data instanceof List) {
          checkData((List)data);
      }
      else if (data instanceof Set) {
          checkData((Set)data);
      }
      else if (data instanceof Map) {
          checkData((Map)data);
      }
  }
  private void checkData(List data) {
      int      i = 0;
      Iterator e = data.iterator();
      while (e.hasNext()) {
          Object o = e.next();
          if (o instanceof Class) {
              o = Core.getInstance((Class)o);
              data.set(i, o);
          }
          else {
              checkData(o);
          }
          i ++;
      }
  }
  private void checkData(Set data) {
      Iterator e = data.iterator();
      while (e.hasNext()) {
          Object o = e.next();
          if (o instanceof Class) {
              o = Core.getInstance((Class)o);
              e.remove( );
              data.add(o);
          }
          else {
              checkData(o);
          }
      }
  }
  private void checkData(Map<Object, Object> data) {
      for (Map.Entry<Object, Object> e : data.entrySet()) {
          Object o = e.getValue();
          if (o instanceof Class) {
              o = Core.getInstance((Class)o);
              e.setValue(o);
          }
          else {
              checkData(o);
          }
      }
  }

  public Object getDataByKey(String key)
  {
    Object data = this.datas.get(key);
    checkData(data);
    return data;
  }

  public Map getDataByReq(String uri)
  {
    Map map = new HashMap();
    if (this.reqDatas.containsKey(uri))
    {
      Set <String[]> data = this.reqDatas.get(uri);
      for (String[] key : data)
      {
        map.put(key[0], this.getDataByKey(key[1]));
      }
    }
    return map;
  }

  public Map getDataByRsp(String uri)
  {
    Map map = new HashMap();
    if (this.rspDatas.containsKey(uri))
    {
      Set <String[]> data = this.rspDatas.get(uri);
      for (String[] key : data)
      {
        map.put(key[0], this.getDataByKey(key[1]));
      }
    }
    return map;
  }

  //** 工厂方法 **/

  public static DatumsConfig getInstance(String name) throws HongsException {
      String key = "__DAT__." + name;
      Core core = Core.getInstance();
      DatumsConfig inst;
      if (core.containsKey(key)) {
          inst = (DatumsConfig)core.get(key);
      }
      else {
          inst = new DatumsConfig(name);
          core.put( key, inst );
      }
      return inst;
  }

  public static DatumsConfig getInstance() throws HongsException {
      return getInstance("default");
  }
}
