package app.hongs.cmdlet;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.util.Text;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 外壳程序启动器(原名shell)
 *
 * <h3>配置选项:</h3>
 * <pre>
 * core.server.id         服务ID
 * core.language.probing  自动识别语言
 * core.language.default  默认语言类型
 * </pre>
 *
 * @author Hongs
 */
public class Cmdlet
{

  public static void main(String[] args)
    throws IOException, HongsException
  {
    args = init(args);

    Core core = Core.getInstance();
    String act = Core.ACTION_PATH.get();

    if (act == null || act.length() == 0)
    {
      CmdletHelper.println("ERROR: Cmdlet name can not be empty.");
      return;
    }

    if (act.startsWith(".") || act.endsWith("."))
    {
      CmdletHelper.println("ERROR: Can not parse Cmdlet name '"+act+"'.");
      return;
    }

    int pos = act.lastIndexOf('.');
    if (pos == -1)
    {
      CmdletHelper.println("ERROR: Can not parse Cmdlet name '"+act+"'.");
      return;
    }

    String cls, mtd;
    mtd =  "cmdlet";
    cls = act.substring(pos+1);
    act = act.substring(0,pos);

    // 命令地址映射
    CoreConfig conf = (CoreConfig )
      Core.getInstance(CoreConfig.class);
    act = "app."+act+".cmdlet";
    if (conf.containsKey( act )) {
        act = conf.getProperty(act);
    }

    cls = act+"."+cls;

    /** 执行指定程序 **/

    // 获取类
    Class klass;
    try
    {
      klass = Class.forName(cls);
    }
    catch (ClassNotFoundException ex)
    {
      CmdletHelper.println("ERROR: Can not find class '" + cls + "'.");
      return;
    }

    // 动作类必须加上 Cmdlet 注解. Add by Hongs, 2014/7/14
    if (! klass.isAnnotationPresent(app.hongs.cmdlet.annotation.Cmdlet.class))
    {
      CmdletHelper.println("ERROR: Can not exec class '" + cls + "'.");
      return;
    }

    // 获取方法
    Method method;
    try
    {
      method = klass.getMethod(mtd, new Class[] {String[].class});
    }
    catch (NoSuchMethodException ex)
    {
      CmdletHelper.println("ERROR: Can not find method '" + cls + "." + mtd + "'.");
      return;
    }
    catch (SecurityException ex)
    {
      CmdletHelper.println("ERROR: Can not execute method  '" + cls + "." + mtd + "'.");
      return;
    }
    finally
    {
      klass = null;
    }

    // 执行方法
    try
    {
      if (0 < Core.DEBUG)
      {
        CoreLogger.debug(Core.ACTION_PATH.get() + " Starting...");
      }

      method.invoke(null, new Object[] {args});

      if (0 < Core.DEBUG)
      {
        CoreLogger.debug(Core.ACTION_PATH.get() + " Finished!!!");
      }
    }
    catch (IllegalAccessException ex)
    {
      CmdletHelper.println("ERROR: Illegal access for method '" + cls + "." + mtd + "'.");
      return;
    }
    catch (IllegalArgumentException ex)
    {
      CmdletHelper.println("ERROR: Illegal argument for method '" + cls + "." + mtd + "'.");
      return;
    }
    catch (InvocationTargetException ex)
    {
      Throwable ta = ex.getCause();

      /**
       * 构建错误消息
       */
      String error = ta.getLocalizedMessage();
      if (! (ta instanceof HongsException)
      &&  ! (ta instanceof HongsError  ) )
      {
        CoreLanguage lang = (CoreLanguage)
            Core.getInstance(CoreLanguage.class );
        if (error == null || error.length() == 0)
        {
          error = lang.translate("core.error.unkwn");
        }
          error = lang.translate("core.error.label",
                  ta.getClass().getName())
                  + ": " + error ;
      }

      CoreLogger  .error  ( ta  );
      CmdletHelper.println(error);
    }
    finally
    {
      method = null;

      /**
       * 输出总的运行时间
       * 并清除参数及核心
       */
      if (1 < Core.DEBUG)
      {
          CoreLogger.debug("Total exec time: "
          +(Text.humanTime(System.currentTimeMillis()-core.STARTS_TIME)));
      }

      try
      {
          core.destroy( );
      }
      catch (Throwable e)
      {
          CoreLogger.error(e);
      }
    }
  }

