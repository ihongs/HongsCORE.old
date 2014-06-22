package app.hcim.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.CommitSuccess;
import app.hongs.action.annotation.InForm;
import app.hongs.action.annotation.InList;
import java.util.Map;

/**
 * 模型动作接口
 * @author Hongs
 */
public class Attr {

    private app.hcim.model.Attr model;

    public Attr() {
        model = (app.hcim.model.Attr)
                Core.getInstance(app.hcim.model.Attr.class);
    }

    @InList(conf="hcim", keys={"type=ATTR_TYPES"})
    public void actionList(ActionHelper helper)
    throws HongsException {
        Map data = model.getPage(helper.getRequestData());
        helper.back(data);
    }

    @InForm(conf="hcim", keys={"type=ATTR_TYPES"})
    public void actionInfo(ActionHelper helper)
    throws HongsException {
        Map data = model.getInfo(helper.getRequestData());
        helper.back(data);
    }

    @CommitSuccess
    public void actionSave(ActionHelper helper)
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
        String msg = "保存属性 "+nms+" 成功";

        helper.back(msg, id, nms);
    }

    @CommitSuccess
    public void actionRemove(ActionHelper helper)
    throws HongsException {
        model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = "删除属性 "+nms+" 成功";

        helper.back(msg);
    }

    public void actionUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.back(rst);
    }

}
