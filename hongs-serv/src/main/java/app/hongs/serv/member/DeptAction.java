package app.hongs.serv.member;

import app.hongs.Core;
import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.anno.Action;
import app.hongs.db.DB;
import app.hongs.action.anno.CommitSuccess;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门动作接口
 * @author Hongs
 */
@Action("admin/member/dept")
public class DeptAction {

    private app.hongs.serv.member.Dept model;

    public DeptAction()
    throws HongsException {
        model = (Dept) DB.getInstance("member").getModel("dept");
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
        Map data = helper.getRequestData( );
        CoreLocale lang = CoreLocale.getInstance().clone( );
        lang.load("member");
        
        String  id  = model.save(data);
        String  msg = lang.translate("core.save.dept.success");
        
        Map info = new HashMap();
        info.put( "id" , id);
        info.put("name", data.get("name"));
        helper.reply(msg , info);
    }

    @Action("delete")
    @CommitSuccess
    public void doDelete(ActionHelper helper)
    throws HongsException {
        CoreLocale lang = CoreLocale.getInstance().clone( );
        lang.load("member");
        int     rd  = model.delete(helper.getRequestData());
        String  msg = lang.translate("core.delete.dept.success", Integer.toString(rd));
        helper.reply(msg, rd);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        boolean v = model.exists(helper.getRequestData());
        Map rst = new HashMap();
        rst.put("valid" , v);
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
            app.hongs.serv.member.Dept model2 = Core.getInstance(app.hongs.serv.member.Dept.class);
            Set rolez = model2.getRoles(id);
            data.put("roles", rolez);
        }

        helper.reply(data);
    }
}
