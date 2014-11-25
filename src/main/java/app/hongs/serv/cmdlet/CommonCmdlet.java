package app.hongs.serv.cmdlet;

import app.hongs.Core;
import app.hongs.CoreSerially;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.cmdlet.CmdletHelper;
import app.hongs.annotation.Cmdlet;
import app.hongs.db.DB;
import app.hongs.db.Table;
import app.hongs.db.sync.TableSync;
import app.hongs.util.Data;
import app.hongs.util.Util;
import java.util.List;
import java.util.Map;

/**
 * 测试
 * @author Hongs
 */
@Cmdlet("common")
public class CommonCmdlet {

  public void action(ActionHelper helper)
          throws HongsException {
    helper.print("HELLO!!!");
  }

  public static void cmdlet(String[] args)
          throws app.hongs.HongsException {

    /*
    Map vars = new java.util.HashMap();
    vars.put("var",  "var1");
    vars.put("var.2", "var2");
    System.out.println(app.hongs.util.Util.inject("This is $var and that is ${var.2}.", vars));
    System.out.println(app.hongs.util.Util.inject("This is $0 and that is ${1}2.", "name", "var"));
    */
      
    //app.hongs.util.Data.dumps(args);
    Map<String, Object> opts = CmdletHelper.getOpts(args,
      "show-env:b",
      "show-properties:b",
      "test-text:b",
      "test-rate:b",
      "test-left:b",
      "test-opts:b",
      "test-radix:i",
      "test-inject:b",
      "test-serially:b",
      "sync-table:s",
      "?Usage: --test-rate --tset-left");
    //app.hongs.util.Data.dumps(opts);

    if (opts.containsKey("show-env")
    && (Boolean)opts.get("show-env")) {
      Data.dumps(System.getenv());
    }

    if (opts.containsKey("show-properties")
    && (Boolean)opts.get("show-properties")) {
      Data.dumps(System.getProperties());
    }

    if (opts.containsKey("test-text")
    && (Boolean)opts.get("test-text")) {
      testText();
    }

    if (opts.containsKey("test-rate")
    && (Boolean)opts.get("test-rate")) {
      testRate();
    }

    if (opts.containsKey("test-left")
    && (Boolean)opts.get("test-left")) {
      testLeft();
    }

    if (opts.containsKey("test-opts")
    && (Boolean)opts.get("test-opts")) {
      testOpts(args);
    }

    if (opts.containsKey("test-radix")) {
      testRadix((Long)opts.get("test-radix"));
    }
    
    if (opts.containsKey("test-serially")
    && (Boolean)opts.get("test-serially")) {
      new TestSerially();
    }
    
    if (opts.containsKey("sync-table")) {
      diffTable((String)opts.get("sync-table"));
    }

    if (opts.containsKey("make-demo-data")
    && (Boolean)opts.get("make-demo-data")) {
      makeDemoData();
    }
  }

  private static void diffTable(String tables) throws HongsException {
      String[] ts = tables.split(":");
      String srcTableName = ts[0];
      String dstTableName = ts[1];
      DB db = (DB)Core.getInstance(app.hongs.db.DB.class);
      Table srcTable = Table.getInstanceByTableName(db, srcTableName);
      Table dstTable = Table.getInstanceByTableName(db, dstTableName);
      TableSync sync = new TableSync(srcTable);
      List<String> sqls = sync.syncSlaverSqls(dstTable, true);
      for (String sql : sqls) {
          System.out.println(sql);
      }
  }
  
  private static void testText() {
    String s = "This is Hong's Framework. Ask: \\\"Who ar you?\"\r\n"
             + "这是弘的框架, 问: \\\"你是谁?\"\r\n"
             + "%abc,$def.";
    CmdletHelper.println("actual: " + s);
    String s1 = Util.escape(s);
    CmdletHelper.println("escape: " + s1);
    String s2 = Util.resume(s1);
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

  private static void testOpts(String[] args) throws HongsException {
    app.hongs.util.Data.dumps(args);
    Map opts = CmdletHelper.getOpts(args,
      "opt_s|opt-s:s", "opt_i|opt-i:i", "opt_f|opt-f:f", "opt_b|opt-b:b",
      "opt_o|opt-o=s", "opt_m|opt-m+s", "opt_n|opt-n*s", "opt_r|opt-r=/(a|b)/i",
      "!U", "!V", "?Useage:\ncmd opt-o xxx opt-m xxx... [opt-n xxx...]"
    );
    app.hongs.util.Data.dumps(opts);
  }
  
  private static void testRadix(long num) {
    CmdletHelper.println("36 Radix: "+app.hongs.util.Util.as36Hex(num));
    CmdletHelper.println("26 Radix: "+app.hongs.util.Util.as26Hex(num));
  }

  private static void makeDemoData() {
    
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
