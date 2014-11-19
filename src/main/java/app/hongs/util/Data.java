package app.hongs.util;

import app.hongs.Core;
import app.hongs.HongsException;

import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * 简单JSON格式工具
 *
 * <p>
 * 支持将 <b>数组,集合框架,基础类型</b> 的数据转换为 Data 字符串,
 反向解析 Data 字符串到 Java 对象; 暂采用 org.json.simple 来完成.
 </p>
 * 
 * <p>
 顺便说说为什么不采用第3方的 Data 库:
 最开始用 org.json, 还不错, 可惜在解析 Data 时会解析成他自身的对象而不是 Java 集合框架的对象;
 后来采用 org.json.simple, 也很好, 但是不支持 Set, 需要修改其源码将 List 改成 Collection;
 考虑到我有一个 Dump 类, 用于调试输出基础类型和集合对象, 其实现与 Data 大同小异,
 故将其修改成该 JSON类; 但是 Data 的解析太麻烦, 就还是调 org.json.simple 好了.
 另, 最近听闻有些移动端编程对 null 不方便处理, 加了一个参数来用于全部转为字符串.
 </p>
 *
 * <h3>异常代码</h3>
 * <pre>
 * 0x1121 解析JSON数据错误
 * </pre>
 *
 * @author Hongs
 */
public class Data
{

  /**
   * 将JSON字符串解析成Java对象
   * @param str JSON字符串
   * @return 数组,集合框架,基础类型
   */
  public static Object toObject(String str) throws HongsException
  {
    try
    {
      return JSONValue.parseWithException(str);
    }
    catch (ParseException ex)
    {
      throw new HongsException(0x100a, "Can not parse data by json", ex);
    }
  }

  /**
   * 将Java对象转换为JSON字符串
   * @param obj 数组,集合框架,基础类型
   * @return JSON字符串
   */
  public static String toString(Object obj)
  {
    return toString(obj, false);
  }

  /**
   * 将Java对象转换为JSON字符串
   * @param obj 数组,集合框架,基础类型
   * @param bnn2Str Boolean,Number,Null 均转换为字符串, false为"0",true为"1",null为""
   * @return JSON字符串
   */
  public static String toString(Object obj, boolean bnn2Str)
  {
    String xx = 1 == (1 & Core.DEBUG) ? "": null;
    StringBuilder sb = new StringBuilder();
    Data.dumps(sb, xx, null, obj, 0, 0, bnn2Str);
    return sb.toString().trim();
  }

  /**
   * 直接将Java对象输出到标准控制台, 用于简单调试, 输出到 STDERR
   * @param obj 数组,集合框架,基础类型
   */
  public static void dumps(Object obj)
  {
    StringBuilder sb = new StringBuilder();
    Data.dumps(sb, "", null, obj, 0, 0, true);
    System.err.print(sb.toString());
  }

  /**
   * 直接将Java对象输出到指定输出流
   * @param obj
   * @param out
   */
  public static void dumps(Object obj, PrintStream out)
  {
    StringBuilder sb = new StringBuilder();
    Data.dumps(sb, "", null, obj, 0, 0, true);
    out.print(sb.toString());
  }

  /**
   * 直接将Java对象输出到指定书写器
   * @param obj
   * @param out
   */
  public static void dumps(Object obj, PrintWriter out)
  {
    StringBuilder sb = new StringBuilder();
    Data.dumps(sb, "", null, obj, 0, 0, true);
    out.print(sb.toString());
  }

  //** 操作方法 **/

