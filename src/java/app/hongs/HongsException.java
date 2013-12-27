package app.hongs;

/**
 * 通用异常类
 *
 * <p>
 * 因 printStackTrace 默认调 getLocalizedMessage 输出消息,
 * 但我希望在输出对象堆栈跟踪时输出基础信息, 故:
 * getLocalizedMessage 返回的消息是开发人员的,
 * getMessage 返回的消息才是根据用户区域变化的.
 * </p>
 *
 * <h3>取值范围:</h3>
 * <pre>
 * 核心: 0x1000~0xFFFF (4096~65535)
 * 用户: 0x10000~0xFFFFF (65536~1048575)
 * </pre>
 *
 * @author Hongs
 */
public class HongsException
  extends Exception
  implements HongsThrowable
{

  private int code;

  private String error;

  private String[] translate;

  public HongsException(int code, String error, Throwable cause)
  {
    super(error, cause);

    this.code = code;
    this.error = error;

    // 检查代号是否在0x1000到0xFFFFF之间
    if (code < 0x1000 || code > 0xFFFFF)
    {
      throw new HongsError(0x14,
      "Exception code must be from 0x10000(65536) to 0xFFFFF(1048575).");
    }

    // 0x1010到0x1030之间的错误直接输出
    if (code > 0x1010 && code < 0x1030)
    {
      this.printStackTrace(System.err);
    }

    // 代码为奇数则将错误记录到日志
    if (code % 2 > 0)
    {
      this.printStackTrace(CoreLogger.getWriter("error"));
    }
  }

  public HongsException(int code, String error)
  {
    this(code, error, null);
  }

  public HongsException(int code, Throwable cause)
  {
    this(code, null, cause);
  }

  public HongsException(int code)
  {
    this(code, null, null);
  }

  @Override
  public String toString()
  {
    return "HongsException(0x"
      + Integer.toHexString(code) + "): "
      + (error  != null  ?  error  : "");
  }

  @Override
  public String getMessage()
  {
    CoreLanguage lang = new CoreLanguage("error");
    String level = "0x"+Integer.toHexString(code);
    String notes = error != null  ?  error  :  "";
    String[] trans = translate != null ? translate : new String[] {};
    String key1, key2;

    if (code >= 0x1000 && code <= 0xFFFF)
    {
      // 系统错误(异常)
      key1 = "core.error";
      key2 = "core.error." + level;
    }
    else
    {
      // 用户错误(异常)
      key1 = "user.error";
      key2 = "user.error." + level;
    }

    if (lang.containsKey(key1))
    {
      level = lang.translate(key1, level);
    }
    if (lang.containsKey(key2))
    {
      notes = lang.translate(key2, trans);
    }

    return level + ": " + notes;
  }

  @Override
  public int getCode()
  {
    return this.code;
  }

  @Override
  public String getError()
  {
    return this.error;
  }

  @Override
  public String[] getTranslate()
  {
    return this.translate;
  }

  @Override
  public void setTranslate(String... translate)
  {
    this.translate = translate;
  }

}
