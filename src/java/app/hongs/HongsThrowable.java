package app.hongs;

/**
 * 异常/错误接口
 * @author Hongs
 */
public interface HongsThrowable
{

  /**
   * 获取错误代码
   * @return 错误代码
   */
  public int getCode();

  /**
   * 获取错误信息
   * @return 错误信息
   */
  public String getError();

  /**
   * 获取翻译参数
   * @return 翻译参数
   */
  public String[] getTranslate();

  /**
   * 设置翻译参数
   * @param translate
   */
  public void setTranslate(String... translate);

}
