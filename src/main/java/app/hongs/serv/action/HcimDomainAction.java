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

    @InList(conf="hcim", keys={"type=DOMAIN_TYPES"})
    public void action_list(ActionHelper helper)
    throws HongsException {
        Map data = model.getPage(helper.getRequestData());
        helper.reply(data);
    }

    @InForm(conf="hcim", keys={"type=DOMAIN_TYPES"})
    public void action_info(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());
        helper.reply(data);
    }

    @CommitSuccess
    public void action_save(ActionHelper helper)
    throws HongsException {
        Map data = helper.getRequestData();
        
        String type = data.get("type").toString();
        if ("2".equals(type)) {
            // 数字取值范围
            String min = data.get("min").toString();
            String max = data.get("max").toString();
            String rule = min + "," + max;
            data.put("rule", rule);
        }
        else if ("1".equals(type)) {
            // 字符
            data.put("scale", "0");
            data.put("signed","0");
        }
        else if ("6".equals(type)) {
            // 文本
            data.put("rule" , "" );
            data.put("scale", "0");
            data.put("signed","0");
        }
        else {
            // 其他
            data.put("rule" , "" );
            data.put("size" , "0");
            data.put("scale", "0");
            data.put("signed","0");
        }
        
        String id = model.save(data);

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.save.domain.success", nms);

        helper.reply(msg, id, nms);
    }

    @CommitSuccess
    public void action_remove(ActionHelper helper)
    throws HongsException {
        model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = lang.translate("core.remove.domain.success", nms);

        helper.reply(msg);
    }

    public void action_unique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.reply(rst);
    }

}
