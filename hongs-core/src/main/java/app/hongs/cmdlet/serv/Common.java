package app.hongs.cmdlet.serv;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.cmdlet.CmdletHelper;
import app.hongs.cmdlet.CmdletRunner;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.util.Tool;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 常规服务
 * @author Hongs
 */
@Cmdlet("common")
public class Common {

  @Cmdlet("__main__")
  public static void exec(String[] args)
          throws app.hongs.HongsException {
    Map<String, Object> opts = CmdletHelper.getOpts(args,
      "to-16hex:i", "to-26hex:i", "to-36hex:i",
      "as-16hex:s", "as-26hex:s", "as-36hex:s",
      "?Usage: --to-36hex NUM --as-36hex STR");

    // 进制转换
    if (opts.containsKey("to-16hex")) {
        System.out.println(opts.get("to-16hex") + " to 16 Hex: "+Long.toHexString((Long) opts.get("to-16hex")));
    }
    if (opts.containsKey("to-26hex")) {
        System.out.println(opts.get("to-26hex") + " to 26 Hex: "+app.hongs.util.Tool.to26Hex((Long) opts.get("to-26hex")));
    }
    if (opts.containsKey("to-36hex")) {
        System.out.println(opts.get("to-36hex") + " to 36 Hex: "+app.hongs.util.Tool.to36Hex((Long) opts.get("to-36hex")));
    }
    if (opts.containsKey("as-16hex")) {
        System.out.println(opts.get("as-16hex") + " as 16 Hex: "+Long.parseLong((String) opts.get("as-16hex"), 16));
    }
    if (opts.containsKey("as-26hex")) {
        System.out.println(opts.get("as-36hex") + " as 26 Hex: "+app.hongs.util.Tool.as26Hex((String) opts.get("as-26hex")));
    }
    if (opts.containsKey("as-36hex")) {
        System.out.println(opts.get("as-36hex") + " as 36 Hex: "+app.hongs.util.Tool.as36Hex((String) opts.get("as-36hex")));
    }
  }

  //** 测试 **/
  
  @Cmdlet("test-text")
  public static void testText(String[] args) {
    String s = "\\\"Hello world!\", this is Hong's framework.\r\n"
             + "\\\"世界您好！\"，这是弘的框架。\"\r\n";
    CmdletHelper.println("source: " + s );
    String s1 = Tool.escape(s);
    CmdletHelper.println("escape: " + s1);
    String s2 = Tool.resume(s1);
    CmdletHelper.println("resume: " + s2);
  }

  @Cmdlet("test-opts")
  public static void testOpts(String[] args) throws HongsException {
    app.hongs.util.Data.dumps(args);
    Map opts = CmdletHelper.getOpts(args,
      "opt_s|opt-s:s", "opt_i|opt-i:i", "opt_f|opt-f:f", "opt_b|opt-b:b",
      "opt_o|opt-o=s", "opt_m|opt-m+s", "opt_n|opt-n*s", "opt_r|opt-r=/(a|b)/i",
      "!U", "!V", "?Useage:\ncmd opt-o xxx opt-m xxx... [opt-n xxx...]"
    );
    app.hongs.util.Data.dumps(opts);
  }

  @Cmdlet("test-rate")
  public static void testRate(String[] args) {
    try {
      int o = 0, e = 0;
      for (int i = 1; i <= 100; i++) {
        Thread.sleep(100);
        if (i % 3 > 0) {
          o++;
        } else {
          e++;
        }
        CmdletHelper.progres(100, o, e);
      }
    } catch (InterruptedException ex) {
    }
  }

