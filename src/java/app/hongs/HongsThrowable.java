package app.hongs;

/**
 * 异常/错误接口
 * @author Hongs
 */
public interface HongsThrowable
{

  /**
   * 获取错误代码
   * @return
   */
  public int getCode();

  /**
   * 获取错误信息
   * @return
   */
  public String getError();

  /**
   * 获取翻译参数
   * @return
   */
  public String[] getTranslate();

  /**
   * 设置翻译参数
   * @param translate
   */
  public void setTranslate(String... translate);

}
