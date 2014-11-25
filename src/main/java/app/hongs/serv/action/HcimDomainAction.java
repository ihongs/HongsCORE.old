package app.hongs.serv.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.annotation.CommitSuccess;
import app.hongs.annotation.InForm;
import app.hongs.annotation.InList;
import java.util.Map;

/**
 * 模型动作接口
 * @author Hongs
 */
@Action("hcim/domain")
public class HcimDomainAction {

    private final app.hongs.serv.HcimDomain model;
    private final CoreLanguage lang;

    public HcimDomainAction() {
        model = (app.hongs.serv.HcimDomain)
                Core.getInstance(app.hongs.serv.HcimDomain.class);
        lang  = (CoreLanguage)
                Core.getInstance(CoreLanguage.class);
        lang.load("hcim");
    }

    @Action("list")
    @InList(conf="hcim", keys={"type=DOMAIN_TYPES"})
    public void getList(ActionHelper helper)
    throws HongsException {
        Map data = model.getPage(helper.getRequestData());
        helper.reply(data);
    }

    @Action("info")
    @InForm(conf="hcim", keys={"type=DOMAIN_TYPES"})
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
        String  msg = lang.translate("core.save.domain.success");
        helper.reply(msg, id);
    }

    @Action("delete")
    @CommitSuccess
    public void doDelete(ActionHelper helper)
    throws HongsException {
        int     rd  = model.delete(helper.getRequestData());
        String  msg = lang.translate("core.delete.domain.success", Integer.toString(rd));
        helper.reply(msg);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.exists(helper.getRequestData());
        helper.reply(rst);
    }

}
