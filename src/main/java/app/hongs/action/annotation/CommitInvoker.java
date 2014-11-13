package app.hongs.action.annotation;

import app.hongs.action.ActionChains;
import app.hongs.Core;
import app.hongs.HongsError;
import app.hongs.HongsException;
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
public class CommitInvoker implements ActionInvoker {
    @Override
    public void invoke(ActionHelper helper, ActionChains chains, Annotation anno)
    throws HongsException {
        Core core = Core.getInstance();
        core.put("__IN_TRANSC_MODE__", true);
        try {
            for (String k  :  core.keySet()) {
                if (k.startsWith("__DB__.")) {
                    DB db = (DB) core.get(k);
                    db.IN_TRANSC_MODE = true;
                }
            }

            chains.doAction();

            try {
                for (String k  :  core.keySet()) {
                    if (k.startsWith("__DB__.")) {
                        DB  db = (DB)core.get(k);
                        db.commit(  );
                    }
                }
            } catch (HongsError ex) {
                for (String k  :  core.keySet()) {
                    if (k.startsWith("__DB__.")) {
                        DB  db = (DB)core.get(k);
                        db.rollback();
                    }
                }
                throw ex;
            }
        } finally {
            core.remove("__IN_TRANSC_MODE__");
        }
    }
}
