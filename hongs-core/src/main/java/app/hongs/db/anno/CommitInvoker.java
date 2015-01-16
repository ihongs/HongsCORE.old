package app.hongs.db.anno;

import app.hongs.Core;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.anno.FilterInvoker;
import app.hongs.db.DB;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;

/**
 * 操作成功才提交数据更改
 *
 * 由Action.doAction自动调用
 *
 * @author Hongs
 */
public class CommitInvoker implements FilterInvoker {
    @Override
    public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno)
    throws HongsException {
        Core core = Core.getInstance();
        String dc = DB.class.getName();

        try {
            core.put("__IN_TRANSC_MODE__", true);
            for(Object ot : core.entrySet()) {
                Map.Entry et = ( Map.Entry ) ot ;
                String bc = (String) et.getKey();
                if (bc.startsWith(dc)) {
                    DB db = ( DB ) et.getValue();
                    db.IN_TRANSC_MODE = true ;
                }
            }

            try {
                chains.doAction();

                for(Object ot : core.entrySet()) {
                    Map.Entry et = ( Map.Entry ) ot ;
                    String bc = (String) et.getKey();
                    if (bc.startsWith(dc)) {
                        DB db = ( DB ) et.getValue();
                        db.commit(  );
                    }
                }
            } catch (HongsError ex) {
                for(Object ot : core.entrySet()) {
                    Map.Entry et = ( Map.Entry ) ot ;
                    String bc = (String) et.getKey();
                    if (bc.startsWith(dc)) {
                        DB db = ( DB ) et.getValue();
                        db.rollback();
                    }
                }
            }
        } finally {
            core.remove("__IN_TRANSC_MODE__");
            for(Object ot : core.entrySet()) {
                Map.Entry et = ( Map.Entry ) ot ;
                String bc = (String) et.getKey();
                if (bc.startsWith(dc)) {
                    DB db = ( DB ) et.getValue();
                    db.IN_TRANSC_MODE = false;
                }
            }
        }
    }
}
