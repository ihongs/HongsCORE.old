package net.hongs.search.serv;

import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.Supply;
import app.hongs.action.anno.Verify;
import app.hongs.dl.lucene.LuceneAction;
import java.util.Map;

/**
 * 搜索动作
 * @author Hongs
 */
@Action("search")
public class SearchAction extends LuceneAction {

    @Action("retrieve")
    @Supply()
    @Override
    public void retrieve(ActionHelper helper) throws HongsException {
        super.retrieve(helper);
    }

    @Action("counts/retrieve")
    public void counts(ActionHelper helper) throws HongsException {
        Search sr = getModel(helper);
        Map rd = helper.getRequestData();
        Map sd = sr.counts(rd);
        sr.destroy();
        helper.reply(sd);
    }

    @Action("create")
    @Verify()
    @Override
    public void create(ActionHelper helper) throws HongsException {
        Search sr = getModel(helper);
        Map rd = helper.getRequestData();
        int sn = sr.upsert(rd);
        sr.destroy();
        helper.reply(CoreLanguage.getInstance().translate("core.update.success", Integer.toString(sn)));
    }

    @Override
    public void update(ActionHelper helper) throws HongsException {
        throw new HongsException(HongsException.NOTICE, "Not supported yet.");
    }

    @Override
    public Search getModel(ActionHelper helper)
    throws HongsException {
        ActionRunner runner = (ActionRunner) helper.getAttribute("__RUNNER__");
        String ent = runner.getAction();
        ent  = ent.substring(0, ent.length() - runner.getMtdAnn().length() -1);
        int    pos = ent.lastIndexOf('/');
        String mod = ent.substring(0,pos);
               ent = ent.substring(pos+1);
        return new Search(mod, ent);
    }
    
}
