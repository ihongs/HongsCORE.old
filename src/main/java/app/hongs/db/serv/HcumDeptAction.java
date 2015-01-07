package app.hongs.db.serv;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotaion.Action;
import app.hongs.db.DB;
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

    private app.hongs.db.serv.HcumDept model;
    private CoreLanguage lang;

    public HcumDeptAction()
    throws HongsException {
        model = (HcumDept) DB.getInstance("hcum").getModel("dept");
        lang  = CoreLanguage.getInstance().clone();
        lang.load("hcum");
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
    @CommitSuccess
    public void doSave(ActionHelper helper)
    throws HongsException {
        String  id  = model.save(helper.getRequestData());
        String  msg = lang.translate("core.save.dept.success");
        helper.reply(msg, id, helper.getRequestData().get("name"));
    }

    @Action("delete")
    @CommitSuccess
    public void doDelete(ActionHelper helper)
    throws HongsException {
        int     rd  = model.delete(helper.getRequestData());
        String  msg = lang.translate("core.delete.dept.success", Integer.toString(rd));
        helper.reply(msg);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.exists(helper.getRequestData());
        helper.reply(null, rst);
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
            app.hongs.db.serv.HcumDept model2 = (app.hongs.db.serv.HcumDept)
                Core.getInstance(app.hongs.db.serv.HcumDept.class);
            Set deptGroups = model2.getGroups(id);
            data.put("userGroups", deptGroups);
        }

        helper.reply(data);
    }
}
