package app.hongs.serv;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.cmdlet.CmdletRunner;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 通用命令
 * @author Hongs
 */
@Cmdlet("common")
public class CommonCmdlet {

    @Cmdlet("make-uid")
    public static void makeUID(String[] args) {
        System.out.println(Core.getUniqueId());
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
            System.err.println("Action name required!\r\nUsage: ACTION_NAME --request-- URL_QUERY_STRING --context-- XXX --session XXX");
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
