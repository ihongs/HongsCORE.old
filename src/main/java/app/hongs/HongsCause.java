package app.hongs;

/**
 * 异常基类
 * @author Hongs
 */
public interface HongsCause
{

    public int getCode();

    public String getDesc();

    public String getMessage();

    public String getLocalizedMessage();

    public String getLocalizedSection();

    public String[] getLocalizedOptions();

    public void setLocalizedSection(String lang);

    public void setLocalizedOptions(String... opts);

}
