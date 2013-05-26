package app.hongs.util;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;
import java.util.Iterator;
import java.util.Enumeration;

import java.io.PrintStream;

import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import app.hongs.Core;
import app.hongs.HongsError;
import java.io.PrintWriter;

/**
 * <h1>简单JSON格式工具</h1>
 * <pre>
 * 支持将 <b>数组,集合框架,基础类型</b> 的数据转换为JSON字符串, 反向解析JSON字符串到
 * Java对象; 暂采用 org.json.simple 来完成.
 * 顺便说说为什么不采用第3方的JSON库: 最开始采用的是org.json, 效果相当不错, 可惜在解
 * 析JSON时首先解析成他自身的对象而不是Java集合框架的对象; 后来采用org.json.simple,
 * 也很好, 但是对Set类型不支持, 修改其源码将List改成Collection即可解决; 再后来考虑到
 * 我已经有一个Dump类, 用于调试输出基础数据类型, 其实现与JSON大同小异, 故将其修改成该
 * JSON类. 但是JSON的解析太麻烦, 就还是调org.json.simple来做了.
 * </pre>
 * 
 * <h2>错误代码</h2>
 * <pre>
 * 0x40 解析JSON数据错误
 * </pre>
 *
 * @author Hongs
 */
public class JSON
{

  /**
   * 将JSON字符串解析成Java对象
   * @param str JSON字符串
   * @return 数组,集合框架,基础类型
   */
  public static Object parse(String str)
  {
    try
    {
      return JSONValue
      .parseWithException(str);
    }
    catch (ParseException exp)
    {
      throw new HongsError(0x40, exp);
    }
  }

  /**
   * 直接将Java对象输出到标准控制台, 用于简单调试
   * @param obj 数组,集合框架,基础类型
   */
  public static void print(Object obj)
  {
    StringBuilder sb = new StringBuilder();
    JSON.print(sb, "", null, obj, 0, 0);
    System.out.print(sb.toString());
  }

  /**
   * 直接将Java对象输出到指定输出流, 用于简单调试
   * @param obj
   * @param out
   */
  public static void print(Object obj, PrintStream out)
  {
    StringBuilder sb = new StringBuilder();
    JSON.print(sb, "", null, obj, 0, 0);
    out.print(sb.toString());
  }

  /**
   * 直接将Java对象输出到指定输出器, 用于简单调试
   * @param obj
   * @param out
   */
  public static void print(Object obj, PrintWriter out)
  {
    StringBuilder sb = new StringBuilder();
    JSON.print(sb, "", null, obj, 0, 0);
    out.print(sb.toString());
  }

  /**
   * 将Java对象转换为JSON字符串
   * @param obj 数组,集合框架,基础类型
   * @return JSON字符串
   */
  public static String toString(Object obj)
  {
    String sp = Core.IN_DEBUG_MODE ? "" : null;
    StringBuilder sb = new StringBuilder();
    JSON.print(sb, sp, null, obj, 0, 0);
    return sb.toString();
  }

  /** 操作方法 **/

  private static void print(StringBuilder sb, String pre, Object[] arr)
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

      JSON.print(sb, pre == null ? pre : pre + "  ", null, obj, i, j);
    }

    if (pre != null)
    {
      sb.append(pre);
    }
    sb.append("]");
  }

  private static void print(StringBuilder sb, String pre, Collection col)
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

      JSON.print(sb, pre == null ? pre : pre + "  ", null, obj, i, j);

      i ++;
    }

    if (pre != null)
    {
      sb.append(pre);
    }
    sb.append("]");
  }

  private static void print(StringBuilder sb, String pre, Dictionary dic)
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

      JSON.print(sb, pre == null ? pre : pre + "  ", key, val, i, j);

      i ++;
    }

    if (pre != null)
    {
      sb.append(pre);
    }
    sb.append("}");
  }

  private static void print(StringBuilder sb, String pre, Map map)
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

      JSON.print(sb, pre == null ? pre : pre + "  ", key, val, i, j);

      i ++;
    }

    if (pre != null)
    {
      sb.append(pre);
    }
    sb.append("}");
  }

  private static void print(StringBuilder sb, String pre, Object key, Object val, int i, int j)
  {
    /** 键 **/

    if (pre != null)
    {
      sb.append(pre);
    }

    if (key != null)
    {
      if (key instanceof Number || key instanceof Boolean)
      {
        sb.append(key.toString());
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
      sb.append("null");
    }
    else if (val instanceof Object[])
    {
      JSON.print(sb, pre, (Object[])val);
    }
    else if (val instanceof Collection)
    {
      JSON.print(sb, pre, (Collection)val);
    }
    else if (val instanceof Dictionary)
    {
      JSON.print(sb, pre, (Dictionary)val);
    }
    else if (val instanceof Map)
    {
      JSON.print(sb, pre, (Map)val);
    }
    else if (val instanceof Number || val instanceof Boolean)
    {
      sb.append(val.toString());
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

  /** 编码 **/
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
