package app.hongs.serv;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionRunner;
import app.hongs.cmdlet.Cmdlet;
import app.hongs.cmdlet.CmdletHelper;
import app.hongs.cmdlet.CmdletRunner;
import app.hongs.db.DB;
import app.hongs.util.Synt;
import app.hongs.util.Text;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 通用命令
 * @author Hongs
 */
@Cmdlet("common")
public class CommonCmdlet {

  @Cmdlet("__main__")
  public static void exec(String[] args)
          throws app.hongs.HongsException {
    Map<String, Object> opts = CmdletHelper.getOpts(args,
      "make-uid:b",
      "show-env:b", "show-properties:b",
      "show-actions:b", "show-cmdlets:b",
      "to-16hex:i", "to-26hex:i", "to-36hex:i",
      "as-16hex:s", "as-26hex:s", "as-36hex:s",
      "test-opts:b", "test-text:b", "test-left:b", "test-rate:b",
      "?Usage: --show-env --show-properties --show-actions --show-cmdlets");

    // 唯一ID
    if (opts.containsKey("make-uid")
    && (Boolean)opts.get("make-uid")) {
        System.out.println("UID: "+Core.getUniqueId());
    }

    // 查看环境
    if (opts.containsKey("show-env")
    && (Boolean)opts.get("show-env")) {
      Map<String, String> env = new TreeMap(new PropComparator());
      env.putAll(new HashMap(System.getenv()));
      System.out.println("ENV:");
      for (Map.Entry<String, String> et : env.entrySet()) {
        String k = et.getKey(  );
        String v = et.getValue();
        System.out.println("  "+k+"\t"+v);
      }
    }

    // 查看属性
    if (opts.containsKey("show-properties")
    && (Boolean)opts.get("show-properties")) {
      Map<String, String> env = new TreeMap(new PropComparator());
      env.putAll(new HashMap(System.getProperties()));
      System.out.println("Properties:");
      for (Map.Entry<String, String> et : env.entrySet()) {
        String k = et.getKey(  );
        String v = et.getValue();
        System.out.println("  "+k+"\t"+v);
      }
    }

    // 查看动作
    if (opts.containsKey("show-actions")
    && (Boolean)opts.get("show-actions")) {
      Map<String, Method> actions = new TreeMap(new PropComparator());
      actions.putAll(ActionRunner.getActions());
      System.out.println("Actions:");
      for (Map.Entry<String, Method> et : actions.entrySet()) {
        String a = et.getKey(  );
        Method m = et.getValue();
        System.out.println("  "+a+"\t"+m.getDeclaringClass().getName()+"."+m.getName());
      }
    }

    // 查看命令
    if (opts.containsKey("show-cmdlets")
    && (Boolean)opts.get("show-cmdlets")) {
      Map<String, Method> actions = new TreeMap(new PropComparator());
      actions.putAll(CmdletRunner.getCmdlets());
      System.out.println("Cmdlets:");
      for (Map.Entry<String, Method> et : actions.entrySet()) {
        String a = et.getKey(  );
        Method m = et.getValue();
        System.out.println("  "+a+"\t"+m.getDeclaringClass().getName()+"."+m.getName());
      }
    }

    // 进制转换
    if (opts.containsKey("to-16hex")) {
        System.out.println(opts.get("to-16hex") + " to 16 Hex: "+Long.toHexString((Long) opts.get("to-16hex")));
    }
    if (opts.containsKey("to-26hex")) {
        System.out.println(opts.get("to-26hex") + " to 26 Hex: "+app.hongs.util.Text.to26Hex((Long) opts.get("to-26hex")));
    }
    if (opts.containsKey("to-36hex")) {
        System.out.println(opts.get("to-36hex") + " to 36 Hex: "+app.hongs.util.Text.to36Hex((Long) opts.get("to-36hex")));
    }
    if (opts.containsKey("as-16hex")) {
        System.out.println(opts.get("as-16hex") + " as 16 Hex: "+Long.parseLong((String) opts.get("as-16hex"), 16));
    }
    if (opts.containsKey("as-26hex")) {
        System.out.println(opts.get("as-36hex") + " as 26 Hex: "+app.hongs.util.Text.as26Hex((String) opts.get("as-26hex")));
    }
    if (opts.containsKey("as-36hex")) {
        System.out.println(opts.get("as-36hex") + " as 36 Hex: "+app.hongs.util.Text.as36Hex((String) opts.get("as-36hex")));
    }

    // 参数测试
    if (opts.containsKey("test-opts")
    && (Boolean)opts.get("test-opts")) {
      testOpts(args);
    }

    // 文本测试
    if (opts.containsKey("test-text")
    && (Boolean)opts.get("test-text")) {
      testText();
    }

    // 进度测试
    if (opts.containsKey("test-rate")
    && (Boolean)opts.get("test-rate")) {
      testRate();
    }
    if (opts.containsKey("test-left")
    && (Boolean)opts.get("test-left")) {
      testLeft();
    }
  }

