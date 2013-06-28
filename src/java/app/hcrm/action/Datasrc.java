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
        Map view = model.getPage(helper.getRequestData());
        helper.back(view);
    }

    public void actionInfo(ActionHelper helper)
    throws HongsException {
        Map view = model.getInfo(helper.getRequestData());

        Map data = new HashMap();
        view.put("data" , data );
        data.put("class", model.getClassSelect());

        helper.back(view);
    }

    public void actionConf(ActionHelper helper)
    throws HongsException {
        Map view = new HashMap();
        List list = model.getClassConfig(helper.getParameter("class"));
        view.put( "list", list );
        helper.back(view);
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
