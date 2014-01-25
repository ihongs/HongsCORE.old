package app.hongs.cmdlet;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;
import app.hongs.HongsError;
import app.hongs.HongsThrowable;
import app.hongs.action.ActionHelper;

import java.net.URLDecoder;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * 外壳程序启动器(原名shell)
 *
 * <h3>配置选项:</h3>
 * <pre>
 * core.server.id         服务ID
 * core.language.detect   自动识别语言
 * core.language.default  默认语言类型
 * </pre>
 *
 * @author Hongs
 */
public class Cmdlet
{

  public static void main(String[] args)
    throws IOException
  {
    args = init(args);

    Core core = Core.getInstance();
    String act = Core.ACTION_PATH.get();

    if (act == null || act.length() == 0)
    {
      CmdletHelper.print("ERROR: Cmdlet name can not be empty.");
      return;
    }

    if (act.startsWith(".") || act.endsWith("."))
    {
      CmdletHelper.print("ERROR: Can not parse Cmdlet name '"+act+"'.");
      return;
    }

    int pos = act.lastIndexOf('.');
    if (pos == -1)
    {
      CmdletHelper.print("ERROR: Can not parse Cmdlet name '"+act+"'.");
      return;
    }

    String cls =    "app."+act.substring(0,pos)
               +".cmdlet."+act.substring(pos+1);
    String mtd = "cmdlet";

    /** 执行指定程序 **/

    // 获取类
    Class klass;
    try
    {
      klass = Class.forName(cls);
    }
    catch (ClassNotFoundException ex)
    {
      CmdletHelper.print("ERROR: Can not find class '" + cls + "'.");
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
      CmdletHelper.print("ERROR: Can not find method '" + cls + "." + mtd + "'.");
      return;
    }
    catch (SecurityException ex)
    {
      CmdletHelper.print("ERROR: Can not execute method  '" + cls + "." + mtd + "'.");
      return;
    }
    finally
    {
      klass = null;
    }

    // 执行方法
    try
    {
      method.invoke(null, new Object[] {args});

      //ShellHelper.print(core.ACTION_PATH + " complete.");
    }
    catch (IllegalAccessException ex)
    {
      CmdletHelper.print("ERROR: Illegal access for method '" + cls + "." + mtd + "'.");
      return;
    }
    catch (IllegalArgumentException ex)
    {
      CmdletHelper.print("ERROR: Illegal argument for method '" + cls + "." + mtd + "'.");
      return;
    }
    catch (InvocationTargetException ex)
    {
      Throwable ta = ex.getCause();

      /**
       * 构建错误消息
       */
      String error = ta.getMessage();
      if (!(ta instanceof HongsThrowable))
      {
        CoreLanguage lang = (CoreLanguage)
            Core.getInstance(CoreLanguage.class );
        if (error == null || error.length() == 0)
        {
          error = lang.translate("core.error.unkwn");
        }
          error = lang.translate("core.error.label",
                  ta.getClass().getName())
                  + ": " + error;
      }

      CmdletHelper.print(error);

      /**
       * 记录跟踪信息
       */
      if (Core.IN_DEBUG_MODE)
      {
        ta.printStackTrace(System.err);
      }
      else
      if (!(ta instanceof HongsThrowable))
      {
        ta.printStackTrace(System.err);
      }

      return;
    }
    finally
    {
      method = null;

      /**
       * 输出总的运行时间
       * 并清除参数及核心
       */
      if (Core.IN_DEBUG_MODE)
          CmdletHelper.printETime("Total exec time", core.GLOBAL_TIME);

      core.destroy();
    }
  }

  public static String[] init(String[] args)
    throws IOException
  {
    Map<String, Object> opts;
    opts = CmdletHelper.getOpts(args,
      "basepath:s","basehref:s","language:s","session:s","request:s","debug:b"
    );
    args = (String[]) opts.get("");

    Core.THREAD_CORE.set(Core.GLOBAL_CORE);
    Core.ACTION_TIME.set(Core.GLOBAL_TIME);

    /** 静态属性配置 **/

    Core.IN_SHELL_MODE = true ;
    Core.IN_DEBUG_MODE = false;

    if (opts.containsKey("debug"))
    {
      Core.IN_DEBUG_MODE = (Boolean)opts.get("debug");
    }

    if (opts.containsKey("basepath"))
    {
      Core.BASE_PATH = (String)opts.get( "basepath" );
      Core.BASE_PATH =  Pattern.compile( "[/\\\\]$" )
          .matcher(Core.BASE_PATH).replaceFirst( "" );
    }
    else
    {
      Core.BASE_PATH = System.getProperty("user.dir");
    }

    Core.CONF_PATH = Core.BASE_PATH + File.separator + "conf";
    Core.LANG_PATH = Core.BASE_PATH + File.separator + "lang";
    Core.LOGS_PATH = Core.BASE_PATH + File.separator + "logs";
    Core.TMPS_PATH = Core.BASE_PATH + File.separator + "tmps";

    CoreConfig conf = (CoreConfig)Core.getInstance(CoreConfig.class);
    Core.LOGS_PATH  = conf.getProperty("core.logs.dir", Core.LOGS_PATH);
    Core.TMPS_PATH  = conf.getProperty("core.tmps.dir", Core.TMPS_PATH);
    Core.BASE_HREF  = conf.getProperty("core.base.href", "" );
    Core.SERVER_ID  = conf.getProperty("core.server.id", "0");

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
      if (conf.getProperty("core.language.detect", true))
      {
        String l = System.getProperty("user.language");
        String c = System.getProperty("user.country" );
        if (l != null && c != null)
        {
          lang = l.toLowerCase() + "-" + c.toUpperCase();
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
        CmdletHelper.print("ERROR: Unsupported language: "+lang+".");
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

    ActionHelper helper = (ActionHelper)
          Core.getInstance(ActionHelper.class);
    helper.init(req, ses);

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
