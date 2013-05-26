package app.hongs.action;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import app.hongs.Core;
import app.hongs.CoreSerially;
import app.hongs.HongsException;
import app.hongs.util.JSON;

/**
 * <h1>数据配置工具</h1>
 * 
 * @author Hongs
 */
public class DatumsConfig
  extends CoreSerially
{

  private String name;

  public Map<String, Object> datas;
  public Map<String, Set<String[]>> getDatas;
  public Map<String, Set<String[]>> refDatas;

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
                + File.separator + "dat-" + name + ".xml");
    File serFile = new File(Core.TMPS_PATH
                + File.separator + "dat-" + name + ".ser");
    return xmlFile.lastModified() > serFile.lastModified();
  }

  @Override
  protected void loadData()
    throws HongsException
  {
    File df = new File(Core.CONF_PATH
                + File.separator + "dat-" + name + ".xml");
    if (!df.exists())
    {
      throw new HongsException(0x10e0, "Action config file '"
                + Core.CONF_PATH
                + File.separator + "dat-" + name + ".xml"
                + "' is not exists");
    }

    this.datas = new HashMap();
    this.getDatas = new HashMap();
    this.refDatas = new HashMap();

    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbn = dbf.newDocumentBuilder();
      Document doc = dbn.parse(df);

      this.parseDatTree(doc.getDocumentElement( ),
        this.datas, null, this.getDatas, this.refDatas);

      // 测试
      /*
      app.hongs.util.JSON.print(datas);
      app.hongs.util.JSON.print(getDatas);
      app.hongs.util.JSON.print(refDatas);
      */
    }
    catch (IOException ex)
    {
      throw new HongsException(0x10e2, ex);
    }
    catch (SAXException ex)
    {
      throw new HongsException(0x10e2, ex);
    }
    catch (ParserConfigurationException ex)
    {
      throw new HongsException(0x10e2, ex);
    }
  }

  private void parseDatTree(Element element, Object datas, Set links, Map getDatas, Map refDatas)
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
      String  tagName2  = element2.getTagName();

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
          data2 = this.parseDatText(
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

        this.parseDatTree(element2, data2, null, null, null);
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
      ||  "ref".equals(tagName2))
      {
        Map data2 = new HashMap();
        Set link2 = new HashSet();

        this.parseDatTree(element2, data2, link2, null, null);

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

        if ("ref".equals(tagName2))
        {
          refDatas.put(uri, link2);
        }
        else
        {
          getDatas.put(uri, link2);
        }
      }
    }
  }

  private Object parseDatText(String text, String type)
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

    return data;
  }

  public Object getDataByKey(String key)
  {
    return this.datas.get(key);
  }

  public Map getDataByUri(String uri)
  {
    Map map = new HashMap();
    if (this.getDatas.containsKey(uri))
    {
      Set <String[]> data = this.getDatas.get(uri);
      for (String[] key : data)
      {
        map.put(key[0], this.getDataByKey(key[1]));
      }
    }
    return map;
  }

  public Map getDataByRef(String uri)
  {
    Map map = new HashMap();
    if (this.getDatas.containsKey(uri))
    {
      Set <String[]> data = this.getDatas.get(uri);
      for (String[] key : data)
      {
        map.put(key[0], this.getDataByKey(key[1]));
      }
    }
    return map;
  }
}
