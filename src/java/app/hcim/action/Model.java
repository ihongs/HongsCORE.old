package app.hcim.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.CommitSuccess;
import app.hongs.action.annotation.InForm;
import app.hongs.action.annotation.InList;
import java.util.List;
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

    @InList(conf="hcim", keys={"MODEL_VALUE_TYPES"})
    public void actionList(ActionHelper helper)
    throws HongsException {
        Map data = model.getPage(helper.getRequestData());
        helper.back(data);
    }

    @InForm(conf="hcim", keys={"MODEL_VALUE_TYPES"})
    public void actionInfo(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());
        helper.back(data);
    }

    @CommitSuccess
    public void actionSave(ActionHelper helper)
    throws HongsException {app.hongs.util.JSON.print(helper.getRequestData());
        Map data = helper.getRequestData();
        List<Map<String, String>> cols = (List<Map<String, String>>)
            ((Map)data.get("hcrm_model_cols")).values();
        int i = 0;
        for (Map col : cols) {
            col.put("serialno", i++);
        }
        
        String id = model.save(data);

        String nms = model.getAffectedNames();
        String msg = "保存模型 "+nms+" 成功";

        helper.back(msg, id, nms);
    }

    @CommitSuccess
    public void actionRemove(ActionHelper helper)
    throws HongsException {
        model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = "删除模型 "+nms+" 成功";

        helper.back(msg);
    }

    public void actionUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.back(rst);
    }

}
