package app.hongs;

import app.hongs.util.Text;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 语言资源读取工具
 *
 * <p>
 * 为与配置保持一致, 故从CoreConfig继承.<br/>
 * 放弃使用"ResourceBundle"类加载语言资源.<br/>
 * 资源文件名为"xxx_语言[-国家].properties".<br/>
 * </p>
 *
 * <h3>配置选项:</h3>
 * <pre>
 * core.load.language.once  为true则仅加载一次, 为false由Core控制
 * core.language.link.xx    语言链接, xx为语言, 如:link.zh=zh-CN
 * </pre>
 *
 * @author Hongs
 */
public class CoreLanguage
  extends CoreConfig
{

  private String lang;

  private CoreLanguage that = null;

  /**
   * 加载指定路径\语言和名称的配置
   * 注意: 与 CoreConfig  不同, 由于语言文件存在多份,
   * 构造时用 loadIgnrFNF 加载, 故如果没有也不会报错.
   * @param name
   * @param lang
   */
  public CoreLanguage(String name, String lang)
  {
    super(null);

    this.lang = lang;

    if (null == lang)
    {
      throw new app.hongs.HongsError(0x2c, "Language is not specified for '" + name + "'.");
    }

    if (null != name)
    {
      this.loadIgnrFNF(name);
    }

    String dlng = getAcceptLanguage(CoreConfig.getInstance()
                 .getProperty("core.language.default","zh"));
    if ( ! dlng.equals(lang))
    {
      that = new CoreLanguage(name, dlng);
    }
  }

  /**
   * 加载指定名称的配置
   * @param name
   */
  public CoreLanguage(String name)
  {
    this(/**/name , Core.ACTION_LANG.get());
  }

  /**
   * 加载默认配置
   */
  public CoreLanguage()
  {
    this("default", Core.ACTION_LANG.get());
  }

  @Override
  public CoreLanguage clone()
  {
    return (CoreLanguage) super.clone();
  }

  /**
   * 加载指定语言文件
   * @param name
   */
  @Override
  public void load(String name)
  {
    super.load(name + "_" + this.lang);
    if (that != null)
    {
        that.loadIgnrFNF(name);
    }
  }

  /**
   * 翻译指定键对应的语句
   * @param key
   * @return 翻译后的语句
   */
  @Override
  public String getProperty(String key)
  {
    String str = super.getProperty(key);
    if  (  str == null && that != null) {
           str =  that.getProperty(key);
    }
    return str != null  ?  str  :  key ;
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
    String str =  this.getProperty(key);
    if  (  rep == null  )  return  str ;

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
   * @return 翻译后的语言, 会替换特定标识
   */
  public String translate(String key, List<String> rep)
  {
    String str =  this.getProperty(key);
    if  (  rep == null  )  return  str ;

    /**
     * 将语句中替换$n或${n}为指定的文字, n从0开始
     */
    return Text.inject(str, rep);
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
    String str =  this.getProperty(key);
    if  (  rep == null  )  return  str ;

    /**
     * 将语句中的$xxx或${xxx}替换成指定文字
     * 如果指定的替换文字不存在, 则替换为空
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
    return getInstance(name, Core.ACTION_LANG.get());
  }

  /**
   * 按配置名和语言名获取唯一语言对象
   * 如果配置core.load.language.once为true则仅加载一次
   * @param name 配置名
   * @return 唯一语言实例
   */
  public static CoreLanguage getInstance(String name, String lang)
  {
    String ck = CoreLanguage.class.getName() + ":" + name + ":" + lang;

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

    CoreLanguage inst = new CoreLanguage(name, lang);
    CoreConfig conf = CoreConfig.getInstance();
    if (conf.getProperty("core.load.language.once", false))
    {
      gore.put(ck, inst);
    }
    else
    {
      core.put(ck, inst);
    }

    return inst;
  }

  /**
   * 从HEAD串中获取支持的语言
   * @param lang
   * @return 语言标识, 如zh,zh-CN, 不存在为null
   */
  public static String getAcceptLanguage(String lang)
  {
    CoreConfig conf = Core.getInstance(CoreConfig.class);
    String[]   arr1 = lang.split(",");
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
    CoreConfig conf = CoreConfig.getInstance();
    String x = conf.getProperty("core.language.support",
               conf.getProperty("core.language.default", "zh"));
    return /**/Arrays.asList( x.split( "," ) ).contains( lang );

    /**
     * 有时候会单独为某个模块添加语言文件
     * 这时候检查默认语言资源就不太妥当了
     */
//    String path = Core.CONF_PATH + File.separator;
//    path = path + /* * * */ "default_" + lang + ".properties";
//    if ((new File(path)).exists())
//    {
//      return true;
//    }
//
//    path = "app/hongs/config/default_" + lang + ".properties";
//    if (null != CoreConfig.class.getClassLoader().getResourceAsStream(path))
//    {
//      return true;
//    }
//
//    return  false;
  }

}
