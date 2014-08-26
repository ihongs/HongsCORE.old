package app.hcim.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.Action;
import app.hongs.action.annotation.CommitSuccess;
import java.util.Map;

/**
 * 模型动作接口
 * @author Hongs
 */
@Action
public class Module {

    private app.hcim.model.Module model;

    public Module() {
        model = (app.hcim.model.Module)
                Core.getInstance(app.hcim.model.Module.class);
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
    throws HongsException {app.hongs.util.JSON.dumps(helper.getRequestData());
        String id = model.save(helper.getRequestData());

        CoreLanguage lang = (CoreLanguage)Core.getInstance(CoreLanguage.class);

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.save.module.success", nms);

        helper.back(msg, id, nms);
    }

    @CommitSuccess
    public void actionRemove(ActionHelper helper)
    throws HongsException {
        model.remove(helper.getRequestData());

        CoreLanguage lang = (CoreLanguage)Core.getInstance(CoreLanguage.class);

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.remove.module.success", nms);

        helper.back(msg);
    }

    public void actionUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.back(rst);
    }

}
