package app.hongs.util;

/**
 * 数字工具
 * @author Hongs
 */
public class Num
{

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
  private final static char[] rad = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
    'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
    'U', 'V', 'W', 'X', 'Y', 'Z',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
    'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
    'u', 'v', 'w', 'x', 'y', 'z'
  };

  /** 进制 **/

  /**
   * 十进制转其他进制
   * @param num
   * @param arr
   * @param x 进制长度
   * @param o 起始位置
   * @return 指定进制的串
   */
  public static String toXRadix(long num, char[] arr, int x, int o)
  {
    StringBuilder sb = new StringBuilder();

    while (num > 0)
    {
      int idx = (int) (num % x) + o;
          num = (long)(num / x);
          sb.append(arr[idx]);
    }

    return sb.reverse().toString();
  }

  /**
   * 十进制转36进制(0~9A-Z)
   * @param num
   * @return 36进制串
   */
  public static String to36Radix(long num)
  {
    return toXRadix(num, rad, 36, 0);
  }

  /**
   * 十进制转62进制(0~9A-Za-z)
   * @param num
   * @return 62进制串
   */
  public static String to62Radix(long num)
  {
    return toXRadix(num, rad, 62, 0);
  }

  /**
   * 十进制转字母形式(A-Za-z)
   * @param num
   * @return 52进制串
   */
  public static String toCSRadix(long num)
  {
    return toXRadix(num, rad, 52, 10);
  }

  /**
   * 十进制转大写字母形式(A-Z)
   * @param num
   * @return 26进制串
   */
  public static String toUCRadix(long num)
  {
    return toXRadix(num, rad, 26, 10);
  }

  /**
   * 十进制转小写字母形式(a-z)
   * @param num
   * @return 26进制串
   */
  public static String toLCRadix(long num)
  {
    return toXRadix(num, rad, 26, 36);
  }

  /** 格式 **/

  /**
   * 友好的时间格式
   * @param time 毫秒数
   * @return
   */
  public static String humanTime(long time)
  {
    int d = (int) Math.floor(time / 86400000);
    time = time % 86400000;
    int h = (int) Math.floor(time / 3600000);
    time = time % 3600000;
    int m = (int) Math.floor(time / 60000);
    time = time % 60000;
    float s = time/1000;

    StringBuilder sb = new StringBuilder();
    if (d > 0) sb.append(d).append(" D ");
    if (h > 0) sb.append(h).append(" H ");
    if (m > 0) sb.append(m).append(" M ");
    if (s > 0) sb.append(s).append(" S ");
    if (sb.length() < 1) sb.append("0 S");

    return sb.toString().trim();
  }

  /**
   * 友好的容量格式
   * @param size 字节数
   * @return
   */
  public static String humanSize(long size)
  {
    int g = (int) Math.floor(size / 1073741824);
    size = size % 1073741824;
    int m = (int) Math.floor(size / 1048576);
    size = size % 1048576;
    int k = (int) Math.floor(size / 1024);
    size = size % 1024;
    int b = (int) size;

    StringBuilder sb = new StringBuilder();
    if (g > 0) sb.append(g).append(" G ");
    if (m > 0) sb.append(m).append(" M ");
    if (k > 0) sb.append(k).append(" K ");
    if (b > 0) sb.append(b).append(" B ");
    if (sb.length() < 1) sb.append("0 B");

    return sb.toString().trim();
  }

}
