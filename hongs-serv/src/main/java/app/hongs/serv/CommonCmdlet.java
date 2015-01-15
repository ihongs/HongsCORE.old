package app.hongs.serv;

import app.hongs.CoreSerially;
import app.hongs.HongsException;
import app.hongs.action.ActionRunner;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.cmdlet.CmdletHelper;
import app.hongs.cmdlet.CmdletRunner;
import app.hongs.util.Text;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 测试
 * @author Hongs
 */
@Cmdlet("common")
public class CommonCmdlet {

  @Cmdlet("__main__")
  public static void exec(String[] args)
          throws app.hongs.HongsException {
    Map<String, Object> opts = CmdletHelper.getOpts(args,
      "show-env:b",
      "show-properties:b",
      "show-actions:b",
      "show-cmdlets:b",
      "to-16hex:i",
      "to-26hex:i",
      "to-36hex:i",
      "as-16hex:s",
      "as-26hex:s",
      "as-36hex:s",
      "test-opts:b",
      "test-text:b",
      "test-left:b",
      "test-rate:b",
      "test-serially:b",
      "?Usage: --show-env --show-properties --show-actions --show-cmdlets");

    // 查看环境
    if (opts.containsKey("show-env")
    && (Boolean)opts.get("show-env")) {
      Map<String, String> env = new TreeMap(new MapKeyComparator());
      env.putAll(new HashMap(System.getenv()));
      System.err.println("ENV:");
      for (Map.Entry<String, String> et : env.entrySet()) {
        String k = et.getKey(  );
        String v = et.getValue();
        System.err.println("  "+k+"\t"+v);
      }
    }
    if (opts.containsKey("show-properties")
    && (Boolean)opts.get("show-properties")) {
      Map<String, String> env = new TreeMap(new MapKeyComparator());
      env.putAll(new HashMap(System.getProperties()));
      System.err.println("Properties:");
      for (Map.Entry<String, String> et : env.entrySet()) {
        String k = et.getKey(  );
        String v = et.getValue();
        System.err.println("  "+k+"\t"+v);
      }
    }
    
    // 查看动作
    if (opts.containsKey("show-actions")
    && (Boolean)opts.get("show-actions")) {
      Map<String, Method> actions = new TreeMap(new MapKeyComparator());
      actions.putAll(ActionRunner.getActions());
      System.err.println("Actions:");
      for (Map.Entry<String, Method> et : actions.entrySet()) {
        String a = et.getKey(  );
        Method m = et.getValue();
        System.err.println("  "+a+"\t"+m.getDeclaringClass().getName()+"."+m.getName());
      }
    }
    if (opts.containsKey("show-cmdlets")
    && (Boolean)opts.get("show-cmdlets")) {
      Map<String, Method> actions = new TreeMap(new MapKeyComparator());
      actions.putAll(CmdletRunner.getCmdlets());
      System.err.println("Cmdlets:");
      for (Map.Entry<String, Method> et : actions.entrySet()) {
        String a = et.getKey(  );
        Method m = et.getValue();
        System.err.println("  "+a+"\t"+m.getDeclaringClass().getName()+"."+m.getName());
      }
    }

    // 进制转换
    if (opts.containsKey("to-16hex")) {
        CmdletHelper.println(opts.get("to-16hex") + " to 16 Hex: "+Long.toHexString((Long) opts.get("to-16hex")));
    }
    if (opts.containsKey("to-26hex")) {
        CmdletHelper.println(opts.get("to-26hex") + " to 26 Hex: "+app.hongs.util.Text.to26Hex((Long) opts.get("to-26hex")));
    }
    if (opts.containsKey("to-36hex")) {
        CmdletHelper.println(opts.get("to-36hex") + " to 36 Hex: "+app.hongs.util.Text.to36Hex((Long) opts.get("to-36hex")));
    }
    if (opts.containsKey("as-16hex")) {
        CmdletHelper.println(opts.get("as-16hex") + " as 16 Hex: "+Long.parseLong((String) opts.get("as-16hex"), 16));
    }
    if (opts.containsKey("as-26hex")) {
        CmdletHelper.println(opts.get("as-36hex") + " as 26 Hex: "+app.hongs.util.Text.as26Hex((String) opts.get("as-26hex")));
    }
    if (opts.containsKey("as-36hex")) {
        CmdletHelper.println(opts.get("as-36hex") + " as 36 Hex: "+app.hongs.util.Text.as36Hex((String) opts.get("as-36hex")));
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

    // 进度演示
    if (opts.containsKey("test-rate")
    && (Boolean)opts.get("test-rate")) {
      testRate();
    }
    if (opts.containsKey("test-left")
    && (Boolean)opts.get("test-left")) {
      testLeft();
    }

    if (opts.containsKey("test-serially")
    && (Boolean)opts.get("test-serially")) {
      new TestSerially();
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
    String s = "This is Hong's Framework. Ask: \\\"Who ar you?\"\r\n"
             + "这是弘的框架, 问: \\\"你是谁?\"\r\n"
             + "%abc,$def.";
    CmdletHelper.println("actual: " + s);
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

  private static class MapKeyComparator implements Comparator<String>{  
    public int compare(String str1, String str2) {  
        return str1.compareTo(str2);  
    }  
  } 
  
  private static class TestSerially extends CoreSerially {
    java.util.List a;
    java.util.Set b;
    java.util.Map c;

    public TestSerially() throws HongsException {
      super("test_serially");
    }

    @Override
    protected void imports() {
      a = new java.util.ArrayList();
      b = new java.util.HashSet();
      c = new java.util.HashMap();
    }
  }
}
