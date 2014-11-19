package app.hongs.serv.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.annotation.CommitSuccess;
import app.hongs.serv.HcumUser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门动作接口
 * @author Hongs
 */
@Action("hcum/dept")
public class HcumDeptAction {

    private app.hongs.serv.HcumDept model;
    private CoreLanguage lang;

    public HcumDeptAction() {
        model = (app.hongs.serv.HcumDept)
                Core.getInstance(app.hongs.serv.HcumDept.class);
        lang  = (CoreLanguage)
                Core.getInstance(CoreLanguage.class);
        lang.load("hcum");
    }

    public void action_tree(ActionHelper helper)
    throws HongsException {
        Map data = model.getTree(helper.getRequestData());
        helper.reply(data);
    }

    public void action_info(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());
        helper.reply(data);
    }

    @CommitSuccess
    public void action_save(ActionHelper helper)
    throws HongsException {
        String id = model.save(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.svae.dept.success", nms);

        helper.reply(msg, id, nms);
    }

    @CommitSuccess
    public void action_remove(ActionHelper helper)
    throws HongsException {
        int num = model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.remove.dept.success", nms);

        helper.reply(msg);
    }

    public void action_exists(ActionHelper helper)
    throws HongsException {
        boolean rst = model.exists(helper.getRequestData());
        helper.reply(rst);
    }

    public void action_groups(ActionHelper helper)
    throws HongsException {
        Map data = new HashMap();

        // 全部权限分组
        List pageGroups = HcumUser.getPageGroups("default");
        data.put("pageGroups", pageGroups);

        // 用户动作分组
        String id = helper.getParameter("id");
        if (id != null) {
            app.hongs.serv.HcumDept model2 = (app.hongs.serv.HcumDept)
                Core.getInstance(app.hongs.serv.HcumDept.class);
            Set deptGroups = model2.getGroups(id);
            data.put("userGroups", deptGroups);
        }

        helper.reply(data);
    }
}
