package net.hongs.search.serv;

import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import static app.hongs.action.ActionWarder.PATH;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.Supply;
import app.hongs.action.anno.Verify;
import app.hongs.dh.IAction;
import java.util.Map;

/**
 * 搜索动作
 * @author Hongs
 */
@Action("search")
public class SearchAction implements IAction {

    @Action("retrieve")
    @Supply()
    public void retrieve(ActionHelper helper) throws HongsException {
        SearchRecord sr = getModel(helper);
        Map rd = helper.getRequestData();
        Map sd = sr.retrieve(rd);
        sr.destroy();
        helper.reply(sd);
    }

    @Action("counts/retrieve")
    public void counts(ActionHelper helper) throws HongsException {
        SearchRecord sr = getModel(helper);
        Map rd = helper.getRequestData();
        Map sd = sr.counts(rd);
        sr.destroy();
        helper.reply(sd);
    }

    @Action("create")
    @Verify()
    public void create(ActionHelper helper) throws HongsException {
        SearchRecord sr = getModel(helper);
        Map rd = helper.getRequestData();
        int sn = sr.upsert(rd);
        sr.destroy();
        helper.reply(CoreLanguage.getInstance().translate("core.update.success", "索引", Integer.toString(sn)));
    }

    @Action("delete")
    public void delete(ActionHelper helper) throws HongsException {
        SearchRecord sr = getModel(helper);
        Map rd = helper.getRequestData();
        int sn = sr.delete(rd);
        sr.destroy();
        helper.reply(CoreLanguage.getInstance().translate("core.delete.success", "索引", Integer.toString(sn)));
    }

    public void update(ActionHelper helper) throws HongsException {
        throw new HongsException(HongsException.NOTICE, "Not supported yet.");
    }

    public SearchRecord getModel(ActionHelper helper)
    throws HongsException {
        ActionRunner runner = (ActionRunner) helper.getAttribute("__RUNNER__");
        String act = runner.getAction();
        act  = act.substring(0, act.length() - runner.getMtdAnn().length() -1);
        act  = act.substring(act.lastIndexOf('/') + 1);
        return new SearchRecord ( act );
    }
    
}
