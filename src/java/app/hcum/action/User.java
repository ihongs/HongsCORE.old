package app.hcum.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.CommitSuccess;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户动作接口
 * @author Hongs
 */
public class User {

    private app.hcum.model.User model;

    public User() {
        model = (app.hcum.model.User)
                Core.getInstance(app.hcum.model.User.class);
    }

    public void actionList(ActionHelper helper)
    throws HongsException {
        Map data = model.getPage(helper.getRequestData());

        // Remove the password field, don't show password in page
        List<Map> list = (List)data.get("list");
        for (Map  info :  list) {
            info.remove("password");
        }

        helper.back(data);
    }

    public void actionInfo(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());

        // Remove the password field, don't show password in page
        Map info = (Map)data.get("info");
        info.remove("password");

        helper.back(data);
    }

    @CommitSuccess
    public void actionSave(ActionHelper helper)
    throws HongsException {app.hongs.util.JSON.print(helper.getRequestData());
        String id = model.save(helper.getRequestData());
        helper.back(id);
    }

    @CommitSuccess
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
        List pageGroups = app.hcum.model.User.getPageGroups("default");
        data.put("pageGroups", pageGroups);

        // 用户动作分组
        String id = helper.getParameter("id");
        if (id != null) {
            Set userGroups = model.getGroups(id);
            data.put("userGroups", userGroups);
        }

        helper.back(data);
    }
}
