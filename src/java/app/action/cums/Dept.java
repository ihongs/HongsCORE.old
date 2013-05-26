package app.action.cums;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.model.cums.DeptBaseInfo;
import app.model.cums.DeptBaseTree;
import app.model.cums.UserBaseInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门动作接口
 * @author Hongs
 */
public class Dept {

    private DeptBaseTree model;

    public Dept() {
        model = (DeptBaseTree)Core.getInstance("app.model.cums.DeptBaseTree");
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

    public void actionSave(ActionHelper helper)
    throws HongsException {
        String id = model.save(helper.getRequestData());
        helper.back(id);
    }

    public void actionRemove(ActionHelper helper)
    throws HongsException {
        int num = model.remove(helper.getRequestData());
        helper.back(num);
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
        List pageGroups = UserBaseInfo.getPageGroups("default");
        data.put("pageGroups", pageGroups);

        // 用户动作分组
        String id = helper.getParameter("id");
        if (id != null) {
            DeptBaseInfo model2 = (DeptBaseInfo)
                Core.getInstance("app.model.cums.DeptBaseInfo");
            Set deptGroups = model2.getGroups(id);
            data.put("userGroups", deptGroups);
        }

        helper.back(data);
    }
}
