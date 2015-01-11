package app.hongs.cmdlet;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.util.ClassNames;
import app.hongs.util.Text;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
      CmdletHelper.println("ERROR: Cmdlet name can not be empty.");
      return;
    }

    // 获取方法
    Method method = getCmdlets().get(act);
    if (null == method)
    {
      CmdletHelper.println("ERROR: Cmdlet "+act+" is not exists.");
      return;
    }

    // 执行方法
    try
    {
      if (0 < Core.DEBUG)
      {
        CoreLogger.debug(Core.ACTION_NAME.get()+" Starting...");
      }

      method.invoke(null, new Object[] {args} );

      if (0 < Core.DEBUG)
      {
        CoreLogger.debug(Core.ACTION_NAME.get()+" Finished!!!");
      }
    }
    catch (   IllegalAccessException ex)
    {
      CmdletHelper.println("ERROR: Illegal access for method '"+method.getClass().getName()+"."+method.getName()+"(ActionHelper).");
    }
    catch ( IllegalArgumentException ex)
    {
      CmdletHelper.println("ERROR: Illegal params for method '"+method.getClass().getName()+"."+method.getName()+"(ActionHelper).");
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
      /**
       * 输出总的运行时间
       * 并清除参数及核心
       */
      if (1 < Core.DEBUG)
      {
          CoreLogger.debug("Total exec time: "
          +(Text.humanTime(System.currentTimeMillis()-Core.STARTS_TIME)));
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

    Core.CONF_PATH = Core.BASE_PATH + File.separator + "etc";
    Core.VARS_PATH = Core.BASE_PATH + File.separator + "var";

    CoreConfig conf = Core.getInstance(CoreConfig.class);
    Core.VARS_PATH = conf.getProperty("core.vars.path", Core.VARS_PATH);
    Core.LOGS_PATH = Core.VARS_PATH + File.separator + "log";
    Core.SERS_PATH = Core.VARS_PATH + File.separator + "ser";
    Core.LOGS_PATH = conf.getProperty("core.logs.path", Core.LOGS_PATH);
    Core.SERS_PATH = conf.getProperty("core.tmps.path", Core.SERS_PATH);
    Core.SERVER_ID = conf.getProperty("core.server.id", "" );

    /** 系统属性配置 **/

        Map m = new HashMap();
        m.put("BASE_PATH", Core.BASE_PATH);
        m.put("CONF_PATH", Core.CONF_PATH);
        m.put("VARS_PATH", Core.VARS_PATH);
        m.put("LOGS_PATH", Core.LOGS_PATH);
        m.put("LOGS_PATH", Core.SERS_PATH);
        
        // 启动系统属性
        for (Map.Entry et : conf.entrySet()) {
            String k = (String)et.getKey  ();
            String v = (String)et.getValue();
            if (k.startsWith("start.")) {
                k = k.substring(6  );
                v = Text.inject(v,m);
                System.setProperty(k,v);
            }
        }

    if (0 < Core.DEBUG) {
        // 调试系统属性
        for (Map.Entry et : conf.entrySet()) {
            String k = (String)et.getKey  ();
            String v = (String)et.getValue();
            if (k.startsWith("debug.")) {
                k = k.substring(6  );
                v = Text.inject(v,m);
                System.setProperty(k,v);
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
    Core.ACTION_NAME.set(act);

    String zone = null;
    if (opts.containsKey("timezone"))
    {
      zone = (String)opts.get("timezone");
    }
    if (zone == null || zone.length() == 0)
    {
      zone = conf.getProperty("core.timezone.default");
    }
    Core.ACTION_ZONE.set(zone);

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

    str = (String) opts.get("request--");
    Map req  = null;
    if (str != null && str.length() > 0)
    {
        req = ActionHelper.parseParam(CmdletHelper.parseQuery(str));
    }

    str = (String) opts.get("context--");
    Map con  = null;
    if (str != null && str.length() > 0)
    {
        con = ActionHelper.parseParam(CmdletHelper.parseQuery(str));
    }

    str = (String) opts.get("session--");
    Map ses  = null;
    if (str != null && str.length() > 0)
    {
        ses = ActionHelper.parseParam(CmdletHelper.parseQuery(str));
    }

    ActionHelper helper = new ActionHelper(req, con, ses, null );
    Core.getInstance().put(ActionHelper.class.getName(), helper);

    return args;
  }

    private static Map<String, Method> CMDLETS = null;

    public  static Map<String, Method> getCmdlets() {
        if (CMDLETS != null) {
            return  CMDLETS;
        }
        
        String[] pkgs = CoreConfig.getInstance().getProperty("core.serv.path").split(";");
        CMDLETS = getCmdlets( pkgs );
        return CMDLETS;
    }
    
    private static Map<String, Method> getCmdlets(String... pkgs) {
        Map<String, Method> acts = new HashMap();

        for(String pkgn : pkgs) {
            Set< String > clss;
            try {
                clss = ClassNames.getClassNames(pkgn);
            } catch (IOException ex) {
                throw new HongsError( 0x4b , "Can not load package '" + pkgn + "'.", ex);
            }
            if (clss == null) {
                throw new HongsError( 0x4b , "Can not find package '" + pkgn + "'.");
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
