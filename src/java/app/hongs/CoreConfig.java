package app.hongs;

import java.util.Properties;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * 配置信息读取工具
 *
 * <p>
 * 采用Properties加载配置选项.<br/>
 * 资源文件名为"xxx.properties".<br/>
 * </p>
 *
 * <h3>配置选项:</h3>
 * <pre>
 * core.load.config.once    为true则仅加载一次, 为false由Core控制
 * </pre>
 *
 * <h3>错误代码:</h3>
 * <pre>
 * 0x21 无法找到配置文件
 * 0x23 无法读取配置文件
 * </pre>
 *
 * @author Hongs
 */
public class CoreConfig
  extends Properties
{

  public String path;

  /**
   * 加载指定路径和名称的配置
   * @param path
   * @param name
   */
  public CoreConfig(String path, String name)
  {
    super();

    this.path = path;

    if (name != null)
    {
      this.load(name);
    }
  }

  /**
   * 加载指定名称的配置
   * @param name
   */
  public CoreConfig(String name)
  {
    this(Core.CONF_PATH, name);
  }

  /**
   * 加载默认配置
   */
  public CoreConfig()
  {
    this(Core.CONF_PATH, "default");
  }

  /**
   * 根据配置名称加载配置
   * @param name
   */
  public void load(String name)
  {
    String file = this.path + File.separator + name + ".properties";

    try
    {
      this.load(new FileInputStream(file));
    }
    catch (FileNotFoundException ex)
    {
      throw new app.hongs.HongsError(0x21, "Can not find the properties file '" + file + "'.");
    }
    catch (IOException ex)
    {
      throw new app.hongs.HongsError(0x23, "Can not read the properties file '" + file + "'.");
    }
  }

  /**
   * 根据配置名称加载配置(XML格式)
   * @param name
   */
  public void loadFromXML(String name)
  {
    String file = this.path + File.separator + name + ".xml";

    try
    {
      this.loadFromXML(new FileInputStream(file));
    }
    catch (FileNotFoundException ex)
    {
      throw new app.hongs.HongsError(0x21, "Can not find the xml properties file '" + file + "'.");
    }
    catch (IOException ex)
    {
      throw new app.hongs.HongsError(0x23, "Can not read the xml properties file '" + file + "'.");
    }
  }

  /**
   * 获取配置属性的数字形式
   * @param key
   * @param def
   * @return 数字类型属性
   */
  public int getProperty(String key, int def)
  {
    String value = this.getProperty(key, "");
    if (value.length() != 0)
    {
      return Integer.parseInt(value);
    }
    else
    {
      return def;
    }
  }

  /**
   * 获取配置属性的数字形式
   * @param key
   * @param def
   * @return 数字类型属性
   */
  public long getProperty(String key, long def)
  {
    String value = this.getProperty(key, "");
    if (value.length() != 0)
    {
      return Long.parseLong(value);
    }
    else
    {
      return def;
    }
  }

  /**
   * 获取配置属性的数字形式
   * @param key
   * @param def
   * @return 数字类型属性
   */
  public float getProperty(String key, float def)
  {
    String value = this.getProperty(key, "");
    if (value.length() != 0)
    {
      return Float.parseFloat(value);
    }
    else
    {
      return def;
    }
  }

  /**
   * 获取配置属性的数字形式
   * @param key
   * @param def
   * @return 数字类型属性
   */
  public double getProperty(String key, double def)
  {
    String value = this.getProperty(key, "");
    if (value.length() != 0)
    {
      return Double.parseDouble(value);
    }
    else
    {
      return def;
    }
  }

  /**
   * 获取配置属性的布尔形式
   * @param key
   * @param def
   * @return 布尔类型属性
   */
  public boolean getProperty(String key, boolean def)
  {
    String value = this.getProperty(key, "");
    if (value.length() != 0)
    {
      return Boolean.parseBoolean(value);
    }
    else
    {
      return def;
    }
  }

  //** 静态属性及方法 **/

  public static CoreConfig instance;

  /**
   * 获取唯一实例
   * 如果配置为core.load.config.once为true则仅加载一次
   * @return 唯一配置实例
   */
  public static CoreConfig getInstance()
  {
    if (CoreConfig.instance != null)
    {
      return CoreConfig.instance;
    }

    CoreConfig conf = new CoreConfig();

    if (conf.getProperty("core.load.config.once", false))
    {
      CoreConfig.instance = conf;
    }

    return conf;
  }

  /**
   * 按配置名获取唯一实例
   * 如果配置为core.load.config.once为true则仅加载一次
   * @param name 配置名
   * @return 唯一配置实例
   */
  public static CoreConfig getInstance(String name)
  {
    String key = "__CONF__." + name;
    Core  core = Core.getInstance();
    if (! core.containsKey( key ) )
    {
      CoreConfig conf = new CoreConfig(name);
      core.put(key, conf); return conf;
    }
    else
    {
      return (CoreConfig)core.get(key);
    }
  }
}