  public static String[] init(String[] args)
    throws IOException, HongsException
  {
    Map<String, Object> opts;
    opts = CmdletHelper.getOpts(args,
      "basepath:s", "basehref:s", "language:s",
      "request:s" , "session:s" , "cookie:s" , "debug:i"
    );
    args = (String[]) opts.get("");

    Core.THREAD_CORE.set(Core.GLOBAL_CORE);
    Core.ACTION_TIME.set(Core.STARTS_TIME);

    /** 静态属性配置 **/

    Core.ENVIR = 0;
    Core.DEBUG = 0;
    Core.BASE_HREF = "";
    Core.BASE_PATH = System.getProperty("user.dir");

    if (opts.containsKey("debug"))
    {
      Core.DEBUG = Byte.parseByte(opts.get("debug").toString());
    }

    if (opts.containsKey("basehref"))
    {
      Core.BASE_HREF = (String)opts.get( "basehref" );
      Core.BASE_HREF =  Pattern.compile( "[/\\\\]$" )
          .matcher(Core.BASE_HREF).replaceFirst( "" );
    }

    if (opts.containsKey("basepath"))
    {
      Core.BASE_PATH = (String)opts.get( "basepath" );
      Core.BASE_PATH =  Pattern.compile( "[/\\\\]$" )
          .matcher(Core.BASE_PATH).replaceFirst( "" );
    }

    Core.CONF_PATH = Core.BASE_PATH + File.separator + "conf";
    Core.LANG_PATH = Core.BASE_PATH + File.separator + "lang";
    Core.LOGS_PATH = Core.BASE_PATH + File.separator + "logs";
    Core.TMPS_PATH = Core.BASE_PATH + File.separator + "tmps";

    CoreConfig conf = (CoreConfig)Core.getInstance( CoreConfig.class );
    Core.LOGS_PATH = conf.getProperty("core.logs.dir", Core.LOGS_PATH);
    Core.TMPS_PATH = conf.getProperty("core.tmps.dir", Core.TMPS_PATH);
    Core.SERVER_ID = conf.getProperty("core.server.id", "");

    /** 系统属性配置 **/

        // 启动系统属性
        for (Map.Entry et : conf.entrySet()) {
            String k = (String)et.getKey  ();
            String v = (String)et.getValue();
            if (k.startsWith("start.")) {
                System.setProperty(k.substring(6), v);
            }
        }

    if (0 < Core.DEBUG) {
        // 调试系统属性
        for (Map.Entry et : conf.entrySet()) {
            String k = (String)et.getKey  ();
            String v = (String)et.getValue();
            if (k.startsWith("debug.")) {
                System.setProperty(k.substring(6), v);
            }
        }
    }


    /** 实例属性配置 **/

    String act = null;
    if (args.length > 0 )
    {
      List<String> argz = new ArrayList();
      argz.addAll(Arrays.asList( args ) );
      act  = argz.remove( 0 );
      args = argz.toArray(new String[0] );
    }
    Core.ACTION_PATH.set(act);

    String lang = null;
    if (opts.containsKey("language"))
    {
      lang = (String)opts.get("language");
    }
    if (lang == null || lang.length() == 0)
    {
      if (conf.getProperty("core.language.probing", false))
      {
        String l = System.getProperty("user.language");
        String c = System.getProperty("user.country" );
        if (l != null && c != null)
        {
          lang = l.toLowerCase() +"-"+ c.toUpperCase();
        }
        else
        if (l != null)
        {
          lang = l;
        }

        /**
         * 检查是否是支持的语言
         */
        if (lang != null)
        {
          lang = CoreLanguage.getAcceptLanguage(lang);
        }
        if (lang == null)
        {
          lang = conf.getProperty("core.language.default");
        }
      }
      else
      {
          lang = conf.getProperty("core.language.default");
      }
    }
    else
    {
      /**
       * 检查语言参数设置
       */
          lang = CoreLanguage.getAcceptLanguage(lang);
      if (lang == null)
      {
        CmdletHelper.println("ERROR: Unsupported language: "+lang+".");
        System.exit(1);
      }
    }
    Core.ACTION_LANG.set(lang);

    /** 初始化核心 **/

    String str;

    str = (String)opts.get("request");
    Map<String, String[]>  req = null;
    if (str != null && str.length( ) > 0)
    {
        req  = parseQueryString(str);
    }

    str = (String)opts.get("session");
    Map<String, String[]>  ses = null;
    if (str != null && str.length( ) > 0)
    {
        ses  = parseQueryString(str);
    }

    str = (String)opts.get("cookies");
    Map<String, String[]>  cok = null;
    if (str != null && str.length( ) > 0)
    {
        cok  = parseQueryString(str);
    }

    ActionHelper helper = new ActionHelper(req, ses, cok, null );
    Core.getInstance().put(ActionHelper.class.getName(), helper);

    return args;
  }

