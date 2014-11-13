package app.hongs;

import app.hongs.util.Text;

import java.util.List;
import java.util.Map;
import java.io.File;

/**
 * 语言资源读取工具
 *
 * <p>
 * 为与配置保持一致, 故从CoreConfig继承.<br/>
 * 放弃使用"ResourceBundle"类加载语言资源.<br/>
 * 资源文件名为"xxx.语言[-国家].properties".<br/>
 * </p>
 *
 * <h3>配置选项:</h3>
 * <pre>
 * core.load.language.once  为true则仅加载一次, 为false由Core控制
 * core.language.link.xx    语言链接, xx为语言, 如: link.zh=zh-cn
 * </pre>
 *
 * @author Hongs
 */
public class CoreLanguage
  extends CoreConfig
{

  public String lang;

  /**
   * 加载指定路径\语言和名称的配置
   * @param path
   * @param name
   * @param lang
   */
  public CoreLanguage(String path, String name, String lang)
  {
    super(path, null);

    this.lang = lang ;

    if (name != null)
    {
      this.load(name);
    }
  }

  /**
   * 加载指定语言和名称的配置
   * @param name
   * @param lang
   */
  public CoreLanguage(String name, String lang)
  {
    this(Core.CONF_PATH, name, lang);
  }

  /**
   * 加载指定名称的配置
   * @param name
   */
  public CoreLanguage(String name)
  {
    this(Core.CONF_PATH, name, Core.ACTION_LANG.get());
  }

  /**
   * 加载默认配置
   */
  public CoreLanguage()
  {
    this(Core.CONF_PATH, "default", Core.ACTION_LANG.get());
  }

  /**
   * 加载指定属性文件
   * @param name
   */
  @Override
  public void load(String name)
  {
    super.load(name + "." + this.lang);
  }

  /**
   * 加载指定属性文件(XML格式)
   * @param name
   */
  @Override
  public void loadFromXML(String name)
  {
    super.loadFromXML(name + "." + this.lang);
  }

  /**
   * 翻译指定键对应的语句
   * @param key
   * @return 翻译后的语句
   */
  public String translate(String key)
  {
    String str  = this.getProperty(key, null);
    return str != null  ?  str  :  key;
  }

  /**
   * 翻译指定键对应的语句并替换参数
   * 参数名为$xxx或${xxx}($one,${two}...)
   * @param key
   * @param rep
   * @return 翻译后的语句, 会替换特定标识
   */
  public String translate(String key, Map<String, String> rep)
  {
    String str = this.translate(key);
    if (  rep == null  ) return str ;

    /**
     * 将语句中的$xxx或${xxx}替换成指定文字
     * 如果指定的替换文字不存在, 则替换为空
     */
    return Text.inject(str, rep);
  }

  /**
   * 翻译指定键对应的语句并替换参数
   * 参数名为$n($0,$1...)
   * @param key
   * @param rep
   * @return 翻译后的语言, 会替换特定标识
   */
  public String translate(String key, List<String> rep)
  {
    String str = this.translate(key);
    if (  rep == null  ) return str ;

    /**
     * 将语句中替换$n或${n}为指定的文字, n从0开始
     */
    return Text.inject(str, rep);
  }

  /**
   * 翻译指定键对应的语句并替换参数
   * 参数名为$n($0,$1...)
   * @param key
   * @param rep
   * @return 翻译后的语句, 会替换特定标识
   */
  public String translate(String key, String... rep)
  {
    String str = this.translate(key);
    if (  rep == null  ) return str ;

    /**
     * 将语句中替换$n或${n}为指定的文字, n从0开始
     */
    return Text.inject(str, rep);
  }

  //** 静态属性及方法 **/

  /**
   * 获取唯一语言对象
   * 如果配置core.load.language.once为true则仅加载一次
   * @return 唯一语言实例
   */
  public static CoreLanguage getInstance()
  {
    return getInstance("default");
  }

  /**
   * 按配置名获取唯一语言对象
   * 如果配置core.load.language.once为true则仅加载一次
   * @param name 配置名
   * @return 唯一语言实例
   */
  public static CoreLanguage getInstance(String name)
  {
    String ck = CoreLanguage.class.getName() + ":" + name + "." + Core.ACTION_LANG.get();

    Core core = Core.getInstance();
    if (core.containsKey(ck))
    {
      return (CoreLanguage)core.get(ck);
    }

    Core gore = Core.GLOBAL_CORE;
    if (gore.containsKey(ck))
    {
      return (CoreLanguage)gore.get(ck);
    }

    CoreLanguage lang = new CoreLanguage(name);
    CoreConfig conf = CoreConfig.getInstance();
    if (conf.getProperty("core.load.language.once", false))
    {
      gore.put(ck, lang);
    }
    else
    {
      core.put(ck, lang);
    }

    return lang;
  }

  /**
   * 从HEAD串中获取支持的语言
   * @param lang
   * @return 语言标识, 如zh,zh-cn(全小写), 不存在为null
   */
  public static String getAcceptLanguage(String lang)
  {
    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    String[]   arr1 = lang.toLowerCase().split(",");
    String[]   arr2;

    for (int i = 0; i < arr1.length; i ++)
    {
      arr2 = arr1[i].split(";" , 2);

      lang = arr2[0];
      if (CoreLanguage.hasAcceptLanguage(lang))
      {
        return lang;
      }

      lang = conf.getProperty("core.language.link." + lang);
      if (CoreLanguage.hasAcceptLanguage(lang))
      {
        return lang;
      }

      /**
       * 如果语言字串中带有"-"符号, 则按"-"拆分去后面部分,
       * 检查其是否是允许的语种.
       */
      if ( 0 < arr2[0].indexOf('-'))
      {
        arr2 = arr2[0].split("-", 2);

        lang = arr2[0];
        if (CoreLanguage.hasAcceptLanguage(lang))
        {
          return lang;
        }

        lang = conf.getProperty("core.language.link." + lang);
        if (CoreLanguage.hasAcceptLanguage(lang))
        {
          return lang;
        }
      }
    }

    return null;
  }

  /**
   * 检查服务器是否存在该语言资源
   * @param lang
   * @return 存在为true, 反之为false
   */
  public static boolean hasAcceptLanguage(String lang)
  {
    String path = Core.CONF_PATH + File.separator
          + "default." + lang + ".properties";
    return (new File(path)).exists();
  }

}