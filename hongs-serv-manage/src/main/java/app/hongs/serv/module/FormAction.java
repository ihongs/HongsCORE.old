package app.hongs.serv.module;

import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.anno.Action;
import app.hongs.db.DB;
import app.hongs.action.anno.CommitSuccess;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Hongs
 */
@Action("hongs/module/form")
public class FormAction {
    
    private app.hongs.serv.module.Form model;

    public FormAction() throws HongsException {
        model = (Form) DB.getInstance("module").getModel("form");
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
        Map info = new HashMap();
        info.put( "id" , id);
        info.put("name", data.get("name") );
        helper.reply(msg, info );
    }

    @Action("delete")
    @CommitSuccess
    public void doDelete(ActionHelper helper)
    throws HongsException {
        Map data = helper.getRequestData( );
        CoreLocale lang = CoreLocale.getInstance().clone( );
        int    rd  = model.delete(data);
        String msg = lang.translate("core.remove.success", Integer.toString(rd));
        helper.reply(msg, rd);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        boolean v = model.unique(helper.getRequestData());
        Map rst = new HashMap();
        rst.put("valid" , v);
        helper.reply(rst);
    }

}
