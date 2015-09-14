package app.hongs.serv.module;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.dl.lucene.LuceneAction;
import app.hongs.dl.lucene.LuceneRecord;

/**
 * 数据存储动作
 * @author Hongs
 */
public class DataAction extends LuceneAction {
    @Override
    public LuceneRecord getModel(ActionHelper helper)
    throws HongsException {
        ActionRunner runner = (ActionRunner) helper.getAttribute("__RUNNER__");
        String mod = runner.getClsAnn();
        String mtd = runner.getMtdAnn();
        String ent ;
        int p0  = mod.lastIndexOf('/' );
        int p1  = mtd.lastIndexOf('/' );
            mod = mod.substring(0 , p0);
        if (p1 == -1) {
            ent = mod.substring(1 + p0);
        } else {
            ent = mtd.substring(0 , p1);
        }
        return new Data(mod, ent);
    }
}
