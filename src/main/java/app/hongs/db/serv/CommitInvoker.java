package app.hongs.db.serv;

import app.hongs.action.ActionRunner;
import app.hongs.Core;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.ActionInvoker;
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
    public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno)
    throws HongsException {
        Core core = Core.getInstance();
        String dc = DB.class.getName();
        core.put("__IN_TRANSC_MODE__" , true);
        try {
            for (String bc :  core.keySet( )) {
                if (bc.startsWith(dc)) {
                    DB  db = (DB)core.get(dc);
                    db.IN_TRANSC_MODE = true ;
                }
            }

            chains.doAction();

            try {
                for (String bc :  core.keySet( )) {
                    if (bc.startsWith(dc)) {
                        DB  db = (DB)core.get(bc);
                        db.commit(  );
                    }
                }
            } catch (HongsError ex) {
                for (String bc :  core.keySet( )) {
                    if (bc.startsWith(dc)) {
                        DB  db = (DB)core.get(bc);
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
