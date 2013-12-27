package app.hongs;

/**
 * 通用错误类
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
 * 核心: 0x10~0xFF (16~255)
 * 用户: 0x100~0xFFF (256~4095)
 * </pre>
 *
 * @author Hongs
 */
public class HongsError
  extends Error
  implements HongsThrowable
{

  private int code;

  private String error;

  private String[] translate;

  public HongsError(int code, String error, Throwable cause)
  {
    super(error, cause);

    this.code = code;
    this.error = error;

    // 检查代号是否在0x10到0xFFF之间
    if (code < 0x10 || code > 0xFFF)
    {
      throw new HongsError(0x12,
      "Error code must be from 0x100(256) to 0xFFF(4095).");
    }

    // 0x10到0x20之间的错误直接输出
    if (code > 0x10 && code < 0x20)
    {
      this.printStackTrace(System.err);
    }

    // 代码为奇数则将错误记录到日志
    if (code % 2 > 0)
    {
      this.printStackTrace(CoreLogger.getWriter("error"));
    }
  }

  public HongsError(int code, String error)
  {
    this(code, error, null);
  }

  public HongsError(int code, Throwable cause)
  {
    this(code, null, cause);
  }

  public HongsError(int code)
  {
    this(code, null, null);
  }

  @Override
  public String toString()
  {
    return "HongsError(0x"
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

    if (code >= 0x10 && code <= 0xFF)
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
