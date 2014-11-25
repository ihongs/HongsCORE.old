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
        String  id  = model.save(helper.getRequestData());
        String  msg = lang.translate("core.save.entity.success");
        helper.reply(msg, id);
    }

    @Action("delete")
    @CommitSuccess
    public void doRemove(ActionHelper helper)
    throws HongsException {
        int     rd  = model.delete(helper.getRequestData());
        String  msg = lang.translate("core.remove.entity.success", Integer.toString(rd));
        helper.reply(msg);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.exists(helper.getRequestData());
        helper.reply(rst);
    }

}
