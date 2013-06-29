package app.hongs.util.cmdlet;

import app.hongs.CoreSerially;
import app.hongs.HongsException;
import app.hongs.cmdlet.CmdletHelper;
import app.hongs.action.ActionHelper;
import app.hongs.util.JSON;
import app.hongs.util.Text;
import java.util.Map;

/**
 * 测试
 * @author Hongs
 */
public class test {

  public void action(ActionHelper helper)
          throws HongsException {
    helper.print("HELLO!!!");
  }

  public static void cmdlet(Map<String, String[]> opts)
          throws app.hongs.HongsException {
    //app.hongs.util.JSON.print(opts);
    Map<String, Object> optz = CmdletHelper.getOpts(opts,
      "test-text:b", "test-rate:b", "test-left:b",
      "test-opts:b","test-radix:i", "test-serially:b",
      "show-env:b", "show-properties:b",
      "make-demo-data:b");
    //app.hongs.util.JSON.print(optz);

    if (optz.containsKey("test-text")
    && (Boolean)optz.get("test-text")) {
      testText();
    }

    if (optz.containsKey("test-rate")
    && (Boolean)optz.get("test-rate")) {
      testRate();
    }

    if (optz.containsKey("test-left")
    && (Boolean)optz.get("test-left")) {
      testLeft();
    }

    if (optz.containsKey("test-opts")
    && (Boolean)optz.get("test-opts")) {
      testOpts(opts);
    }

    if (optz.containsKey("test-radix")) {
      testRadix((long)optz.get("test-radix"));
    }
    
    if (optz.containsKey("test-serially")
    && (Boolean)optz.get("test-serially")) {
      new TestSerially();
    }

    if (optz.containsKey("show-env")
    && (Boolean)optz.get("show-env")) {
      JSON.print(System.getenv());
    }

    if (optz.containsKey("show-properties")
    && (Boolean)optz.get("show-properties")) {
      JSON.print(System.getProperties());
    }

    if (optz.containsKey("make-demo-data")
    && (Boolean)optz.get("make-demo-data")) {
      makeDemoData();
    }
  }

  private static void makeDemoData() {
    
  }

  private static void testText() {
    String s = "This is Hong's Framework. Ask: \\\"Who ar you?\"\r\n"
             + "这是弘的框架, 问: \\\"你是谁?\"\r\n"
             + "%abc,$def.";
    CmdletHelper.print("actual: " + s);
    String s1 = Text.escape(s);
    CmdletHelper.print("escape: " + s1);
    String s2 = Text.resume(s1);
    CmdletHelper.print("resume: " + s2);
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

  private static void testOpts(Map<String, String[]> opts) throws HongsException {
    app.hongs.util.JSON.print(opts);
    Map optz = CmdletHelper.getOpts(opts,
      "opt_s|opt-s:s", "opt_i|opt-i:i", "opt_f|opt-f:f", "opt_b|opt-b:b",
      "opt_o|opt-o=s", "opt_m|opt-m+s", "opt_n|opt-n*s", "opt_r|opt-r=/(a|b)/i",
      "!U", "!V", "?Useage:\ncmd opt-o xxx opt-m xxx... [opt-n xxx...]"
    );
    app.hongs.util.JSON.print(optz);
  }
  
  private static void testRadix(long num) {
    CmdletHelper.print("36 Radix: "+app.hongs.util.Num.to36Radix(num));
    CmdletHelper.print("26 Radix: "+app.hongs.util.Num.to26Radix(num));
  }

  private static class TestSerially extends CoreSerially {
    java.util.List a;
    java.util.Set b;
    java.util.Map c;

    public TestSerially() throws HongsException {
      super("test_serially");
    }

    @Override
    protected void loadData() {
      a = new java.util.ArrayList();
      b = new java.util.HashSet();
      c = new java.util.HashMap();
    }
  }
}