  private static void dumps(StringBuilder sb, String pre, Object key, Object val, int i, int j, boolean s)
  {
    /** 键 **/

    if (pre != null)
    {
      sb.append(pre);
    }

    if (key != null)
    {
      if (key instanceof Boolean)
      {
        sb.append(!s ? val.toString() : (((Boolean)val) ? "\"1\"" : "\"0\""));
      }
      else
      {
        sb.append("\"")
          .append(JSONValue.escape(key.toString()))
          .append("\"");
      }

      sb.append(":");
    }

    /** 值 **/

    if (val == null)
    {
      sb.append(!s ? "null" : "\"\"");
    }
    else if (val instanceof Object[])
    {
      Data.dumps(sb, pre, (Object[])val, s);
    }
    else if (val instanceof Collection)
    {
      Data.dumps(sb, pre, (Collection)val, s);
    }
    else if (val instanceof Dictionary)
    {
      Data.dumps(sb, pre, (Dictionary)val, s);
    }
    else if (val instanceof Map)
    {
      Data.dumps(sb, pre, (Map)val, s);
    }
    else if (val instanceof Number && !s)
    {
      sb.append(val.toString());
    }
    else if (val instanceof Boolean)
    {
      sb.append(!s ? val.toString() : (((Boolean)val) ? "\"1\"" : "\"0\""));
    }
    else
    {
      sb.append('"');
      sb.append(JSONValue.escape(val.toString()));
      sb.append('"');
    }

    if (i < j - 1)
    {
      sb.append(",");
    }
    if (pre != null)
    {
      sb.append("\r\n");
    }
  }

  private static void dumps(StringBuilder sb, String pre, Object[] arr, boolean s)
  {
    sb.append("[");
    if (pre != null)
    {
      sb.append("\r\n");
    }

    int j = arr.length;

    for (int i = 0; i < arr.length; i ++)
    {
      Object obj = arr[i];

      Data.dumps(sb, pre == null ? pre : pre + "\t", null, obj, i, j, s);
    }

    if (pre != null)
    {
      sb.append(pre);
    }
    sb.append("]");
  }

  private static void dumps(StringBuilder sb, String pre, Collection col, boolean s)
  {
    sb.append("[");
    if (pre != null)
    {
      sb.append("\r\n");
    }

    int i = 0;
    int j = col.size();

    Iterator it = col.iterator();
    while (it.hasNext())
    {
      Object obj = it.next();

      Data.dumps(sb, pre == null ? pre : pre + "\t", null, obj, i, j, s);

      i ++;
    }

    if (pre != null)
    {
      sb.append(pre);
    }
    sb.append("]");
  }

  private static void dumps(StringBuilder sb, String pre, Dictionary dic, boolean s)
  {
    sb.append("{");
    if (pre != null)
    {
      sb.append("\r\n");
    }

    int i = 0;
    int j = dic.size();

    Enumeration en = dic.keys();
    while (en.hasMoreElements())
    {
      Object key = en.nextElement();
      Object val = dic.get(key);

      Data.dumps(sb, pre == null ? pre : pre + "\t", key, val, i, j, s);

      i ++;
    }

    if (pre != null)
    {
      sb.append(pre);
    }
    sb.append("}");
  }

  private static void dumps(StringBuilder sb, String pre, Map map, boolean s)
  {
    sb.append("{");
    if (pre != null)
    {
      sb.append("\r\n");
    }

    int i = 0;
    int j = map.size();

    Iterator it = map.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry obj = (Map.Entry)it.next();
      Object key = obj.getKey();
      Object val = obj.getValue();

      Data.dumps(sb, pre == null ? pre : pre + "\t", key, val, i, j, s);

      i ++;
    }

