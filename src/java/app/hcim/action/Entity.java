package app.hcim.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.Action;
import app.hongs.action.annotation.CommitSuccess;
import app.hongs.action.annotation.InForm;
import app.hongs.action.annotation.InList;
import app.hongs.db.FetchCase;
import app.hongs.util.Tree;
import java.util.List;
import java.util.Map;

/**
 * 模型动作接口
 * @author Hongs
 */
@Action
public class Entity {

    private app.hcim.model.Entity model;

    public Entity() {
        model = (app.hcim.model.Entity)
                Core.getInstance(app.hcim.model.Entity.class);
    }

    public void actionList(ActionHelper helper)
    throws HongsException {
        Map data = model.getPage(helper.getRequestData());
        helper.back(data);
    }

    public void actionInfo(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());
        
        // 加入选择的模块
        FetchCase fc = new FetchCase();
        fc.select(".id, .name")
          .where (".id = ?", Tree.getValue(data, "info.module_id"));
        Map dd = model.db.getTable("a_hcim_module").fetchLess(fc);
        Tree.setValue(data, "data.module_id.", dd);
        
        helper.back(data);
    }

    @CommitSuccess
    public void actionSave(ActionHelper helper)
    throws HongsException {app.hongs.util.JSON.dumps(helper.getRequestData());
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
