package app.hongs;

import app.hongs.action.ActionHelper;
import app.hongs.cmdlet.CmdletHelper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志记录工具
 *
 * <h3>配置选项:</h3>
 * <pre>
 * core.log.name.date.format    日志名称中的日期格式, 为空则不按日期拆分日志
 * core.log.line.time.format    日志记录中的时间格式, 默认为"yyyy/MM/dd HH:mm:ss"
 * </pre>
 *
 * <h3>异常代码:</h3>
 * <pre>
 * 0x1002   无法写入日志文件
 * </pre>
 *
 * @author Hongs
 */
public class CoreLogger implements Core.Destroy
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
  public synchronized void println(String text, long time)
  {
    if (time == 0)
    {
      time = System.currentTimeMillis();
    }
    this.openlog(time);

    StringBuilder sb = new StringBuilder();

    /**
     * 记录发生的时间
     */

    CoreConfig conf = Core.getInstance(CoreConfig.class);
    String f = conf.getProperty("core.log.line.time.format",
                                "yyyy/MM/dd HH:mm:ss"  );
    DateFormat df = new SimpleDateFormat(f);
    String date = df.format(new Date(time));

    sb.append('[')
      .append(date)
      .append(']');

    /**
     * 记录请求的地址
     */
    if (Core.ENVIR == 1)
    {
      Core core = Core.getInstance();
      if (core.containsKey(app.hongs.action.ActionHelper.class.getName()))
      {
        ActionHelper helper = Core.getInstance(app.hongs.action.ActionHelper.class);
        if (null != helper.getRequest()) {
          sb.append(' ')
            .append('[')
            .append(helper.getRequest().getRemoteAddr())
            .append(' ')
            .append(helper.getRequest().getRemotePort())
            .append(']');
        }
        else
        {
          sb.append(" [IN ACTION]");
        }
      }
      else
      {
        sb.append(" [IN ACTION]");
      }
    }
    else
    {
      sb.append(" [IN CMDLET]");
    }

    /**
     * 记录请求的动作
     */
    sb.append(' ')
      .append('[')
      .append(Core.ACTION_NAME.get())
      .append(' ')
      .append(Thread.currentThread().getId())
      .append(']')
      .append(' ');

//    /**
//     * 去掉空行, 行首缩进
//     */
//    sb.append("\r\n");
//    sb.append(Text.indent(Text.clearEL(text.trim())));
//    sb.append("\r\n");

    sb.append(text.trim());
    this.out.println( sb );
    this.out.flush();
  }

  /**
   * 写日志(当前时间)
   * @param text
   */
  public void println(String text)
  {
    this.println(text, 0);
  }

  /**
   * 检查日志文件
   * @param time
   */
  private void openlog(long time)
  {
    CoreConfig conf = Core.getInstance(CoreConfig.class);
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
      x = new SimpleDateFormat(f).format(new Date(time));
      if (this.out != null && this.ext.equals(x))
      {
        return;
      }

      this.ext = x ;
      p += x+".log";
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
      if (!file.exists())
      {
        File dn = file.getParentFile();
        if (!dn.exists())
        {
             dn.mkdirs();
        }
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
      throw new HongsError(0x2f, ex);
    }
  }

  @Override
  public void destroy( )
    throws Throwable
  {
    if (this.out != null)
    {
        this.out.close( );
    }
  }

  @Override
  protected void finalize()
    throws Throwable
  {
    this .destroy( );
    super.finalize();
  }

  //** 静态属性及方法 **/

  /**
   * 获取日志对象
   * @param name 日志名称
   * @return CoreLogger对象
   */
  public static CoreLogger getInstance(String name)
  {
      String key = CoreLogger.class.getName() + ":" + name;
      Core core = Core.getInstance( );
      CoreLogger inst;
      if (core.containsKey(key)) {
          inst = (CoreLogger)core.get(key);
      }
      else {
          inst = new CoreLogger(name);
          core.put( key, inst );
      }
      return inst;
  }

  /**
   * 记录调试信息
   * @param text
   */
  public static void debug(String text)
  {
    if (4 == (4 & Core.DEBUG)) {
        return; // 禁止了调试
    }
    if (1 == (1 & Core.DEBUG)) {
        CmdletHelper.println(text);
    }
    if (2 == (2 & Core.DEBUG)) {
        try {
            CoreLogger.getInstance("debug").println(text, 0);
        } catch (HongsError e) {
            if (1 != (1 & Core.DEBUG)) {
                CmdletHelper.println(text);
            }
            System.err.println("ERROR: Write to debug log failed!");
        }
    }
  }

  /**
   * 记录错误信息
   * @param text
   */
  public static void error(String text)
  {
    if (8 == (8 & Core.DEBUG)) {
        return; // 禁止了错误
    }
    if (1 == (1 & Core.DEBUG)) {
        CmdletHelper.println(text);
    }
    if (2 == (2 & Core.DEBUG)) {
        try {
            CoreLogger.getInstance("error").println(text, 0);
        } catch (HongsError e) {
            if (1 != (1 & Core.DEBUG)) {
                CmdletHelper.println(text);
            }
            System.err.println("ERROR: Write to error log failed!");
        }
    }
  }

  /**
   * 记录异常信息
   * @param t
   */
  public static void error(Throwable t)
  {
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    t.printStackTrace(new PrintStream(b));
    error( b.toString() );
  }

}
