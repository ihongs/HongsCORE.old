package app.hongs;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.regex.Pattern;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import app.hongs.action.ActionHelper;

/**
 * <h1>日志记录工具</h2>
 *
 * <h2>配置选项:</h2>
 * <pre>
 * core.log.name.date.format    日志名称中的日期格式, 为空则不按日期拆分日志
 * core.log.line.time.format    日志记录中的时间格式, 默认为"yyyy/MM/dd HH:mm:ss"
 * </pre>
 *
 * <h2>错误代码:</h2>
 * <pre>
 * 0x16 无法打开日志文件
 * 0x18 无法写入日志文件
 * </pre>
 *
 * @author Hongs
 */
public class CoreLogger
{

  /**
   * 日志路径
   */
  protected String path;

  /**
   * 日志名称
   */
  protected String name;

  /**
   * 日志扩展名
   */
  private String ext;

  /**
   * 日志写句柄
   */
  private PrintWriter out;

  /**
   * 清理的正则
   */
  private Pattern p1 = Pattern.compile("^\\s*(\\r\\n|\\r|\\n)",
                                            Pattern.MULTILINE);
  private Pattern p2 = Pattern.compile("^", Pattern.MULTILINE);

  /**
   * 以指定路径名称构造日志对象
   * @param path
   * @param name
   */
  public CoreLogger(String path, String name)
  {
    if (path == null)
    {
      path = Core.LOGS_PATH;
    }

    this.path = path;
    this.name = name;
  }

  /**
   * 以指定名称构造日志对象
   * @param name
   */
  public CoreLogger(String name)
  {
    this(null, name);
  }

  /**
   * 写日志(指定时间)
   * @param text
   * @param time
   */
  public synchronized void print(String text, long time)
  {
    if (time == 0)
    {
      time = System.currentTimeMillis();
    }
    this.check(time);

    StringBuilder sb = new StringBuilder();

    /**
     * 记录发生的时间
     */

    CoreConfig conf = (CoreConfig)Core.getInstance(app.hongs.CoreConfig.class);
    String f = conf.getProperty("core.log.line.time.format",
                                "yyyy/MM/dd HH:mm:ss");
    DateFormat df = new SimpleDateFormat(f);
    String date = df.format(new Date(time));

    sb.append('[')
      .append(date)
      .append(']');

    /**
     * 记录请求的地址
     */
    if (Core.IN_SHELL_MODE)
    {
      sb.append(' ')
        .append('[')
        .append("0.0.0.0")
        .append(' ')
        .append("0")
        .append(']');
    }
    else
    {
      ActionHelper helper = (ActionHelper)
        Core.getInstance(app.hongs.action.ActionHelper.class);
      sb.append(' ')
        .append('[')
        .append(helper.request.getRemoteAddr())
        .append(' ')
        .append(helper.request.getRemotePort())
        .append(']');
    }

    /**
     * 记录请求的动作
     */
    Core core = Core.getInstance();
    sb.append(' ')
      .append('[')
      .append(core.ACTION)
      .append(' ')
      .append(core.ID)
      .append(']');

    /**
     * 去掉空行, 行首缩进
     */
    sb.append("\r\n");
    sb.append(p2.matcher(p1.matcher(text.trim())
              .replaceAll("")).replaceAll("\t"));

    /*
    try
    {
    */
      this.out.print(sb.toString() + "\r\n");
      this.out.flush();
    /*
    }
    catch (IOException ex)
    {
      throw new HongsError(0x16, ex);
    }
    */
  }

  /**
   * 写日志(当前时间)
   * @param text
   */
  public void print(String text)
  {
    this.print(text, 0);
  }

  /**
   * 检查日志文件
   * @param time
   */
  private void check(long time)
  {
    CoreConfig conf = (CoreConfig)Core.getInstance(app.hongs.CoreConfig.class);
    String f = conf.getProperty("core.log.name.date.format", "");
    String p = this.path + File.separator + this.name;
    String x;

    /**
     * 如果有配置
     * 则检查当前扩展是否一致
     * 如果一致则退出
     * 否则检查文件
     */
    if (f.length() != 0)
    {
      DateFormat df = new SimpleDateFormat(f);
      Date d = new Date(time);
      x = df.format(d);

      if (this.out != null&&this.ext.equals(x))
      {
        return;
      }

      this.ext  = x;
      p += "." + x + ".log";
    }
    /**
     * 如果配置为空
     * 则检查日志文件是否打开
     * 已经打开则退出
     * 否则检查文件
     */
    else
    {
      if (this.out != null)
      {
        return;
      }

      p += ".log";
    }

    /** 打开文件 **/

    try
    {
      File file = new File(p);

      // 如果文件不存在则建立
      // 并将文件设为可读可写
      if (file.exists() != true)
      {
        file.createNewFile();
        file.setReadable(true, false);
        file.setWritable(true, false);
      }
      // 如果文件已打开则关闭
      if (this.out != null)
      {
        this.out.close();
      }
      // 建立文件写句柄
      this.out = new PrintWriter(
                 new OutputStreamWriter(
                 new FileOutputStream(file, true), "UTF-8"));
    }
    catch (IOException ex)
    {
      throw new HongsError(0x18, ex);
    }
  }

  @Override
  protected void finalize()
    throws Throwable
  {
    super.finalize();

    if (this.out != null)
    {
      this.out.close();
    }
  }

  /** 静态属性及方法 **/

  /**
   * 日志对象集合
   */
  public static Map<String, CoreLogger> instances = new HashMap<String, CoreLogger>();

  /**
   * 获取日志对象
   * @param name 日志名称
   * @return CoreLogger对象
   */
  private static CoreLogger getInstance(String name)
  {
    CoreLogger logs;
    if (! CoreLogger.instances.containsKey(name))
    {
      logs = new CoreLogger(name);
      CoreLogger.instances.put(name, logs);
    }
    else
    {
      logs = CoreLogger.instances.get(name);
    }
    return logs;
  }

  /**
   * 获取写入句柄
   * @param name 日志名称
   * @return PrintWriter对象
   */
  public static PrintWriter getWriter(String name)
  {
    CoreLogger logs = CoreLogger.getInstance(name);
    logs.check(System.currentTimeMillis());
    return logs.out;
  }

  /**
   * 写日志(指定时间)
   * @param name
   * @param text
   * @param time
   */
  public static void log(String name, String text, long time)
  {
    CoreLogger.getInstance(name).print(text, time);
  }

  /**
   * 写日志(当前时间)
   * @param name
   * @param text
   */
  public static void log(String name, String text)
  {
    CoreLogger.log(name, text, 0);
  }

  /**
   * 记录错误信息
   * @param text
   */
  public static void error(String text)
  {
    CoreLogger.log("error", text);
  }

  /**
   * 记录调试信息
   * @param text
   */
  public static void debug(String text)
  {
    CoreLogger.log("debug", text);
  }

}
