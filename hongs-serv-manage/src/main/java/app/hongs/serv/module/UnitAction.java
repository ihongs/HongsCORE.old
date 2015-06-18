package app.hongs.serv.module;

import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.anno.Action;
import app.hongs.db.DB;
import app.hongs.dl.anno.CommitSuccess;
import java.util.Map;

/**
 *
 * @author Hongs
 */
@Action("hongs/module/unit")
public class UnitAction {
    
    private app.hongs.serv.module.Unit model;

    public UnitAction() throws HongsException {
        model = (Unit) DB.getInstance("module").getModel("unit");
    }

    @Action("list")
    public void getList(ActionHelper helper)
    throws HongsException {
        Map data = model.getList(helper.getRequestData());
        helper.reply(data);
    }

    @Action("info")
    public void getInfo(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());
        helper.reply(data);
    }

    @Action("save")
    public void doSave(ActionHelper helper)
    throws HongsException {
        Map data = helper.getRequestData( );
        CoreLocale lang = CoreLocale.getInstance();
        String id  = model.save(data);
        String msg = lang.translate("core.save.success");
        helper.reply(msg, id, helper.getRequestData().get("name"));
    }

    @Action("delete")
    @CommitSuccess
    public void doDelete(ActionHelper helper)
    throws HongsException {
        Map data = helper.getRequestData( );
        CoreLocale lang = CoreLocale.getInstance().clone( );
        int    rd  = model.delete(data);
        String msg = lang.translate("core.remove.success", Integer.toString(rd));
        helper.reply(msg);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.reply(rst);
    }

}
