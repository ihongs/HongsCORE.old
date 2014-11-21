package app.hongs.serv.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.annotation.CommitSuccess;
import java.util.Map;

/**
 * 模型动作接口
 * @author Hongs
 */
@Action("hcim/module")
public class HcimModuleAction {

    private final app.hongs.serv.HcimModule model;
    private final CoreLanguage lang;

    public HcimModuleAction() {
        model = (app.hongs.serv.HcimModule)
                Core.getInstance(app.hongs.serv.HcimModule.class);
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
    throws HongsException {app.hongs.util.Data.dumps(helper.getRequestData());
        String id = model.save(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.save.module.success", nms);

        helper.reply(msg, id, nms);
    }

    @Action("remove")
    @CommitSuccess
    public void doRemove(ActionHelper helper)
    throws HongsException {
        model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.remove.module.success", nms);

        helper.reply(msg);
    }

    @Action("uniuqe")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.reply(rst);
    }

}
