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

    @Action("list")
    public void getList(ActionHelper helper)
    throws HongsException {
        Map data = model.getTree(helper.getRequestData());
        helper.reply(data);
    }

    @Action("info")
    public void getInfo(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());
        helper.reply(data);
    }

    @Action("save")
    @CommitSuccess
    public void doSave(ActionHelper helper)
    throws HongsException {
        String id = model.save(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.svae.dept.success", nms);

        helper.reply(msg, id, nms);
    }

    @Action("remove")
    @CommitSuccess
    public void doRemove(ActionHelper helper)
    throws HongsException {
        int num = model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.remove.dept.success", nms);

        helper.reply(msg);
    }

    @Action("exists")
    public void isExists(ActionHelper helper)
    throws HongsException {
        boolean rst = model.exists(helper.getRequestData());
        helper.reply(rst);
    }

    @Action("groups")
    public void getGroups(ActionHelper helper)
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
