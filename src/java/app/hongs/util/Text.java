package app.hongs.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <h1>文本操作工具</h1>
 * <pre>
 * 用于引用/编码或清理文本, 转换数值进制等.
 * </pre>
 *
 * @author Hongs
 */
public final class Text
{

  /** 引用 **/

  /**
   * 转义正则符
   * jdk1.5 中 Pattern.escape(esc) 替换有问题
   * 故对正则符等转换成Unicode形式来使之正常
   * @param str 原字符串
   * @return 转换了的字符串
   */
  private static String escapeRegular(String str)
  {
    StringBuilder sb = new StringBuilder();
         char[]   cs = str.toCharArray();
    for (char c : cs)
    {
      sb.append(String.format("\\u%04x", (int)c));
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
  private static String escapeReplace(String str)
  {
    return str.replaceAll("(\\\\|\\$)", "\\\\$1");
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
                          "$1"+Text.escapeReplace(sym)+"$3");
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

  /** 替换 **/

  private static Pattern assignPattern = Pattern.compile("\\$((\\w+)|\\{(.*?)\\})");

  public static String assign(String str, Map<String, String> vars) {
      Matcher matcher = assignPattern.matcher(str);
      StringBuffer sb = new StringBuffer();

      while (matcher.find()) {
          String st = matcher.group(2);
          if (null == st) {
                 st = matcher.group(3);
          }
          if (vars.containsKey(st)) {
              st = vars.get(st);
          } else {
              st = "";
          }

          st = Matcher.quoteReplacement(st);
          matcher.appendReplacement(sb, st);
      }
      matcher.appendTail(sb);

      return sb.toString(  );
  }

  public static String assign(String str, List<String> vars)
  {
    /**
     * 将语句中替换$n或${n}为指定的文字, n从0开始
     */
      Map<String, String> rep2 = new HashMap();
      for (int i = 0; i < vars.size(); i ++) {
          rep2.put(String.valueOf(i), vars.get(i));
      }

      return assign(str, rep2);
  }

  public static String assign(String str, String... vars)
  {
    /**
     * 将语句中替换$n或${n}为指定的文字, n从0开始
     */
      Map<String, String> rep2 = new HashMap();
      for (int i = 0; i < vars.length; i ++) {
          rep2.put(String.valueOf(i), vars[i]);
      }

      return assign(str, rep2);
  }

  /** 清理 **/

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

}
