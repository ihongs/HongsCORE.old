package app.hongs.cmdlet;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLocale;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.util.Clazz;
import app.hongs.util.Data;
import app.hongs.util.Synt;
import app.hongs.util.Text;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 外壳程序启动器
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
public class CmdletRunner
{

  public static void main(String[] args)
    throws IOException, HongsException
  {
    args = init(args);

    Core  core = Core.getInstance();
    String act = Core.ACTION_NAME.get(  );

    if (null == act || act.length() == 0)
    {
      System.err.println("ERROR: Cmdlet name can not be empty.");
      System.exit(2);
      return;
    }

    // 获取方法
    Method method = getCmdlets().get(act);
    if (null == method)
    {
      System.err.println("ERROR: Cmdlet "+act+" is not exists.");
      System.exit(2);
      return;
    }

    // 执行方法
    try
    {
      if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG))
      {
        CmdletHelper.println("Starting...");
      }

      method.invoke(null, new Object[] { args } );

      if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG))
      {
        CmdletHelper.println("Finished!!!");
      }
    }
    catch (   IllegalAccessException ex)
    {
      CoreLogger.error("Illegal access for method '"+method.getClass().getName()+"."+method.getName()+"(String[]).");
      System.exit(3);
    }
    catch ( IllegalArgumentException ex)
    {
      CoreLogger.error("Illegal params for method '"+method.getClass().getName()+"."+method.getName()+"(String[]).");
      System.exit(3);
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
        CoreLocale lang = Core.getInstance(CoreLocale.class);
        if (error == null || error.length() == 0)
        {
          error = lang.translate("core.error.unkwn", ta.getClass().getName());
        }
        else
        {
          error = lang.translate("core.error.label", ta.getClass().getName()) + ": " + error;
        }
      }

      CoreLogger.error(ta);
      System.exit(4);
    }
    finally
    {
      try
      {
          core.destroy( );
      }
      catch (Throwable e)
      {
          CoreLogger.error(e);
          System.exit (5);
      }

      /**
       * 输出总的运行时间
       * 并清除参数及核心
       */
      if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG))
      {
          CmdletHelper.println("Total exec time: "
          +(Text.humanTime(System.currentTimeMillis()-Core.STARTS_TIME)));
      }
    }
  }

  public static String[] init(String[] args)
    throws IOException, HongsException
  {
    Map<String, Object> opts;
    opts = CmdletHelper.getOpts(args,
      "debug:i" ,
      "corepath:s" , "confpath:s" , "varspath:s" , "tmpspath:s" ,
      "basepath:s" , "basehref:s" , "language:s" , "timezone:s" ,
      "request--:s", "context--:s", "session--:s", "cookies--:s"
    );
    args = (String[]) opts.get("");

    Core.THREAD_CORE.set(Core.GLOBAL_CORE);
    Core.ACTION_TIME.set(Core.STARTS_TIME);

    /** 核心属性配置 **/

    Core.ENVIR = 0;
    Core.DEBUG = Synt.declare(opts.get("debug") , (byte) 0);
    Core.CORE_PATH = Synt.declare(opts.get("corepath"), System.getProperty("user.dir"));
    Core.CORE_PATH = new File(Core.CORE_PATH).getAbsolutePath();
    Core.CONF_PATH = Synt.declare(opts.get("confpath"), Core.CORE_PATH + File.separator + "etc");
    Core.DATA_PATH = Synt.declare(opts.get("varspath"), Core.CORE_PATH + File.separator + "var");
    Core.BASE_PATH = Synt.declare(opts.get("basepath"), Core.CORE_PATH + File.separator + "web");
    Core.BASE_HREF = Synt.declare(opts.get("basehref"), "");

    // 如果 web 目录不存在, 则视为在 WEB-INF 下
    File bp = new File(Core.BASE_PATH );
    if (!bp.exists()) {
        Core.BASE_PATH = bp.getParent();
    }

    /** 系统属性配置 **/

    CoreConfig cnf = CoreConfig.getInstance("_begin_");
    Core.SERVER_ID = cnf.getProperty("core.server.id" , "1");

    Map m = new HashMap();
    m.put("BASE_PATH", Core.BASE_PATH);
    m.put("CORE_PATH", Core.CORE_PATH);
    m.put("CONF_PATH", Core.CONF_PATH);
    m.put("DATA_PATH", Core.DATA_PATH);

    // 启动系统属性
    for (Map.Entry et : cnf.entrySet( )) {
        String k = (String)et.getKey(  );
        String v = (String)et.getValue();
        if (k.startsWith("begin.")) {
            k = k.substring(6  );
            v = Text.inject(v,m);
            System.setProperty(k,v);
        }
    }

    if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG)) {
    // 调试系统属性
    for (Map.Entry et : cnf.entrySet()) {
        String k = (String)et.getKey ( );
        String v = (String)et.getValue();
        if (k.startsWith("debug.")) {
            k = k.substring(6  );
            v = Text.inject(v,m);
            System.setProperty(k,v);
        }
    }
    }

    /** 实例属性配置 **/

    cnf = CoreConfig.getInstance();

    String act = null;
    if (args.length > 0 )
    {
      List<String> argz = new ArrayList();
      argz.addAll(Arrays.asList( args ) );
      act  = argz.remove( 0 );
      args = argz.toArray(new String[0] );
    }
    Core.ACTION_NAME.set(act);

    String zone = null;
    if (opts.containsKey("timezone"))
    {
      zone = (String)opts.get("timezone");
    }
    if (zone == null || zone.length() == 0)
    {
      zone = cnf.getProperty("core.timezone.default");
    }
    Core.ACTION_ZONE.set(zone);

    String lang = null;
    if (opts.containsKey("language"))
    {
      lang = (String)opts.get("language");
    }
    if (lang == null || lang.length() == 0)
    {
      if (cnf.getProperty("core.language.probing", false))
      {
        String l = System.getProperty("user.language");
        String c = System.getProperty("user.country" );
        if (l != null && c != null)
        {
          lang = l.toLowerCase() +"_"+ c.toUpperCase();
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
          lang = CoreLocale.getAcceptLanguage(lang);
        }
        if (lang == null)
        {
          lang = cnf.getProperty("core.language.default", "zh_CN");
        }
      }
      else
      {
          lang = cnf.getProperty("core.language.default", "zh_CN");
      }
    }
    else
    {
      /**
       * 检查语言参数设置
       */
      String l = lang;
          lang = CoreLocale.getAcceptLanguage(lang);
      if (lang ==null)
      {
        CoreLogger.error("ERROR: Unsupported language: "+l+".");
        System.exit(1);
      }
    }
    Core.ACTION_LANG.set(lang);

    /** 初始化核心 **/

    Map req  = inid((String) opts.get("request--"));
    Map con  = inid((String) opts.get("context--"));
    Map ses  = inid((String) opts.get("session--"));
    Map cok  = inid((String) opts.get("cookies--"));

    ActionHelper helper = new ActionHelper(req,con,ses,cok,null);
    Core.getInstance().put(ActionHelper.class.getName(), helper);

    // Clean instatnces fis core at exit
    Runtime.getRuntime().addShutdownHook( new Thread( )
    {
      @Override
      public void  run()
      {
        try
        {
          Core.GLOBAL_CORE.destroy();
        }
        catch (Throwable ex)
        {
          throw HongsError.common(null, ex);
        }
      }
    });

    return args;
  }

  private static Map inid(String text)
  {
    if (text == null || text.length() == 0) {
        return  null;
    }

        text = text.trim();
    if (text.startsWith("@")) {
        text = Text.fetchFile(text.substring(1));
        text = text.replaceAll("//.*?(\\r|\\n|$)", "$1");
        text = text.trim();
    }

    Map data;
    if (text.startsWith("{") && text.endsWith("}")) {
        data = (Map) Data.toObject(text);
    } else {
        data = ActionHelper.parseParam(ActionHelper.parseQuery(text));
    }

    return  data;
  }

    private static Map<String, Method> CMDLETS = null;

    public  static Map<String, Method> getCmdlets() {
        if (CMDLETS != null) {
            return  CMDLETS;
        }

        String[] pkgs = CoreConfig.getInstance("_begin_").getProperty("core.load.serv").split(";");
        CMDLETS = getCmdlets( pkgs );
        return CMDLETS;
    }

    private static Map<String, Method> getCmdlets(String... pkgs) {
        Map<String, Method> acts = new HashMap();

        for(String pkgn : pkgs) {
            pkgn = pkgn.trim( );
            if (pkgn.length ( ) == 0) continue;
            Set< String > clss ;

            if (pkgn.endsWith(".*")) {
                pkgn = pkgn.substring(0, pkgn.length() - 2);
                try {
                    clss = Clazz.getClassNames(pkgn, false);
                } catch (IOException ex) {
                    throw new HongsError( 0x4b , "Can not load package '" + pkgn + "'.", ex);
                }
                if (clss == null) {
                    throw new HongsError( 0x4b , "Can not find package '" + pkgn + "'.");
                }
            } else {
                clss = new HashSet();
                clss.add(pkgn);
            }

            for(String clsn : clss) {
                Class  clso;
                try {
                    clso = Class.forName(clsn);
                } catch (ClassNotFoundException ex) {
                    throw new HongsError(0x4b, "Can not find class '" + clsn + "'.");
                }

                // 从注解提取动作名
                Cmdlet anno = (Cmdlet) clso.getAnnotation(Cmdlet.class);
                if (anno == null) {
                    continue;
                }
                String actn = anno.value();
                if (actn == null || actn.length() == 0) {
                    actn =  clsn;
                }

                Method[] mtds = clso.getMethods();
                for(Method mtdo : mtds) {
                    String mtdn = mtdo.getName( );

                    // 从注解提取动作名
                    Cmdlet annx = (Cmdlet) mtdo.getAnnotation(Cmdlet.class);
                    if (annx == null) {
                        continue;
                    }
                    String actx = annx.value();
                    if (actx == null || actx.length() == 0) {
                        actx =  mtdn;
                    }

                    // 检查方法是否合法
                    Class[] prms = mtdo.getParameterTypes();
                    if (prms == null || prms.length != 1 || !prms[0].isAssignableFrom(String[].class)) {
                        throw new HongsError(0x4b, "Can not find cmdlet method '"+clsn+"."+mtdn+"(String[])'.");
                    }

                    if ("__main__".equals(actx)) {
                        acts.put(actn /*__main__*/ , mtdo );
                    } else {
                        acts.put(actn + ":" + actx , mtdo );
                    }
                }
            }
        }

        return acts;
    }

}
