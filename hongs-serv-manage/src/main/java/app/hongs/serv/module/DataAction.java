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
        String mod = runner.getAction(), ent; int pos;
        mod  = mod.substring(0, mod.length() - runner.getMtdAnn().length() -1);
        pos  = mod.lastIndexOf('/' );
        ent  = mod.substring(pos +1);
        mod  = mod.substring(0, pos);
        return new Data(mod, ent);
    }
}
