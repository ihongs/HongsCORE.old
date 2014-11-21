package app.hongs.serv.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.annotation.CommitSuccess;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模型动作接口
 * @author Hongs
 */
@Action("hcim/entity")
public class HcimEntityAction {

    private final app.hongs.serv.HcimEntity model;
    private final CoreLanguage lang;

    public HcimEntityAction() {
        model = (app.hongs.serv.HcimEntity)
                Core.getInstance(app.hongs.serv.HcimEntity.class);
        lang  = (CoreLanguage)
                Core.getInstance(CoreLanguage.class);
        lang.load("hcim");
    }

    @Action("list")
    public void getList(ActionHelper helper)
    throws HongsException {
        Map data = model.getPage(helper.getRequestData());
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
        String msg = lang.translate("core.save.entity.success", nms);

        helper.reply(msg, id, nms);
    }

    @Action("remove")
    @CommitSuccess
    public void doRemove(ActionHelper helper)
    throws HongsException {
        model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.remove.entity.success", nms);

        helper.reply(msg);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.reply(rst);
    }

}
