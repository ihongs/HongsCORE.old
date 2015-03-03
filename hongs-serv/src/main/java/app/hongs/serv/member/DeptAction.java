package app.hongs.serv.member;

import app.hongs.dl.anno.CommitSuccess;
import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.anno.Action;
import app.hongs.db.DB;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门动作接口
 * @author Hongs
 */
@Action("hongs/member/dept")
public class DeptAction {

    private app.hongs.serv.member.Dept model;
    private CoreLanguage lang;

    public DeptAction()
    throws HongsException {
        model = (Dept) DB.getInstance("member").getModel("dept");
        lang  = CoreLanguage.getInstance().clone();
        lang.load("member");
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
        helper.reply(rst);
    }

    @Action("roles")
    public void getRoles(ActionHelper helper)
    throws HongsException {
        Map data = new HashMap();

        // 全部权限分组
        List roles = Sign.getRoles("default");
        data.put("role_list", roles);

        // 用户动作分组
        String id = helper.getParameter("id");
        if (id != null) {
            app.hongs.serv.member.Dept model2 = (app.hongs.serv.member.Dept)
                Core.getInstance(app.hongs.serv.member.Dept.class);
            Set rolez = model2.getRoles(id);
            data.put("roles", rolez);
        }

        helper.reply(data);
    }
}
