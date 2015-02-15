package app.hongs.cmdlet.serv;

import app.hongs.Core;
import app.hongs.action.ActionRunner;
import app.hongs.cmdlet.CmdletRunner;
import app.hongs.cmdlet.anno.Cmdlet;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * HTTP 服务器
 * @author Hongs
 */
@Cmdlet("common")
public class Common {

    @Cmdlet("make-uid")
    public static void makeUID(String[] args) {
      System.out.println("UID: "+Core.getUniqueId());
    }

    @Cmdlet("show-env")
    public static void showENV(String[] args) {
      Map<String, String> env = new TreeMap(new PropComparator());
      env.putAll(new HashMap(System.getenv()));
      System.out.println("ENV:");
      for (Map.Entry<String, String> et : env.entrySet()) {
        String k = et.getKey(  );
        String v = et.getValue();
        System.out.println("  "+k+"\t"+v);
      }
    }

    @Cmdlet("show-properties")
    public static void showProperties(String[] args) {
      Map<String, String> env = new TreeMap(new PropComparator());
      env.putAll(new HashMap(System.getProperties()));
      System.out.println("Properties:");
      for (Map.Entry<String, String> et : env.entrySet()) {
        String k = et.getKey(  );
        String v = et.getValue();
        System.out.println("  "+k+"\t"+v);
      }
    }

    @Cmdlet("show-actions")
    public static void showActions(String[] args) {
      Map<String, Method> actions = new TreeMap(new PropComparator());
      actions.putAll(ActionRunner.getActions());
      System.out.println("Actions:");
      for (Map.Entry<String, Method> et : actions.entrySet()) {
        String a = et.getKey(  );
        Method m = et.getValue();
        System.out.println("  "+a+"\t"+m.getDeclaringClass().getName()+"."+m.getName());
      }
    }

    @Cmdlet("show-cmdlets")
    public static void showCmdlets(String[] args) {
      Map<String, Method> actions = new TreeMap(new PropComparator());
      actions.putAll(CmdletRunner.getCmdlets());
      System.out.println("Cmdlets:");
      for (Map.Entry<String, Method> et : actions.entrySet()) {
        String a = et.getKey(  );
        Method m = et.getValue();
        System.out.println("  "+a+"\t"+m.getDeclaringClass().getName()+"."+m.getName());
      }
    }

    private static class PropComparator implements Comparator<String> {
      @Override
      public int compare(String s1, String s2) {
        return s1.compareTo(s2);
      }
    }

}