  @Cmdlet("test-left")
  public static void testLeft(String[] args) {
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
        CmdletHelper.progres(t, 100, o, e);
      }
    } catch (InterruptedException ex) {
    }
  }

    //** 命令 **/
  
    @Cmdlet("make-uid")
    public static void makeUID(String[] args) {
        System.out.println(Core.getUniqueId(args.length > 0 ? args[0] : Core.SERVER_ID));
    }

    @Cmdlet("show-env")
    public static void showENV(String[] args) {
        Map<String, String> a = new TreeMap(new PropComparator());
        Map<String, String> m = new HashMap(    System.getenv ());
        int i = 0, j;

        for (Map.Entry<String, String> et : m.entrySet()) {
            String k = et.getKey(  );
            String v = et.getValue();
            a.put(k, v);
            j = k.length();
            if (i < j && j < 31) {
                i = j;
            }
        }

        for (Map.Entry<String, String> n : a.entrySet()) {
            StringBuilder s = new StringBuilder();
            s.append(n.getKey());
            for (j = n.getKey( ).length( ); j < i; j ++) {
                s.append(" " );
            }   s.append("\t");
            s.append(n.getValue());
            System.out.println (s);
        }
    }

    @Cmdlet("show-properties")
    public static void showProperties(String[] args) {
        Map<String, String> a = new TreeMap( new  PropComparator());
        Map<String, String> m = new HashMap(System.getProperties());
        int i = 0, j;

        for (Map.Entry<String, String> et : m.entrySet()) {
            String k = et.getKey(  );
            String v = et.getValue();
            a.put(k, v);
            j = k.length();
            if (i < j && j < 31) {
                i = j;
            }
        }

        for (Map.Entry<String, String> n : a.entrySet()) {
            StringBuilder s = new StringBuilder();
            s.append(n.getKey());
            for (j = n.getKey( ).length( ); j < i; j ++) {
                s.append(" " );
            }   s.append("\t");
            s.append(n.getValue());
            System.out.println (s);
        }
    }

    @Cmdlet("show-cmdlets")
    public static void showCmdlets(String[] args) {
        Map<String, String> a = new TreeMap(new PropComparator());
        int i = 0, j;

        for (Map.Entry<String, Method> et : CmdletRunner.getCmdlets().entrySet()) {
            String k = et.getKey(  );
            Method v = et.getValue();
            a.put( k, v.getDeclaringClass().getName()+"."+v.getName() );
            j = k.length();
            if (i < j && j < 31) {
                i = j;
            }
        }

        for (Map.Entry<String, String> n : a.entrySet()) {
            StringBuilder s = new StringBuilder();
            s.append(n.getKey());
            for (j = n.getKey( ).length( ); j < i; j ++) {
                s.append(" " );
            }   s.append("\t");
            s.append(n.getValue());
            System.out.println (s);
        }
    }

    @Cmdlet("show-actions")
    public static void showActions(String[] args) {
        Map<String, String> a = new TreeMap(new PropComparator());
        int i = 0, j;

        for (Map.Entry<String, ActionRunner.Mathod> et : ActionRunner.getActions().entrySet()) {
            String k = et.getKey(  );
            ActionRunner.Mathod v = et.getValue();
            a.put( k, v.toString() );
            j = k.length();
            if (i < j && j < 31) {
                i = j;
            }
        }

        for (Map.Entry<String, String> n : a.entrySet()) {
            StringBuilder s = new StringBuilder();
            s.append(n.getKey());
            for (j = n.getKey( ).length( ); j < i; j ++) {
                s.append(" " );
            }   s.append("\t");
            s.append(n.getValue());
            System.out.println (s);
        }
    }

    @Cmdlet("exec-action")
    public static void execAction(String[] args) throws HongsException {
        if (args.length == 0) {
            System.err.println("Action name required!\r\nUsage: ACTION_NAME --request-- QUERY_STRING --context-- QUERY_STRING --session QUERY_STRING");
            return;
        }
        ActionHelper a = Core.getInstance(ActionHelper.class/**/);
        new app.hongs.action.ActionRunner(args[0], a ).doAction();
        app.hongs.util.Data.dumps(a.getResponseData());
    }

    private static class PropComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }

}
