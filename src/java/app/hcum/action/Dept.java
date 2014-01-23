package app.hcum.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.CommitSuccess;
import app.hcum.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门动作接口
 * @author Hongs
 */
public class Dept {

    private app.hcum.model.Dept model;

    public Dept() {
        model = (app.hcum.model.Dept)
                Core.getInstance(app.hcum.model.Dept.class);
    }

    public void actionTree(ActionHelper helper)
    throws HongsException {
        Map data = model.getTree(helper.getRequestData());
        helper.back(data);
    }

    public void actionInfo(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());
        helper.back(data);
    }

    @CommitSuccess
    public void actionSave(ActionHelper helper)
    throws HongsException {
        String id = model.save(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = "保存部门 "+nms+" 成功";

        helper.back(msg, id, nms);
    }

    @CommitSuccess
    public void actionRemove(ActionHelper helper)
    throws HongsException {
        int num = model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = "删除部门 "+nms+" 成功";

        helper.back(msg);
    }

    public void actionExists(ActionHelper helper)
    throws HongsException {
        boolean rst = model.exists(helper.getRequestData());
        helper.back(rst);
    }

    public void actionGroups(ActionHelper helper)
    throws HongsException {
        Map data = new HashMap();

        // 全部权限分组
        List pageGroups = User.getPageGroups("default");
        data.put("pageGroups", pageGroups);

        // 用户动作分组
        String id = helper.getParameter("id");
        if (id != null) {
            app.hcum.model.Dept model2 = (app.hcum.model.Dept)
                Core.getInstance(app.hcum.model.Dept.class);
            Set deptGroups = model2.getGroups(id);
            data.put("userGroups", deptGroups);
        }

        helper.back(data);
    }
}
