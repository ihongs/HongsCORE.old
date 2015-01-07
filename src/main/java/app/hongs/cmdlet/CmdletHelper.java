package app.hongs.cmdlet;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.util.Util;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 外壳程序助手类
 *
 * <h3>配置选项:</h3>
 * <pre>
 * core.cmd.line.time.format    输出记录中的时间格式, 默认为"yyyy/MM/dd HH:mm:ss"
 * </pre>
 *
 * @author Hongs
 */
public class CmdletHelper
{

  //** 参数相关 **/

  /**
   * 错误消息集合
   */
  private static String[] getErrs = new String[]
  {
    "Can not parse rule '%chk'",
    "Option '%opt' is required",
    "Must have a value after the option '%opt'",
    "There is one value at most for option '%opt'",
    "Value for option '%opt' must be int",
    "Value for option '%opt' must be float",
    "Value for option '%opt' must be boolean",
    "Value for option '%opt' not matches: %mat",
    "Unrecognized option '%opt'",
    "Unupport anonymous options"
  };

  public static Map<String, Object> getOpts(String[] args, String... chks)
  {
    Map<String, Object[]>  chkz = new HashMap();
    Map<String, Object> newOpts = new HashMap();
    List<String>        newArgs = new ArrayList();
    Set<String>         reqOpts = new LinkedHashSet();
    Set<String>         errMsgs = new LinkedHashSet();
    Pattern  p = Pattern.compile("^([\\w\\.\\-\\|]*)(=|:|\\+|\\*|:\\+)([sifb]|\\/(.+)\\/(i)?( .*)?)$");
    Pattern bp = Pattern.compile("^(true|false|yes|no|y|n|1|0)$", Pattern.CASE_INSENSITIVE);
    Pattern tp = Pattern.compile("^(true|yes|y|1)$", Pattern.CASE_INSENSITIVE);
    Pattern fp = Pattern.compile("^\\d+(\\.\\d+)?$");
    Pattern ip = Pattern.compile("^\\d+$");
    boolean ub = false; // 不支持未知参数
    boolean vb = false; // 不支持匿名参数
    String hlp = null;  // 用法说明
    String pre = "\r\n\t";

    for (String chk : chks) {
      Matcher m = p.matcher(chk);
      
      if (!m.find()) {
        if (chk.equals("!U")) {
            ub  = true;
            continue;
        } else
        if (chk.equals("!A")) {
            vb  = true;
            continue;
        } else
        if (chk.startsWith("?")) {
            hlp = chk.substring(1);
            continue;
        }

        // 0号错误
        errMsgs.add(getErrs[0].replace("%chk", chk));
        continue;
      }
      
      String name = m.group(1);
      String sign = m.group(2);
      String type = m.group(3);
      
      if ("=".equals(sign) || "+".equals(sign)) {
        reqOpts.add(name);
      }
      
      if (type.startsWith("/")) {
        String  reg = m.group(4);
        String  mod = m.group(5);
        String  err = m.group(6);
        Pattern pat;
        if (mod != null) {
          pat = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        } else {
          pat = Pattern.compile(reg);
        }
        if (err != null) {
          err = err.trim();
        } else {
          err = getErrs[7];
        }
        reg = "/"+reg+"/"+mod;
        chkz.put(name, new Object[] {sign.charAt(0),'r',pat,reg,err});
      } else {
        chkz.put(name, new Object[] {sign.charAt(0), type.charAt(0)});
      }
    }
    
    F:for (int i = 0; i < args.length; i ++) {
      String name = args[i];
      
      if (name.startsWith("--")) {
        name = name.substring(2);
        
        if (chkz.containsKey(name)) {
          Object[] chk = chkz.get(name);
          char    sign = (Character)chk[0];
          char    type = (Character)chk[1];
          Pattern   rp = null;
          String   reg = null;
          String   err = null;
          Object   val = null;
          List    vals = null;
          
          if ('r' == type) {
            rp  = (Pattern)chk[3];
            reg = (String) chk[4];
            err = (String) chk[5];
          }
          
          if ('+' == sign || '*' == sign) {
            vals = (List)newOpts.get(name);
            if (vals == null) {
              vals = new ArrayList( );
              newOpts.put(name, vals);
            }
          }
          
          W:while (i < args.length-1) {
            String arg = args[i + 1];
            if (arg.startsWith("--")) {
              if (i == 0) break;
              else  continue  F;
            }
            if (arg.startsWith("\\")) {
              arg = arg.substring(1);
            }
            i ++;
            
            switch (type) {
              case 'i':
                if (!ip.matcher(arg).matches()) {
                  errMsgs.add(getErrs[4].replace("%opt", name));
                  continue W;
                }
                val = Long.parseLong(arg);
                break;
              case 'f':
                if (!fp.matcher(arg).matches()) {
                  errMsgs.add(getErrs[5].replace("%opt", name));
                  continue W;
                }
                val = Double.parseDouble(arg);
                break;
              case 'b':
                if (!bp.matcher(arg).matches()) {
                  errMsgs.add(getErrs[6].replace("%opt", name));
                  continue W;
                }
                val = tp.matcher(arg).matches();
                break;
              case 'r':
                if (!rp.matcher(arg).matches()) {
                  errMsgs.add(err.replace("%opt", name).replace("%mat", reg));
                  continue W;
                }
              default:
                val = arg;  
            }
            
            if ('+' == sign || '*' == sign) {
              vals.add(val );
            } else {
              if (newOpts.containsKey(name)) {
                errMsgs.add(getErrs[3].replace("%opt", name));
              } else {
                newOpts.put(name, val );
              }
              continue F;
            }
          }
          
          if ('b' == type) {
            if ('+'== sign || '*' == sign) {
              vals.add(true);
            } else {
              if (newOpts.containsKey(name)) {
                errMsgs.add(getErrs[3].replace("%opt", name));
              } else {
                newOpts.put(name, true);
              }
            }
          } else {
            errMsgs.add(getErrs[2].replace("%opt", name));
          }
        }
        else if (ub) {
          // 7号错误
          errMsgs.add(getErrs[8].replace("%opt", name));
        }
        else {
          newArgs.add(args[i]);
        }
      }
      else if (vb) {
        // 8号错误
        errMsgs.add(getErrs[9]);
      }
      else {
        newArgs.add(args[i]);
      }
    }

    for (String name : reqOpts) {
      if (!newOpts.containsKey(name)) {
        Set<String> err = new LinkedHashSet();
        err.add(getErrs[1].replace("%opt", name));
        err.addAll(errMsgs);
        errMsgs  =  err;
      }
    }
    
    if (!errMsgs.isEmpty()) {
      StringBuilder err = new StringBuilder();
      for ( String  msg : errMsgs ) {
        err.append(pre).append(msg);
      }
      String msg = err.toString(  ); 
      String trs = msg;
      if (null  != hlp) {
        trs += pre + hlp.replaceAll("\\n", pre);
      }
      
      HongsError er = new HongsError(0x25, msg);
                 er.setLocalizedOptions(trs);
      throw      er;
    }
    else if (null != hlp && newOpts.isEmpty( )) {
      System.err.println(hlp.replaceAll("\\n",pre));
      System.exit(0);
    }

    // 把剩余的参数放进去
    newOpts.put("", newArgs.toArray(new String[0]));

    return newOpts;
  }

