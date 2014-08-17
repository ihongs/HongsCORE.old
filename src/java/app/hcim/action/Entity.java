package app.hcim.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.Action;
import app.hongs.action.annotation.CommitSuccess;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模型动作接口
 * @author Hongs
 */
@Action
public class Entity {

    private final app.hcim.model.Entity model;

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
        helper.back(data);
    }

    @CommitSuccess
    public void actionSave(ActionHelper helper)
    throws HongsException {
        Map data = helper.getRequestData();
        if (data.containsKey("a_hcim_entity_cols")) {
            List<Map<String, String>> cols = (List<Map<String, String>>)
                new ArrayList(((Map)data.get("a_hcim_entity_cols")).values());
            int i = 0;
            for (Map col : cols) {
                col.put("serialno", i++);
            }
        }
        if (data.containsKey("a_hcim_entity_rels")) {
            List<Map<String, String>> cols = (List<Map<String, String>>)
                new ArrayList(((Map)data.get("a_hcim_entity_rels")).values());
            int i = 0;
            for (Map col : cols) {
                col.put("serialno", i++);
            }
        }
        
        String id = model.save(data);

        String nms = model.getAffectedNames();
        String msg = "保存实体 "+nms+" 成功";

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