  @Cmdlet("start")
  public static void start(String[] args) throws HongsException {
    Map<String, Object> opts = CmdletHelper.getOpts(args,
        "port:i", "path:s", "help:b");

    if (Synt.declare(opts.get("help"), false)) {
        System.err.println("Usage: --port SERVICE_PORT --path CONTEXT_PATH");
        return;
    }

    WebAppContext webapp = new WebAppContext();
    Server server = new Server(Synt.declare(opts.get("port"),8080));
    webapp.setContextPath (Synt.declare(opts.get("path"),""));
    webapp.setDescriptor  (Core.BASE_PATH+"/WEB-INF/web.xml");
    webapp.setResourceBase(Core.WEBS_PATH);
    webapp.setParentLoaderPriority( true );
    server.setHandler(webapp);

    try {
        server.start();
        server.join( );
    }
    catch (Exception ex) {
        throw new HongsException(HongsException.COMMON, ex);
    }
  }

  @Cmdlet("setup")
  public static void setup(String[] args) throws HongsException {
    String dir = "_setup_";
    if ( 0  < args.length ) {
        dir = args[0];
    }

    if (! new File(dir).isAbsolute()) {
        dir = Core.CONF_PATH+"/"+dir;
    }

    List<File> fxs = new ArrayList();
    File[/**/] fos = new File( dir ).listFiles();
    for (File fo : fos) {
        if (fo.isDirectory() || fo.isHidden()
        || !fo.getName( ).endsWith( ".sql" )) {
            continue;
        }
        fxs.add(fo );
    }

    Pattern pat = Pattern.compile("^--\\s*DB:\\s*([^\\s*]+)");
    Collections.sort(fxs, new FileComparator());
    for (File fo : fxs) {
        try {
            FileInputStream in=new FileInputStream(fo);
            int    size = in.available();
            byte[] buff = new byte[size];
            in.read (buff);
            in.close(/**/);

            String  sql = new String (buff,"UTF-8");
            Matcher mat = pat.matcher(sql);
            String  dbn = "default";
            if (mat.find()) {
                dbn = mat.group(1).trim( );
            }
            DB  db  =  DB.getInstance(dbn);

            CmdletHelper.println("Run '"+fo.getName()+"' for '"+dbn+"'...");
            sql = sql.replaceAll("(^|[\r\n])\\s*(--|/\\*\\!).*", "");
            String[] a = sql.split(";\\s*[\r\n]");

            long st = System.currentTimeMillis( );
            int al = a.length;
            int ok = 0;
            for(String s : a) {
                db.execute(s.trim());
                CmdletHelper.printELeft(st, al, ++ ok);
            }
        } catch (FileNotFoundException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }
  }

  private static void testOpts(String[] args) throws HongsException {
    app.hongs.util.Data.dumps(args);
    Map opts = CmdletHelper.getOpts(args,
      "opt_s|opt-s:s", "opt_i|opt-i:i", "opt_f|opt-f:f", "opt_b|opt-b:b",
      "opt_o|opt-o=s", "opt_m|opt-m+s", "opt_n|opt-n*s", "opt_r|opt-r=/(a|b)/i",
      "!U", "!V", "?Useage:\ncmd opt-o xxx opt-m xxx... [opt-n xxx...]"
    );
    app.hongs.util.Data.dumps(opts);
  }

  private static void testText() {
    String s = "\\\"Hello world!\", this is Hong's framework.\r\n"
             + "\\\"世界您好！\"，这是弘的框架。\"\r\n";
    CmdletHelper.println("source: " + s );
    String s1 = Text.escape(s);
    CmdletHelper.println("escape: " + s1);
    String s2 = Text.resume(s1);
    CmdletHelper.println("resume: " + s2);
  }

  private static void testRate() {
    try {
      int o = 0, e = 0;
      for (int i = 1; i <= 100; i++) {
        Thread.sleep(100);
        if (i % 3 > 0) {
          o++;
        } else {
          e++;
        }
        CmdletHelper.printERate(100, o, e);
      }
    } catch (InterruptedException ex) {
    }
  }

  private static void testLeft() {
    try {
      long t = System.currentTimeMillis();
      int o = 0, e = 0;
      for (int i = 1; i <= 100; i++) {
        Thread.sleep(100);
        if (i % 3 > 0) {
          o++;
        } else {
          e++;
        }
        CmdletHelper.printELeft(t, 100, o, e);
      }
    } catch (InterruptedException ex) {
    }
  }

  private static class FileComparator implements Comparator< File > {
    public int compare(File f1, File f2) {
        String s1 = f1.getName();
        String s2 = f2.getName();
        return s1.compareTo(s2 );
    }
  }

  private static class PropComparator implements Comparator<String> {
    public int compare(String s1, String s2) {
        return s1.compareTo(s2 );
    }
  }

}
