package app.hongs.action.annotation;

import app.hongs.Core;
import app.hongs.action.ActionHelper;
import app.hongs.db.DB;
import java.lang.annotation.Annotation;
import java.sql.Connection;

/**
 * 操作成功才提交数据更改
 *
 * 由Action.doAction自动调用
 *
 * @author Hongs
 */
public class CommitInvoker {
    public static void invoke(ActionHelper helper, ActionChain chain, Annotation anno)
    throws Throwable {
        Core core = Core.getInstance();
        try {
            core.put("__DB_AUTO_COMMIT__", false);

            chain.doAction();

            commit();
        }
        catch (Throwable ex) {
            rollback();
            throw ex;
        }
        finally {
            core.remove("__DB_AUTO_COMMIT__");
        }
    }

    public static void commit()
    throws Exception {
        try {
            Core core = Core.getInstance(  );
            for (String k  :  core.keySet()) {
                if (k.startsWith("__DB__.")) {
                    DB  db = (DB)core.get(k);
                    Connection con = db.connect();
                    if (con.getAutoCommit( ) == false ) {
                        con.commit();
                    }
                }
            }
        }
        catch (Exception ex) {
            rollback();
            throw ex;
        }
        catch (Error ex) {
            rollback();
            throw ex;
        }
    }

    public static void rollback()
    throws Exception {
        try {
            Core core = Core.getInstance(  );
            for (String k  :  core.keySet()) {
                if (k.startsWith("__DB__.")) {
                    DB  db = (DB)core.get(k);
                    Connection con = db.connect();
                    if (con.getAutoCommit( ) == false ) {
                        con.rollback();
                    }
                }
            }
        }
        catch (Exception ex) {
            throw ex;
        }
        catch (Error ex) {
            throw ex;
        }
    }
}
