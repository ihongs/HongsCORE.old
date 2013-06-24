package app.hongs.action.annotation;

import app.hongs.Core;
import app.hongs.db.DB;
import app.hongs.action.ActionHelper;
import java.lang.annotation.Annotation;

/**
 * 操作成功才提交
 * 由Action.doAction自动调用
 * @author Hongs
 */
public class CommitInvoker {
    public static void invoke(ActionHelper helper, ActionChain chain, Annotation anno)
    throws Exception {
        Core core = Core.getInstance();
        try {
            core.put("__DB_AUTO_COMMIT__", false);

            chain.doAction();

            commit();
        }
        catch (Exception ex) {
            rollback();
            throw ex;
        }
        catch (Error ex) {
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
                    if(!db.connection.getAutoCommit())
                        db.connection.commit();
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
                    if(!db.connection.getAutoCommit())
                        db.connection.rollback();
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
