package app.hongs.action.annotation;

import app.hongs.Core;
import app.hongs.action.ActionHelper;
import app.hongs.db.DB;
import java.lang.annotation.Annotation;

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
            core.put("__IN_TRANSC_MODE__", null);
            for (String k  :  core.keySet()) {
                if (k.startsWith("__DB__.")) {
                    DB  db = (DB)core.get(k);
                    db.IN_TRANSC_MODE = true;
                }
            }

            chain.doAction();

            commit();
        }
        finally {
            core.remove("__IN_TRANSC_MODE__");
        }
    }

    public static void commit()
    throws Exception {
        try {
            Core core = Core.getInstance(  );
            for (String k  :  core.keySet()) {
                if (k.startsWith("__DB__.")) {
                    DB  db = (DB)core.get(k);
                    db.commit();
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
                    db.rollback();
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
