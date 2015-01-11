package app.hongs.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 常用工具
 *
 * <p>
 * 用于引用/编码或清理文本, 转换数值进制等.
 * </p>
 *
 * @author Hongs
 */
public final class Text
{

  //** 进制 **/

  /**
   * 符号转换表
   * <pre>
   * 所用字符集: 0-9A-Za-z
   * 随想:
   * 这拉丁字符实在太乱了, 字符高矮不齐, 单词长短不一;
   * 对于一个中国人, 早已习惯中文的整齐, 看着有些别扭;
   * 不过发现了16进制的一个特点, 从a到f, 偶数矮奇数高;
   * 正所谓: 阳中有阴, 阴中有阳, 呵呵^________________^
   * </pre>
   */
  private final static char[] _36Hex = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
    'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
    'U', 'V', 'W', 'X', 'Y', 'Z'
  };
  private final static char[] _26Hex = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
    'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
    'U', 'V', 'W', 'X', 'Y', 'Z'
  };

  /**
   * 十进制转其他进制
   * 进制为arr的长度, 左边不能是arr首位, 如36进制(数字加字母), 35是Z, 36是10
   * @param num 待转数字 0~MAX
   * @param arr 转换序列
   * @return 指定进制的数字字符串
   */
  public static String toXHex(long num, char[] arr)
  {
    StringBuilder str = new StringBuilder();
    int x = arr.length;
    int i;

    if   (num == 0)
          str.insert(0, arr[0]);
    while(num >= 1)
    {
          i   = (int) ( num%x );
          num = (long)( num/x );
          str.insert(0, arr[i]);
    }

    return str.toString();
  }

  /**
   * 十进制转其他进制
   * 进制为arr的长度, 左边可以是arr首位, 如26进制(字母), 1是A, 26是Z, 27是AA
   * @param num 待转数字 1~MAX
   * @param arr 转换序列
   * @return 指定进制的数字字符串
   */
  public static String toYHex(long num, char[] arr)
  {
    StringBuilder str = new StringBuilder();
    int x = arr.length;
    int i;

    while(num >= 1)
    {     num -= 1;
          i   = (int) ( num%x );
          num = (long)( num/x );
          str.insert(0, arr[i]);
    }

    return str.toString();
  }

  /**
   * 十进制转36进制(0~Z)
   * @param num 0~MAX
   * @return 36进制串
   */
  public static String to36Hex(long num)
  {
    return Text.toXHex(num, _36Hex);
  }

  /**
   * 十进制转26进制(A-Z)
   * @param num 1~MAX
   * @return 26进制串
   */
  public static String to26Hex(long num)
  {
    return Text.toYHex(num, _26Hex);
  }

  public static long asXHex(String str, char[] arr) {
      Map<Character, Integer> map = new HashMap();
      char[] chs = str.toCharArray();
      long   num = 0;
      int x = arr.length;
      int y = chs.length;
      int i;
      for (i = 0; i < x; i ++) {
          map.put( arr[i], i );
      }
      for (i = 0; i < y; i ++) {
          num += Math.pow(x, y-i-1) * map.get(chs[i]);
      }
      return num;
  }

  public static long asYHex(String str, char[] arr) {
      Map<Character, Integer> map = new HashMap();
      char[] chs = str.toCharArray();
      long   num = 0;
      int x = arr.length;
      int y = chs.length;
      int i;
      for (i = 0; i < x; i ++) {
          map.put( arr[i], i );
      }
      for (i = 0; i < y; i ++) {
          num += Math.pow(y-i+1, x) * (map.get(chs[i])+1);
      }
      return num;
  }

  public static long as36Hex(String str) {
      return asXHex(str, _36Hex);
  }

  public static long as26Hex(String str) {
      return asYHex(str, _26Hex);
  }

  //** 转义 **/

  /**
   * 转义正则符
   * jdk1.5 中 Pattern.escape(esc) 替换有问题
   * 故对正则符等转换成Unicode形式来使之正常
   * @param str 原字符串
   * @return 转换了的字符串
   */
  public static String escapeRegular(String str)
  {
    StringBuilder sb = new StringBuilder();
         char[]   cs = str.toCharArray();
    for (char c : cs)
    {
      sb.append(String.format("\\u%04x",(int)c));
    }
    return sb.toString();
  }

  /**
   * 转义转义符
   * jdk1.5 中缺少 Matcher.quoteReplacement
   * 故对转义符等前面加斜杠进行转义使之正常
   * @param str 原字符串
   * @return 转换了的字符串
   */
  public static String escapeReplace(String str)
  {
    return str.replaceAll("(\\\\|\\$)","\\\\$1");
  }

  /**
   * 引用(转义指定字符)
   * @param str 待引用源字符串
   * @param esc 被转义字符
   * @param sym 转义字符
   * @return 已转义特定字符
   */
  public static String escape(String str, String esc, String sym)
  {
    /**
     * 转换转义符里的特殊字符,
     * 偶数个转义符后加转义符.
     */

    if (esc.indexOf(sym) == -1)
        esc    +=   sym;

    String esc2 = Text.escapeRegular(esc);
    String sym2 = Text.escapeReplace(sym);

    return str.replaceAll("(["+esc2+"])", sym2+"$1");
    /*
    return str.replaceAll("(("+sym2+sym2+")+)?(["+esc2+"])",
                          "$1"+Util.escapeReplace(sym)+"$3");
    */
  }
  public static String escape(String str, String esc)
  {
    return Text.escape(str, esc, "\\");
  }
  public static String escape(String str)
  {
    return Text.escape(str, "'\"", "\\");
  }

  /**
   * 反引用(反转义指定字符)
   * @param str 待反引用源字符串
   * @param esc 被转义字符
   * @param sym 转义字符
   * @return 已反转义特定字符
   */
  public static String resume(String str, String esc, String sym)
  {
    /**
     * 把两个转义符换成一个,
     * 把单个转义符全删除掉.
     */

    if (esc.indexOf(sym) == -1)
        esc   +=    sym;

    String esc2 = Text.escapeRegular(esc);
    String sym2 = Text.escapeRegular(sym);

    return str.replaceAll(sym2 + "(["+esc2+"])", "$1");
  }
  public static String resume(String str, String esc)
  {
    return Text.resume(str, esc, "\\");
  }
  public static String resume(String str)
  {
    return Text.resume(str, "'\"", "\\");
  }

  //** 注入 **/

  // 偶数个转义符$单词或{文本}
  private static Pattern injectPattern = Pattern.compile("((?:[\\\\][\\\\])*)\\$(?:(\\w+)|\\{(.*?)\\})");

  /**
   * 注入参数
   * @param str
   * @param vars
   * @return 注入后的文本
   */
  public static String inject(String str, Map<String, String> vars) {
      Matcher matcher = injectPattern.matcher(str);
      StringBuffer sb = new StringBuffer();
      String       st;

      while (matcher.find()) {
          st = matcher.group(2);
          if (st == null) {
              st = matcher.group(3);
          }
          if (vars.containsKey(st)) {
              st = vars.get(st);
          } else {
              st = "";
          }
          st = matcher.group(1) + st;
          st = Matcher.quoteReplacement(st);
          matcher.appendReplacement(sb, st);
      }
      matcher.appendTail(sb);

      return sb.toString(  );
  }

  /**
   * 注入参数
   * @param str
   * @param vars
   * @return 注入后的文本
   */
  public static String inject(String str, List<String> vars)
  {
    /**
     * 将语句中替换$n或${n}为指定的文字, n从0开始
     */
      Map<String, String> rep2 = new HashMap();
      for (int i = 0; i < vars.size(); i ++) {
          rep2.put(String.valueOf(i), vars.get(i));
      }

      return inject(str, rep2);
  }

  /**
   * 注入参数
   * @param str
   * @param vars
   * @return  注入后的文本
   */
  public static String inject(String str, String... vars)
  {
    /**
     * 将语句中替换$n或${n}为指定的文字, n从0开始
     */
      Map<String, String> rep2 = new HashMap();
      for (int i = 0; i < vars.length; i ++) {
          rep2.put(String.valueOf(i), vars[i]);
      }

      return inject(str, rep2);
  }

  //** 缩进 **/

  /**
   * 行首缩进
   * @param str
   * @param inds
   * @return 缩进后的文本
   */
  public static String indent(String str, String inds) {
      return Pattern.compile("^", Pattern.MULTILINE)
                    .matcher(str).replaceAll(inds);
  }

  /**
   * 行首缩进
   * @param str
   * @return 缩进后的文本
   */
  public static String indent(String str) {
      return indent(str, "\t");
  }

  //** 清理 **/

  /**
   * 清除换行
   * @param str
   * @return 新串
   */
  public static String clearNL(String str)
  {
    return Pattern.compile("(\\r\\n|\\r|\\n)", Pattern.MULTILINE)
                  .matcher(str).replaceAll("");
  }

  /**
   * 清除空行
   * @param str
   * @return 新串
   */
  public static String clearEL(String str)
  {
    return Pattern.compile("^\\s*(\\r\\n|\\r|\\n)", Pattern.MULTILINE)
                  .matcher(str).replaceAll("");
  }

  /**
   * 清理空白
   * @param str
   * @return 新串
   */
  public static String clearSC(String str)
  {
    return Pattern.compile("[ \\t\\v\\f\\x0B\\u3000]+", Pattern.MULTILINE)
                  .matcher(str).replaceAll(" ");
  }

  /**
   * 清理XML
   * 该实现并不严谨, 对于复杂的XML(包含CDATA等)不推荐使用
   * @param str
   * @return 新串
   */
  public static String cleanXML(String str)
  {
    Pattern pat;
    pat = Pattern.compile("<[^>]*?>", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
    str = pat.matcher(str).replaceAll(" ");
    pat = Pattern.compile("&[^&;]*;", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
    str = pat.matcher(str).replaceAll(" ");
    return  str;
  }

  /**
   * 清理HTML
   * 该实现并不严谨, 对于复杂的HTML(包含JS等)不推荐使用
   */
  public static String cleanHTML(String str)
  {
    Pattern pat;
    pat = Pattern.compile( "<style.*?>.*?</style>" , Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
    str = pat.matcher(str).replaceAll("");
    pat = Pattern.compile("<script.*?>.*?</script>", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
    str = pat.matcher(str).replaceAll("");
    return   cleanXML(str);
  }

  //** 格式 **/

  /**
   * 友好的时间格式(精确到秒)
   * @param time 毫秒数
   * @return 最大到天, 最小到秒
   */
  public static String humanTime(long time)
  {
    StringBuilder sb = new StringBuilder( );
    int n;

    n = (int) Math.floor(time / 86400000);
    if (n > 0) {  time = time % 86400000;
      sb.append(n).append("d");
    }
    n = (int) Math.floor(time / 3600000);
    if (n > 0) {  time = time % 3600000;
      sb.append(n).append("h");
    }
    n = (int) Math.floor(time / 60000);
    if (n > 0) {  time = time % 60000;
      sb.append(n).append("m");
    }
    
    float m = (float) time / 1000;
    if (0 != m || 0 == sb.length()) {
      sb.append(m).append("s");
    }

    return sb.toString();
  }

  /**
   * 友好的容量格式
   * @param size 容量数
   * @return 最大到T 注意: 不带单位, B或b等请自行补充
   */
  public static String humanSize(long size)
  {
    StringBuilder sb = new StringBuilder( );
    int n;

    n = (int) Math.floor(size / 1099511627776L);
    if (n > 0) {  size = size % 1099511627776L;
      sb.append(n).append("T");
    }
    n = (int) Math.floor(size / 1073741824);
    if (n > 0) {  size = size % 1073741824;
      sb.append(n).append("G");
    }
    n = (int) Math.floor(size / 1048576);
    if (n > 0) {  size = size % 1048576;
      sb.append(n).append("M");
    }
    n = (int) Math.floor(size / 1024);
    if (n > 0) {  size = size % 1024;
      sb.append(n).append("K");
    }

    n = (int) size;
    if (0 != n || 0 == sb.length()) {
      sb.append(n);
    }

    return sb.toString();
  }

}
