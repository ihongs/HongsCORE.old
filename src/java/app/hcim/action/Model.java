package app.hcim.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.CommitSuccess;
import java.util.Map;

/**
 * 模型动作接口
 * @author Hongs
 */
public class Model {

    private app.hcim.model.Model model;

    public Model() {
        model = (app.hcim.model.Model)
                Core.getInstance(app.hcim.model.Model.class);
    }

    public void actionList(ActionHelper helper)
    throws HongsException {
        Map data = model.getPage(helper.getRequestData());
        helper.back(data);
    }

    public void actionInfo(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());
        helper.back(data);
    }

    @CommitSuccess
    public void actionSave(ActionHelper helper)
    throws HongsException {app.hongs.util.JSON.print(helper.getRequestData());
        String id = model.save(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = "保存模型 "+nms+" 成功";

        helper.back(id, msg);
    }

    @CommitSuccess
    public void actionRemove(ActionHelper helper)
    throws HongsException {
        int num = model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = "删除模型 "+nms+" 成功";

        helper.back(num, msg);
    }

    public void actionUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.back(rst);
    }

}
