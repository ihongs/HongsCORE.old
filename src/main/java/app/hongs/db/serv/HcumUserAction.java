package app.hongs.db.serv;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.db.DB;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户动作接口
 * @author Hongs
 */
@Action("hcum/user")
public class HcumUserAction {

    private app.hongs.db.serv.HcumUser model;
    private CoreLanguage lang;

    public HcumUserAction()
    throws HongsException {
        model = (HcumUser) DB.getInstance("hcum").getModel("user");
        lang  = CoreLanguage.getInstance().clone();
        lang.load("hcum");
    }

    @Action("list")
    public void getList(ActionHelper helper)
    throws HongsException {
        Map data = model.getList(helper.getRequestData());

        // Remove the password field, don't show password in page
        List<Map> list = (List)data.get("list");
        for (Map  info :  list) {
            info.remove("password");
        }

        helper.reply(data);
    }

    @Action("info")
    public void getInfo(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());

        // Remove the password field, don't show password in page
        Map info = (Map)data.get("info");
        info.remove("password");

        helper.reply(data);
    }

    @Action("save")
    public void doSave(ActionHelper helper)
    throws HongsException {
        String  id  = model.save(helper.getRequestData());
        String  msg = lang.translate("core.save.user.success");
        helper.reply(msg, id, helper.getRequestData().get("name"));
    }

    @Action("delete")
    @CommitSuccess
    public void doDelete(ActionHelper helper)
    throws HongsException {
        int     rd  = model.delete(helper.getRequestData());
        String  msg = lang.translate("core.remove.user.success", Integer.toString(rd));
        helper.reply(msg);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.reply(null, rst);
    }

    @Action("groups")
    public void getGroups(ActionHelper helper)
    throws HongsException {
        Map data = new HashMap();

        // 全部权限分组
        List pageGroups = app.hongs.db.serv.HcumUser.getPageGroups("default");
        data.put("pageGroups", pageGroups);

        // 用户动作分组
        String id = helper.getParameter("id");
        if (id != null) {
            app.hongs.db.serv.HcumUser model2 = (app.hongs.db.serv.HcumUser)
                Core.getInstance(app.hongs.db.serv.HcumUser.class);
            Set userGroups = model2.getGroups(id);
            data.put("userGroups", userGroups);
        }

        helper.reply(data);
    }
}
