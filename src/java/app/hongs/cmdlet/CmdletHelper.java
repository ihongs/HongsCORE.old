package app.hongs.cmdlet;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.util.Num;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

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
    "There is one value at most for option '%opt'",
    "Value for option '%opt' must be int",
    "Value for option '%opt' must be float",
    "Value for option '%opt' must be boolean",
    "Value for option '%opt' not matches: %mat",
    "Unrecognized option '%opt'",
    "Unupport anonymous options"
  };

  /**
   * 解析选项集合
   * <pre>
   * 选项格式:
   * arg0 arg1 arg2 -opt0 -opt1 value -opt2 value1 value2
   * 格式特点:
   * 以"-"开头的为选项名(后面跟数字的除外);
   * 需要选项值以"-"开头时,前面加上"\"符号;
   * 首个以"-"开头前的选项记作匿名选项("").
   * <pre>
   * @param args
   * @return 选项值
   */
  public static Map<String, String[]> getOpts(String[] args)
  {
    Map <String, String[]> optz = new HashMap();
    List<String> opt = new ArrayList();
    String n = "";

    Pattern p1 = Pattern.compile("^-([^\\d\\-][\\w\\-\\.]*)$");
    Pattern p2 = Pattern.compile("^\\\\");
    Matcher m3;

    for (int i = 0; i < args.length; i ++)
    {
      m3 = p1.matcher(  args[i]  );
      if ( m3.find() )
      {
    optz.put(n, opt.toArray(new String[0]));

        n = m3.group(1);
        opt.clear( );
        continue;
      }

      m3 = p2.matcher(  args[i]  );
      opt.add(m3.replaceFirst(""));
    }

    optz.put(n, opt.toArray(new String[0]));

    return optz;
  }

  /**
   * 获取选项集合
   * <p>
   * 规则格式为: 选项名[|选项别名1[|选项别名2]](=或:或+或*)(s或i或f或b或/正则/)<br/>
   * 最后的选项别名如果以"-"开头则表示其为纯别名, 仅用于在错误消息中显示<br/>
   * "="表示1个, ":"表示0个或1个, "+"表示1个或多个, "*"表示0个或多个<br/>
   * 本规则参考自Perl的Getopt::Long模块, 为兼容, ":+"也表示0个或多个<br/>
   * "s"表示字符串, "i"表示整数, "f"表示浮点数, "b"表示布尔值<br/>
   * 规则"!A"(不含引号)表示不支持匿名选项<br/>
   * 规则"!U"(不含引号)表示不支持未知选项<br/>
   * </p>
   * <pre>
   * 例如:
   * Map opts = getOpts(args, "name=s", "age=i", "pay=f",
   *                    "is_a_man|is-a-man=b", "notes*s",
   *                    "date=/^\\d{4}-\\d{1,2}-\\d{1,2}$/");
   * </pre>
   * @param opts 选项
   * @param chks 规则
   * @return 选项集合
   */
  public static Map<String, Object> getOpts(Map<String, String[]> opts, String... chks)
  throws HongsError
  {
    Map<String, Object> newOpts = new HashMap();
    StringBuilder       errMsgs = new StringBuilder();
    Pattern  p = Pattern.compile("^([\\w\\.\\-\\|]*)(=|:|\\+|\\*|:\\+)([sifb]|\\/(.+)\\/(i)?( .*)?)$");
    Pattern bp = Pattern.compile("^(true|false|yes|no|y|n|1|0)$", Pattern.CASE_INSENSITIVE);
    Pattern fp = Pattern.compile("^\\d+(\\.\\d+)?$");
    Pattern ip = Pattern.compile("^\\d+$");
    boolean ub = false; // 不支持未知参数
    boolean vb = false; // 不支持匿名参数
    String hlp = null;  // 帮助信息
    String pre = "\r\n\t";

    /**
     * 错误消息
     */
    String[] errs;
    if (opts.containsKey("!"))
    {
      errs = opts.get("!");
    }
    else
    {
      errs = getErrs;
    }

    F:for (String chk : chks)
    {
      Matcher m = p.matcher(chk);

      /**
       * 解析选项
       */
      if (!m.find())
      {
        /**
         * 特殊标识
         */
        if (chk.equals("!U"))
        {
            ub  = true;
            continue F;
        }
        else
        if (chk.equals("!A"))
        {
            vb  = true;
            continue F;
        }
        else
        if (chk.startsWith("?"))
        {
            hlp = chk.substring(1);
            continue F;
        }

        // 0号错误
        errMsgs.append(pre).append(errs[0].replace("%chk", chk));
        continue F;
      }

      String name = m.group(1);
      String sign = m.group(2);
      String type = m.group(3);

      String[] keys = name.split("\\|");
      String   key1 = keys[0];
      String   key2 = keys[keys.length - 1];

      String[] values = null;
      List     valuez = new ArrayList();

      /**
       * 获取并移除原始值
       */
      if (key1.startsWith("-"))
      {
        key1 = key1.substring(1);
      }
      if (key2.startsWith("-"))
      {
        key2 = key2.substring(1);
      }
      for (int i = 0; i < keys.length; i ++)
      {
        if (keys[i].startsWith("-"))
        {
          continue;
        }
        if (opts.containsKey(keys[i]))
        {
          values = opts.remove(keys[i]);
        }
      }

      /**
       * 是否是必要的
       */
      if (values == null)
      {
        if (sign.equals("=") || sign.equals("+"))
        {
          // 1号错误
          errMsgs.append(pre).append(errs[1].replace("%opt", key2));
        }
        continue F;
      }
      if (values.length < 1
      &&  ! type.equals("b"))
      {
        if (sign.equals("=") || sign.equals("+"))
        {
          // 1号错误
          errMsgs.append(pre).append(errs[1].replace("%opt", key2));
        }
        continue F;
      }

      /**
       * 是否仅限一个值
       */
      if (values.length > 1
      && (sign.equals("=") || sign.equals(":")))
      {
        // 2号错误
        errMsgs.append(pre).append(errs[2].replace("%opt", key2));
        continue F;
      }

      /**
       * 检查值类型
       */
      if (type.equals("s"))
      {
        valuez.addAll(Arrays.asList(values));
      }
      else
      if (type.equals("i"))
      {
        for (String value : values)
        {
          if (!ip.matcher(value).matches())
          {
            // 3号错误
            errMsgs.append(pre).append(errs[3].replace("%opt", key2));
            continue F;
          }
          valuez.add(Long.parseLong(value));
        }
      }
      else
      if (type.equals("f"))
      {
        for (String value : values)
        {
          if (!fp.matcher(value).matches())
          {
            // 4号错误
            errMsgs.append(pre).append(errs[4].replace("%opt", key2));
            continue F;
          }
          valuez.add(Double.parseDouble(value));
        }
      }
      else
      if (type.equals("b"))
      {
        /**
         * 布尔值只要有选项, 就表明为真
         */
        if (values.length == 0)
        {
          valuez.add(true);
        }
        else
        for (String value : values)
        {
          if (!bp.matcher(value).matches())
          {
            // 5号错误
            errMsgs.append(pre).append(errs[5].replace("%opt", key2));
            continue F;
          }
          valuez.add(value.matches("(true|yes|y|1)"));
        }
      }
      else
      {
        /**
         * 使用用户正则进行校验
         */
        String mat = m.group(4);
        String cas = m.group(5);
        String err = m.group(6);
        Pattern rp;
        if (cas != null)
        {
          rp = Pattern.compile(mat, Pattern.CASE_INSENSITIVE);
        }
        else
        {
          rp = Pattern.compile(mat);
        }
        if (err != null)
        {
          err = err.trim();
        }
        else
        {
          err = errs[6];
        }

        for (String value : values)
        {
          if (!rp.matcher(value).matches())
          {
            // 6号错误
            errMsgs.append(pre).append(err.replace("%opt", key2)
                                          .replace("%mat", mat));
            continue F;
          }
          valuez.add(value);
        }
      }

      /**
       * 登记值
       */
      if (sign.equals("=") || sign.equals(":"))
      {
        newOpts.put(key1, valuez.get(0));
      }
      else
      {
        newOpts.put(key1, valuez);
      }
    }

    /**
     * 多余的选项
     */
    for (String key : opts.keySet())
    {
      if (key.length() != 0)
      {
        if (ub)
        {
          // 7号错误
          errMsgs.append(pre).append(errs[7].replace("%opt", key));
        }
      }
      else
      {
        String[]  v = opts.get(key);
        if (vb && v.length   !=  0)
        {
          // 8号错误
          errMsgs.append(pre).append(errs[8]);
        }
        else
        {
          newOpts.put("", v);
        }
      }
    }

    if (errMsgs.length() != 0)
    {
      String msg = errMsgs.toString();
      String trs = msg;
      if (null  != hlp)
      {
        trs += pre + hlp.replaceAll("\\n", pre);
      }
      HongsError er = new HongsError(0x42, msg);
                 er.setTranslate(trs);
      throw      er;
    }

    return newOpts;
  }
  
  public static Map<String, Object> getOpts(String[] args, String... chks)
  throws HongsError
  {
    Map<String, String[]>  chkz = new HashMap();
    Map<String, Object> newOpts = new HashMap();
    List<String>        newArgs = new ArrayList();
    Set<String>         errMsgs = new LinkedHashSet();
    Pattern  p = Pattern.compile("^([\\w\\.\\-\\|]*)(=|:|\\+|\\*|:\\+)([sifb]|\\/(.+)\\/(i)?( .*)?)$");
    Pattern bp = Pattern.compile("^(true|false|yes|no|y|n|1|0)$", Pattern.CASE_INSENSITIVE);
    Pattern fp = Pattern.compile("^\\d+(\\.\\d+)?$");
    Pattern ip = Pattern.compile("^\\d+$");
    boolean ub = false; // 不支持未知参数
    boolean vb = false; // 不支持匿名参数
    String hlp = null;  // 用法说明
    String pre = "\r\n\t";

    for (String chk in chks)
    {
      Matcher m = p.matcher(chk);
      
      /**
       * 解析选项
       */
      if (!m.find())
      {
        /**
         * 特殊标识
         */
        if (chk.equals("!U"))
        {
            ub  = true;
            continue;
        }
        else
        if (chk.equals("!A"))
        {
            vb  = true;
            continue;
        }
        else
        if (chk.startsWith("?"))
        {
            hlp = chk.substring(1);
            continue;
        }

        // 0号错误
        errMsgs.append(pre).append(errs[0].replace("%chk", chk));
        continue;
      }
      
      String name = m.group(1);
      String sign = m.group(2);
      String type = m.group(3);
      String  pat = m.group(4);
      String  mod = m.group(5);
      String  err = m.group(6);
      
      if (type.startswith("/")) {
          type = "";
      }
      chkz.put(name, new String[] {sign, type, pat, mod, err});
    }
    
    for (int i = 0; i < args.length; i ++)
    {
      String name = args[i];
      
      if (arg.startswith("--")) {
        name = arg.substring(2);
        
        if (chkz.constainsKey(arg)) {
          String[] chk = chkz.get(arg);
          String  sign = chk[0];
          String  type = chk[1];
          String   pat = chk[2];
          String   mod = chk[3];
          String   err = chk[4];
          Object   val;
          List     vals;
          
          if ( "".equals(type)) {
            Pattern rp;
            if (cas != null)
            {
              rp = Pattern.compile(mat, Pattern.CASE_INSENSITIVE);
            }
            else
            {
              rp = Pattern.compile(mat);
            }
            if (err != null)
            {
              err = err.trim();
            }
            else
            {
              err = errs[6];
            }
          }
          
          if ("+".equals(sign) || "*".equals("sign")) {
            vals = newOpts.get(name);
            if (vals == null) {
              vals = new ArrayList();
              newOpts.put(name,vals);
            }
          }
          
          while (i < args.lnegth-1) {
            if (args[i+1].startswith("--")) {
              break;
            }
            
            String arg = args[i++];
            
            if ("s".equals(type)) {
              val = arg;
            } else
            if ("i".equals(type)) {
              if (!ip.matcher(arg).matches())
              {
                // 3号错误
                errMsgs.add(errs[3].replace("%opt", name));
                continue;
              }
              val = Long.parseLong(value);
            } else
            if ("f".equals(type)) {
              if (!fp.matcher(arg).matches())
              {
                // 4号错误
                errMsgs.add(errs[4].replace("%opt", name));
                continue;
              }
              val = Double.parseDouble(value);
            } else
            if ("b".equals(type)) {
              if (!bp.matcher(arg).matches())
              {
                // 5号错误
                errMsgs.add(errs[5].replace("%opt", name));
                continue;
              }
              val = value.matches("(true|yes|y|1)");
            } else {
              if (!rp.matcher(arg).matches())
              {
                // 6号错误
                errMsgs.add(err.replace("%opt", name)
                               .replace("%mat", mat));
                continue;
              }
              val = arg;
            }
            
            if ("+".equals(sign) || "*".equals("sign")) {
              vals.add(val );
            } else {
              newOpts.put(name, val );
              break;
            }
          }
          
          if ("b".equals(type)) {
            if ("+".equals(sign) || "*".equals("sign")) {
              vals.add(true);
            } else {
              newOpts.put(name, true);
            }
          } else {
            // error
          }
        }
        else if (ub) {
          // 7号错误
          errMsgs.add(errs[7].replace("%opt", name));
        }
        else {
          newArgs.add(arg);
        }
      }
      else if (vb) {
        // 8号错误
        errMsgs.add(errs[8]);
      }
      else {
        newArgs.add(arg);
      }
    }

    if (errMsgs.length() != 0)
    {
      StringBuilder err = new StringBuilder();
      String msg;
      for (msg : errMsg) {
        err.append(pre).append(msg);
      }
      msg = err.toString();
      
      String trs = msg;
      if (null  != hlp)
      {
        trs += pre + hlp.replaceAll("\\n", pre);
      }
      
      HongsError er = new HongsError(0x42, msg);
                 er.setTranslate(trs);
      throw      er;
    }

    // 把剩余的参数放进去
    if (newArgs.size() > 0) {
      newOpts.put("", newArgs.toArray(new String[0]));
    }

    return newOpts;
  }

  //** 输出相关 **/

  /**
   * 输出(到标准错误)
   * 通常, 辅助信息输出标准错误, 结果数据输出标准输出, 方便其他程序/进程处理
   * @param text
   */
  public static void print(String text)
  {
    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    String f = conf.getProperty("core.cmd.line.time.format",
                                "yyyy/MM/dd HH:mm:ss");
    Date   d = new Date(System.currentTimeMillis());
    String t = new SimpleDateFormat(f).format(d);
    System.err.println(t + " " + text);
  }

  /**
   * 输出到日志
   * @param text
   */
  public static void print2Log(String text)
  {
    CoreLogger.log(Core.ACTION_PATH.get().replace('.', '_'), text, 0);
  }

  /**
   * 输出和日志
   * @param text
   */
  public static void print4Log(String text)
  {
    CmdletHelper.print    (text);
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
    String notes = Num.humanTime(start);
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
    sb.append("\r");

    if (scale == 100)
    {
      System.err.println(sb.toString());
    }
    else
    {
      System.err.print(sb.toString());
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
    String left2 = Num.humanTime((long) left1);
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
    String left2 = Num.humanTime((long) left1);
    String left3 = String.format("ok(%d) error(%d) Time left: %s",
                               ok, err, left2);
    printERate(scale, left3);
  }

}
