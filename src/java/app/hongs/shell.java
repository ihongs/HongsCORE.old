package app.hongs;

import java.io.File;
import java.net.URLDecoder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import app.hongs.action.ActionHelper;

/**
 * <h1>外壳程序启动器</h1>
 *
 * <h2>配置选项:</h2>
 * <pre>
 * core.server.id         服务ID
 * core.language.detect   自动识别语言
 * core.language.default  默认语言类型
 * </pre>
 *
 * @author Hongs
 */
public class shell
{

  private static Map<String, String[]> opts;

  public static void main(String[] args)
    throws IOException
  {
    shell.init(args);

    Core core = Core.getInstance();

    if (core.ACTION == null || core.ACTION.length() == 0)
    {
      ShellHelper.print("ERROR: Shell name can not be empty.");
      return;
    }

    if (core.ACTION.startsWith(".") || core.ACTION.endsWith("."))
    {
      ShellHelper.print("ERROR: Can not parse shell name '"+core.ACTION + "'.");
      return;
    }

    String cls = "app.shell." + core.ACTION;
    String mtd = "shell";

    /** 执行指定程序 **/

    // 获取类
    Class klass;
    try
    {
      klass = Class.forName(cls);
    }
    catch (ClassNotFoundException ex)
    {
      ShellHelper.print("ERROR: Can not find class '" + cls + "'.");
      return;
    }

    // 获取方法
    Method method;
    try
    {
      method = klass.getMethod(mtd, new Class[] {Map.class});
    }
    catch (NoSuchMethodException ex)
    {
      ShellHelper.print("ERROR: Can not find method '" + cls + "." + mtd + "'.");
      return;
    }
    catch (SecurityException ex)
    {
      ShellHelper.print("ERROR: Can not execute method  '" + cls + "." + mtd + "'.");
      return;
    }
    finally
    {
      klass = null;
    }

    // 执行方法
    try
    {
      method.invoke(null, new Object[] {opts});

      ShellHelper.print(core.ACTION + " complete.");
    }
    catch (IllegalAccessException ex)
    {
      ShellHelper.print("ERROR: Illegal access for method '" + cls + "." + mtd + "'.");
      return;
    }
    catch (IllegalArgumentException ex)
    {
      ShellHelper.print("ERROR: Illegal argument for method '" + cls + "." + mtd + "'.");
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
          Core.getInstance("app.hongs.CoreLanguage");
        if (error == null || error.length() == 0)
        {
          error = lang.translate("core.error.unkwn");
        }
          error = lang.translate("core.error.label",
                  ta.getClass().getName())
                  + ": " + error;
      }

      ShellHelper.print(error);

      /**
       * 记录跟踪信息
       */
      if (Core.IN_DEBUG_MODE)
      {
        ta.printStackTrace(System.out);
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
      ShellHelper.printETime("Total exec time", core.TIME);
      Core.destroyAll();
      opts = null;
      args = null;
    }
  }

  public static void init(String[] args)
    throws IOException
  {
    Map<String, Object>        optz ;
    opts = ShellHelper.getOpts(args);
    optz = ShellHelper.getOpts(opts ,":+s",
      "request:s","session:s","language:s",
      "debug:b","base-path:s","base-href:s"
    );

    /** 静态属性配置 **/

    Core.IN_SHELL_MODE = true ;
    Core.IN_DEBUG_MODE = false;

    if (optz.containsKey("debug"))
    {
      Core.IN_DEBUG_MODE = (Boolean)optz.get("debug");
    }

    if (optz.containsKey("base-href"))
    {
      Core.BASE_PATH = (String) optz.get("base-href");
    }

    if (optz.containsKey("base-path"))
    {
      Core.BASE_PATH = (String) optz.get("base-path");
    }
    else
    {
      Core.BASE_PATH = System.getProperty("user.dir");
    }

    Core.CONF_PATH = Core.BASE_PATH + File.separator + "conf";
    Core.LANG_PATH = Core.BASE_PATH + File.separator + "lang";
    Core.LOGS_PATH = Core.BASE_PATH + File.separator + "logs";
    Core.TMPS_PATH = Core.BASE_PATH + File.separator + "tmps";

    CoreConfig conf = (CoreConfig)Core.getInstance("app.hongs.CoreConfig");
    Core.LOGS_PATH  = conf.getProperty("core.logs.dir", Core.LOGS_PATH);
    Core.TMPS_PATH  = conf.getProperty("core.tmps.dir", Core.TMPS_PATH);
    Core.SERVER_ID  = conf.getProperty("core.server.id", "0");
    Core.getInstance(0).put("__IN_OBJECT_MODE__",
                      conf.getProperty("core.in.object.mode", false));

    /** 实例属性配置 **/

    String act = null;
    if (optz.containsKey(""))
    {
      /**
       * 获取并移除第一个匿名参数
       * 并修改opts中的匿名参数表
       */
      List optx = (List)optz.get("");
      act  =  (String)optx.remove(0);
      opts.put("", (String[])optx.toArray(new String[0]));
    }

    String lang = null;
    if (optz.containsKey("language"))
    {
      lang = (String)optz.get("language");
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
        ShellHelper.print("ERROR: Unsupported language: "+lang+".");
        System.exit(1);
      }
    }

    /** 初始化核心 **/

    Core core = Core.getInstance();
      core.init(act, lang);
    ActionHelper helper = (ActionHelper)
      core.get("app.hongs.action.ActionHelper");

    String str;
    str = (String)optz.get("request");
    Map<String, String[]>  req = null;
    if (str != null && str.length( ) > 0)
    {
        req  = parseQueryString(str);
    }
    Map<String, String[]>  ses = null;
    if (str != null && str.length( ) > 0)
    {
        req  = parseQueryString(str);
    }

    helper.init(req, ses);
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