  /**
   * Parses an URL query string and returns a map with the parameter values.
   * The URL query string is the part in the URL after the first '?' character up
   * to an optional '#' character. It has the format "name=value&name=value&...".
   * The map has the same structure as the one returned by
   * javax.servlet.ServletRequest.getParameterMap().
   * A parameter name may occur multiple times within the query string.
   * For each parameter name, the map contains a string array with the parameter values.
   * @param   s an URL query string.
   * @return  a map containing parameter names as keys and parameter values as map values.
   * @author  Christian d'Heureuse, Inventec Informatik AG, Switzerland, www.source-code.biz.
   */
  public static Map<String, String[]> parseQueryString(String s) {
    if (s == null) {
      return new HashMap<String, String[]>(0);
    }
    // In map1 we use strings and ArrayLists to collect the parameter values.
    HashMap<String, Object> map1 = new HashMap<String, Object>();
    int p = 0;
    while (p < s.length()) {
      int p0 = p;
      while (p < s.length() && s.charAt(p) != '=' && s.charAt(p) != '&') {
        p++;
      }
      String name;
      try {
        name = s.substring(p0, p);
        name = URLDecoder.decode(name, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        throw new HongsError(0x10, ex);
      }
      if (p < s.length() && s.charAt(p) == '=') {
        p++;
      }
      p0 = p;
      while (p < s.length() && s.charAt(p) != '&') {
        p++;
      }
      String value;
      try {
        value = s.substring(p0, p);
        value = URLDecoder.decode(value, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        throw new HongsError(0x10, ex);
      }
      if (p < s.length() && s.charAt(p) == '&') {
        p++;
      }
      Object x = map1.get(name);
      if (x == null) {
        // The first value of each name is added directly as a string to the map.
        map1.put(name, value);
      } else if (x instanceof String) {
        // For multiple values, we use an ArrayList.
        ArrayList<String> a = new ArrayList<String>();
        a.add((String) x);
        a.add(value);
        map1.put(name, a);
      } else {
        @SuppressWarnings("unchecked")
        ArrayList<String> a = (ArrayList<String>) x;
        a.add(value);
      }
    }
    // Copy map1 to map2. Map2 uses string arrays to store the parameter values.
    HashMap<String, String[]> map2 = new HashMap<String, String[]>(map1.size());
    for (Map.Entry<String, Object> e : map1.entrySet()) {
      String name = e.getKey();
      Object x = e.getValue();
      String[] v;
      if (x instanceof String) {
        v = new String[]{(String) x};
      } else {
        @SuppressWarnings("unchecked")
        ArrayList<String> a = (ArrayList<String>) x;
        v = new String[a.size()];
        v = a.toArray(v);
      }
      map2.put(name, v);
    }
    return map2;
  }

}
