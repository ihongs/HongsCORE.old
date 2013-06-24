package app.hcrm.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.DatumsConfig;
import app.hongs.action.annotation.CommitSuccess;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源动作
 * @author Hong
 */
public class Datasrc {
    app.hcrm.model.Datasrc model;

    public Datasrc() {
        model = (app.hcrm.model.Datasrc)
                Core.getInstance("app.hcrm.model.Datasrc");
    }

    public void actionList(ActionHelper helper)
    throws HongsException {
        Map data = model.getPage(helper.getRequestData());
        helper.back(data);
    }

    public void actionInfo(ActionHelper helper)
    throws HongsException {
        Map view = model.getInfo(helper.getRequestData());

        // 添加类型选择
        DatumsConfig conf = new DatumsConfig("hcrm");
        CoreLanguage lang = new CoreLanguage("hcrm.js");
        Map  data = new HashMap();
        List clss = new ArrayList();
        List clsz = (List)conf.getDataByKey("LOADER_CLASSES");
        for (Map m : (List<Map>)clsz) {
            if (!m.containsKey("datasrc")) continue;
            String v = (String)m.get("class");
            String t = lang.translate("hcrm.loader."+v);
            List a = new ArrayList();
            a.add(v); a.add(t); clss.add(a);
        }
        data.put("class", clss);
        view.put("data" , data);

        helper.back(view);
    }

    public void actionConf(ActionHelper helper)
    throws HongsException {
        String cls = helper.getParameter("class");
        DatumsConfig conf = new DatumsConfig("hcrm");
        Map  data = new HashMap();
        List clsz = (List)conf.getDataByKey("LOADER_CLASSES");
        for (Map m : (List<Map>)clsz) {
            String c = (String) m.get("class");
            if (!c.equals(cls)) continue;
            data.put("list", m.get("datasrc"));
        }
        helper.back(data);
    }

    @CommitSuccess
    public void actionSave(ActionHelper helper)
    throws HongsException {
        String id = model.save(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = "保存数据源 "+nms+" 成功";

        helper.back(id, msg);
    }

    @CommitSuccess
    public void actionRemove(ActionHelper helper)
    throws HongsException {
        int ar = model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = "删除数据源 "+nms+" 成功";

        helper.back(ar, msg);
    }

    public void actionUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.back(rst);
    }
}
