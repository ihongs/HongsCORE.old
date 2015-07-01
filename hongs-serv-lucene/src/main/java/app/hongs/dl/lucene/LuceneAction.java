package app.hongs.dl.lucene;

import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.Supply;
import app.hongs.action.anno.Verify;
import app.hongs.dl.IAction;
import app.hongs.action.anno.CommitSuccess;
import java.util.Map;

/**
 * Lucene 动作
 * @author Hongs
 */
@Action("hongs/lucene")
public class LuceneAction implements IAction {

    @Action("retrieve")
    @Supply()
    @Override
    public void retrieve(ActionHelper helper) throws HongsException {
        LuceneRecord sr = getModel(helper);
        Map rd = helper.getRequestData();
        Map sd = sr.retrieve(rd);
        sr.destroy();
        helper.reply(sd);
    }

    @Action("create")
    @Verify()
    @CommitSuccess
    @Override
    public void create(ActionHelper helper) throws HongsException {
        LuceneRecord sr = getModel(helper);
        Map rd = helper.getRequestData();
        Object[] sa = sr.create(rd);
        sr.destroy();
        CoreLocale lang = CoreLocale.getInstance();
        helper.reply(lang.translate("fore.update.success", lang.translate("fore.record")), sa);
    }

    @Action("update")
    @Verify()
    @CommitSuccess
    @Override
    public void update(ActionHelper helper) throws HongsException {
        LuceneRecord sr = getModel(helper);
        Map rd = helper.getRequestData();
        int sn = sr.update(rd);
        sr.destroy();
        CoreLocale lang = CoreLocale.getInstance();
        helper.reply(lang.translate("fore.update.success", lang.translate("fore.record"), String.valueOf(sn)));
    }

    @Action("delete")
    @CommitSuccess
    @Override
    public void delete(ActionHelper helper) throws HongsException {
        LuceneRecord sr = getModel(helper);
        Map rd = helper.getRequestData();
        int sn = sr.delete(rd);
        sr.destroy();
        CoreLocale lang = CoreLocale.getInstance();
        helper.reply(lang.translate("fore.delete.success", lang.translate("fore.record"), String.valueOf(sn)));
    }

    public LuceneRecord getModel(ActionHelper helper)
    throws HongsException {
        ActionRunner runner = (ActionRunner) helper.getAttribute("__RUNNER__");
        String mod = runner.getAction(), ent; int pos;
        mod  = mod.substring(0, mod.length() - runner.getMtdAnn().length() -1);
        pos  = mod.lastIndexOf('/' );
        ent  = mod.substring(pos +1);
        mod  = mod.substring(0, pos);
        return new LuceneRecord(mod, ent);
    }

}