  //** 输出相关 **/

  /**
   * 输出(到标准错误)
   * 通常, 辅助信息输出标准错误, 结果数据输出标准输出, 方便其他程序/进程处理
   * @param text
   */
  public static void println(String text)
  {
    CoreConfig conf = Core.getInstance(CoreConfig.class);
    String f = conf.getProperty("core.cmd.line.time.format",
                                "yyyy/MM/dd HH:mm:ss"  );
    Date   d = new Date(System.currentTimeMillis());
    String t = new SimpleDateFormat(f).format(d);
    System.err.println(t + " " + text);
  }

  /**
   * 输出到日志
   * @param text
   */
  public static void print2Log(String text) throws HongsException
  {
    CoreLogger.getInstance(Core.ACTION_NAME.get().replace('.', '_')).println(text);
  }

  /**
   * 输出和日志
   * @param text
   */
  public static void print4Log(String text) throws HongsException
  {
    CmdletHelper.println    (text);
    CmdletHelper.print2Log(text);
  }

  /**
   * 输出执行耗时
   * @param label
   * @param start
   */
  public static void printETime(String label, long start)
  {
    start = System.currentTimeMillis() - start;
    String notes = Util.humanTime(start);
    System.err.println(new StringBuilder()
                          .append(label)
                          .append( ": ")
                          .append(notes) );
  }

  /**
   * 输出执行进度
   *
   * 由于现在的终端(命令行)宽度普遍是80个字符,
   * 所以请将 notes 控制在50个字符以内(一个中文字符占两位).
   *
   * @param scale 百分比, 0~100的浮点数
   * @param notes 说明文本
   */
  public static void printERate(float scale, String notes)
  {
    if (scale < 0) scale = 0;
    if (scale > 100) scale = 100;
    if (notes == null) notes =  "" ;

    StringBuilder sb = new StringBuilder();
    Formatter     ft = new Formatter( sb );

    for (int i = 0; i < 100; i += 5)
    {
      if (scale < i + 5)
      {
        sb.append(".");
      }
      else
      {
        sb.append("!");
      }
    }

    ft.format(" %6.2f%% %s", scale, notes);

    // 清除末尾多余的字符, 并将光标返回行首
    // 每行按最少80个字符来计算
    for (int i = sb.length(); i < 79; i += 1)
    {
      sb.append(" ");
    }
    sb.append("\r" );

    if (scale == 100)
    {
      System.err.println(sb.toString());
    }
    else
    {
      System.err.print  (sb.toString());
    }
  }