    if (pre != null)
    {
      sb.append(pre);
    }
    sb.append("}");
  }

  //** 编码 **/
  /*
  private final static String[] hex = {
      "00","01","02","03","04","05","06","07","08","09","0A","0B","0C","0D","0E","0F",
      "10","11","12","13","14","15","16","17","18","19","1A","1B","1C","1D","1E","1F",
      "20","21","22","23","24","25","26","27","28","29","2A","2B","2C","2D","2E","2F",
      "30","31","32","33","34","35","36","37","38","39","3A","3B","3C","3D","3E","3F",
      "40","41","42","43","44","45","46","47","48","49","4A","4B","4C","4D","4E","4F",
      "50","51","52","53","54","55","56","57","58","59","5A","5B","5C","5D","5E","5F",
      "60","61","62","63","64","65","66","67","68","69","6A","6B","6C","6D","6E","6F",
      "70","71","72","73","74","75","76","77","78","79","7A","7B","7C","7D","7E","7F",
      "80","81","82","83","84","85","86","87","88","89","8A","8B","8C","8D","8E","8F",
      "90","91","92","93","94","95","96","97","98","99","9A","9B","9C","9D","9E","9F",
      "A0","A1","A2","A3","A4","A5","A6","A7","A8","A9","AA","AB","AC","AD","AE","AF",
      "B0","B1","B2","B3","B4","B5","B6","B7","B8","B9","BA","BB","BC","BD","BE","BF",
      "C0","C1","C2","C3","C4","C5","C6","C7","C8","C9","CA","CB","CC","CD","CE","CF",
      "D0","D1","D2","D3","D4","D5","D6","D7","D8","D9","DA","DB","DC","DD","DE","DF",
      "E0","E1","E2","E3","E4","E5","E6","E7","E8","E9","EA","EB","EC","ED","EE","EF",
      "F0","F1","F2","F3","F4","F5","F6","F7","F8","F9","FA","FB","FC","FD","FE","FF"
  };

  private final static byte[] val = {
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
      0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F
  };

  public static String encode(String str) {
    StringBuilder sb = new StringBuilder();
    int len = str.length();
    int i = 0;
    while (i < len) {
      int ch = str.charAt(i);
      if (ch == ' ') {                        // space : map to '+'
        sb.append('+');
      } else if ('0' <= ch && ch <= '9') {    // '0'..'9' : as it was
        sb.append((char)ch);
      } else if ('A' <= ch && ch <= 'Z') {    // 'A'..'Z' : as it was
        sb.append((char)ch);
      } else if ('a' <= ch && ch <= 'z') {    // 'a'..'z' : as it was
        sb.append((char)ch);
      } else if (ch == '-' || ch == '_'       // unreserved : as it was
        || ch == '.' || ch == '!'
        || ch == '~' || ch == '*'
        || ch == '(' || ch == ')'
        || ch == '\'') {
        sb.append((char)ch);
      } else if (ch <= 0x007F) {              // ASCII : map to %XX
        sb.append('%');
        sb.append(hex[ch]);
      } else {                                // Unicode : map to %uXXXX
        sb.append('%');
        sb.append('u');
        sb.append(hex[(ch >>> 8)]);
        sb.append(hex[(0x00FF & ch)]);
      }
      i++;
    }
    return sb.toString();
  }

  public static String decode(String str) {
    StringBuilder sb = new StringBuilder();
    int len = str.length();
    int i = 0;
    while (i < len) {
      int ch = str.charAt(i);
      if (ch == '+') {                        // + : map to ' '
        sb.append(' ');
      } else if ('0' <= ch && ch <= '9') {    // '0'..'9' : as it was
        sb.append((char)ch);
      } else if ('A' <= ch && ch <= 'Z') {    // 'A'..'Z' : as it was
        sb.append((char)ch);
      } else if ('a' <= ch && ch <= 'z') {    // 'a'..'z' : as it was
        sb.append((char)ch);
      } else if (ch == '-' || ch == '_'       // unreserved : as it was
        || ch == '.' || ch == '!'
        || ch == '~' || ch == '*'
        || ch == '(' || ch == ')'
        || ch == '\'') {
        sb.append((char)ch);
      } else if (ch == '%') {
        int cint = 0;
        if ('u' != str.charAt(i+1)) {         // %XX : map to ASCII(XX)
          cint = (cint << 4) | val[str.charAt(i+1)];
          cint = (cint << 4) | val[str.charAt(i+2)];
          i+=2;
        } else {                              // %uXXXX : map to Unicode(XXXX)
          cint = (cint << 4) | val[str.charAt(i+2)];
          cint = (cint << 4) | val[str.charAt(i+3)];
          cint = (cint << 4) | val[str.charAt(i+4)];
          cint = (cint << 4) | val[str.charAt(i+5)];
          i+=5;
        }
        sb.append((char)cint);
      }
      i++;
    }
    return sb.toString();
  }
  */
}
