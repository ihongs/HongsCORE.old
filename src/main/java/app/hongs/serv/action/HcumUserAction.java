package app.hongs.serv.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.annotation.CommitSuccess;
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

    private app.hongs.serv.HcumUser model;
    private CoreLanguage lang;

    public HcumUserAction() {
        model = (app.hongs.serv.HcumUser)
                Core.getInstance(app.hongs.serv.HcumUser.class);
        lang  = (CoreLanguage)
                Core.getInstance(CoreLanguage.class);
        lang.load("hcum");
    }

    public void action_list(ActionHelper helper)
    throws HongsException {
        Map data = model.getPage(helper.getRequestData());

        // Remove the password field, don't show password in page
        List<Map> list = (List)data.get("list");
        for (Map  info :  list) {
            info.remove("password");
        }

        helper.reply(data);
    }

    public void action_info(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());

        // Remove the password field, don't show password in page
        Map info = (Map)data.get("info");
        info.remove("password");

        helper.reply(data);
    }

    public void action_save(ActionHelper helper)
    throws HongsException {app.hongs.util.Data.dumps(helper.getRequestData());
        String id = model.save(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.save.user.success", nms);

        helper.reply(msg, id, nms);
    }

    @CommitSuccess
    public void action_remove(ActionHelper helper)
    throws HongsException {
        int num = model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.remove.user.success", nms);

        helper.reply(msg);
    }

    public void action_unique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.reply(rst);
    }

    public void action_groups(ActionHelper helper)
    throws HongsException {
        Map data = new HashMap();

        // 全部权限分组
        List pageGroups = app.hongs.serv.HcumUser.getPageGroups("default");
        data.put("pageGroups", pageGroups);

        // 用户动作分组
        String id = helper.getParameter("id");
        if (id != null) {
            Set userGroups = model.getGroups(id);
            data.put("userGroups", userGroups);
        }

        helper.reply(data);
    }
}