  /**
   * 输出执行进度(无说明)
   * @param scale
   */
  public static void printERate(float scale)
  {
    printERate(scale, "");
  }

  /**
   * 输出执行进度(按完成量计算)
   * @param n 总条目数
   * @param ok 完成条目数
   */
  public static void printERate(int n, int ok)
  {
    String notes = String.format("ok(%d)", ok);
    float  scale = (float)ok / n * 100;
    printERate(scale, notes);
  }

  /**
   * 输出执行进度(按完成失败量计算)
   * @param n 总条目数
   * @param ok 完成条目数
   * @param err 错误条目数
   */
  public static void printERate(int n, int ok, int err)
  {
    String notes = String.format("ok(%d) error(%d)", ok, err);
    float  scale = (float)(err + ok) / n * 100;
    printERate(scale, notes);
  }

  /**
   * 输出剩余时间(按完成量计算, 此时间为估算)
   * @param t 开始时间(毫秒)
   * @param n 总条目数
   * @param ok 完成条目数
   */
  public static void printELeft(long t, int n, int ok)
  {
    float  scale = (float)ok / n * 100;
    t = System.currentTimeMillis() - t;
    float  left1 = t / scale * 100 - t;
    String left2 = Util.humanTime((long) left1);
    String left3 = String.format("ok(%d) Time left: %s",
                                    ok, left2);
    printERate(scale, left3);
  }

  /**
   * 输出剩余时间(按完成失败量计算, 此时间为估算)
   * @param t 开始时间(毫秒)
   * @param n 总条目数
   * @param ok 完成条目数
   * @param err 错误条目数
   */
  public static void printELeft(long t, int n, int ok, int err)
  {
    float  scale = (float)(err + ok) / n * 100;
    t = System.currentTimeMillis() - t;
    float  left1 = t / scale * 100 - t;
    String left2 = Util.humanTime((long) left1);
    String left3 = String.format("ok(%d) error(%d) Time left: %s",
                               ok, err, left2);
    printERate(scale, left3);
  }

  /**
   * Parses an URL query string and returns a map with the parameter values.
   * The URL query string is the part in the URL after the first '?' character up
   * to an optional '#' character. It has the format "name=value&name=value&...".
   * The map has the same structure as the one returned by
   * javax.servlet.ServletRequest.getParameterMap().
   * A parameter name may occur multiple times within the query string.
   * For each parameter name, the map contains a string array with the parameter values.
   * @param   s an URL query string.
   * @return  a map containing parameter names as keys and parameter values as map values.
   * @author  Christian d'Heureuse, Inventec Informatik AG, Switzerland, www.source-code.biz.
   */
  public static Map<String, String[]> parseQuery(String s) {
    if (s == null) {
      return new HashMap<String, String[]>(0);
    }
    // In map1 we use strings and ArrayLists to collect the parameter values.
    HashMap<String, Object> map1 = new HashMap<String, Object>();
    int p = 0;
    while (p < s.length()) {
      int p0 = p;
      while (p < s.length() && s.charAt(p) != '=' && s.charAt(p) != '&') {
        p++;
      }
      String name;
      try {
        name = s.substring(p0, p);
        name = URLDecoder.decode(name, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        throw new HongsError(0x10, ex);
      }
      if (p < s.length() && s.charAt(p) == '=') {
        p++;
      }
      p0 = p;
      while (p < s.length() && s.charAt(p) != '&') {
        p++;
      }
      String value;
      try {
        value = s.substring(p0, p);
        value = URLDecoder.decode(value, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        throw new HongsError(0x10, ex);
      }
      if (p < s.length() && s.charAt(p) == '&') {
        p++;
      }
      Object x = map1.get(name);
      if (x == null) {
        // The first value of each name is added directly as a string to the map.
        map1.put(name, value);
      } else if (x instanceof String) {
        // For multiple values, we use an ArrayList.
        ArrayList<String> a = new ArrayList<String>();
        a.add((String) x);
        a.add(value);
        map1.put(name, a);
      } else {
        @SuppressWarnings("unchecked")
        ArrayList<String> a = (ArrayList<String>) x;
        a.add(value);
      }
    }
    // Copy map1 to map2. Map2 uses string arrays to store the parameter values.
    HashMap<String, String[]> map2 = new HashMap<String, String[]>(map1.size());
    for (Map.Entry<String, Object> e : map1.entrySet()) {
      String name = e.getKey();
      Object x = e.getValue();
      String[] v;
      if (x instanceof String) {
        v = new String[]{(String) x};
      } else {
        @SuppressWarnings("unchecked")
        ArrayList<String> a = (ArrayList<String>) x;
        v = new String[a.size()];
        v = a.toArray(v);
      }
      map2.put(name, v);
    }
    return map2;
  }

}
