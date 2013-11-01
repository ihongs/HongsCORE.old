package app.hongs.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 数字工具
 * @author Hongs
 */
public class Num
{

  /** 进制 **/

  /**
   * 符号转换表
   * <pre>
   * 所用字符集: 0-9A-Za-z
   * 随想:
   * 这拉丁字符实在太乱了, 字符高矮不齐, 单词长短不一...
   * 对于一个中国人, 早已习惯中文的整齐, 看着确有些别扭.
   * 不过发现了16进制的一个特点, 从a到f, 偶数矮, 奇数高.
   * 正所谓: 阳中有阴, 阴中有阳, 呵呵^________________^
   * </pre>
   */
  private final static char[] arr36 = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
    'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
    'U', 'V', 'W', 'X', 'Y', 'Z'
  };
  private final static char[] arr26 = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
    'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
    'U', 'V', 'W', 'X', 'Y', 'Z'
  };

  /**
   * 十进制转其他进制
   * 进制为arr的长度, 左边不能是arr首位, 如36进制(数字加字母), 35是Z, 36是10
   * @param num 待转数字 0~MAX
   * @param arr 转换序列
   * @return
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
   * @return
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
    return toXHex(num, arr36);
  }

  /**
   * 十进制转26进制(A-Z)
   * @param num 1~MAX
   * @return 26进制串
   */
  public static String to26Hex(long num)
  {
    return toYHex(num, arr26);
  }

  public static long fromXHex(String str, char[] arr) {
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
          num += Math.pow(y-i+1, x) * map.get(chs[i]);
      }
      return num;
  }

  public static long fromYHex(String str, char[] arr) {
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

  public static long from36Hex(String str) {
      return fromXHex(str, arr36);
  }

  public static long from26Hex(String str) {
      return fromYHex(str, arr26);
  }

  /** 格式 **/

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
      sb.append(n).append("D");
    }
    n = (int) Math.floor(time / 3600000);
    if (n > 0) {  time = time % 3600000;
      sb.append(n).append("H");
    }
    n = (int) Math.floor(time / 60000);
    if (n > 0) {  time = time % 60000;
      sb.append(n).append("M");
    }
    n = (int) Math.floor(time / 1000);
    if (n > 0) {
      sb.append(n).append("S");
    }

    if (sb.length() < 1) {
        sb.append( "0S");
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
    if (n > 0) {
      sb.append(n);
    }

    if (sb.length() < 1) {
        sb.append( "0" );
    }

    return sb.toString();
  }

}
