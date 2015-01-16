package net.hongs.search.serv;

import app.hongs.CoreConfig;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.Filter;
import app.hongs.action.anno.FilterInvoker;
import app.hongs.action.anno.Supply;
import app.hongs.action.anno.Verify;
import static app.hongs.action.serv.ServWarder.ENTITY;
import static app.hongs.action.serv.ServWarder.MODULE;
import app.hongs.dh.IAction;
import app.hongs.util.Synt;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 搜索动作
 * @author Hongs
 */
@Action("search")
public class SearchAction implements IAction {

    @Action("retrieve")
    @Filter(MyFilter.class)
    @Supply()
    public void retrieve(ActionHelper helper) throws HongsException {
        SearchRecord sr = new SearchRecord((String) helper.getAttribute(ENTITY));
        Map rd = helper.getRequestData();
        Map sd = sr.retrieve(rd);
        sr.destroy();
        helper.reply(sd);
    }

    @Action("counts/retrieve")
    @Filter(MyFilter.class)
    public void counts(ActionHelper helper) throws HongsException {
        SearchRecord sr = new SearchRecord((String) helper.getAttribute(ENTITY));
        Map rd = helper.getRequestData();
        Map sd = sr.counts(rd);
        sr.destroy();
        helper.reply(sd);
    }

    @Action("create")
    @Filter(MyFilter.class)
    @Verify()
    public void create(ActionHelper helper) throws HongsException {
        SearchRecord sr = new SearchRecord((String) helper.getAttribute(ENTITY));
        Map rd = helper.getRequestData();
        int sn = sr.upsert(rd);
        sr.destroy();
        helper.reply(CoreLanguage.getInstance().translate("core.update.success", "索引", Integer.toString(sn)));
    }

    @Action("delete")
    @Filter(MyFilter.class)
    public void delete(ActionHelper helper) throws HongsException {
        SearchRecord sr = new SearchRecord((String) helper.getAttribute(ENTITY));
        Map rd = helper.getRequestData();
        int sn = sr.delete(rd);
        sr.destroy();
        helper.reply(CoreLanguage.getInstance().translate("core.delete.success", "索引", Integer.toString(sn)));
    }

    public void update(ActionHelper helper) throws HongsException {
        throw new HongsException(HongsException.NOTICE, "Not supported yet.");
    }

    public static class MyFilter implements FilterInvoker {

        public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno) throws HongsException {
            String sc = Synt.declare(helper.getParameter("ss"), String.class);
            if (sc == null) {
                sc = CoreConfig.getInstance("search").getProperty("core.search.scheme.default", "base");
            }
            helper.setAttribute(MODULE, "search");
            helper.setAttribute(ENTITY, sc);
            chains.doAction();
        }

    }

}
