package app.hcrm.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.CommitSuccess;
import app.hongs.action.annotation.SelectData;
import app.hongs.action.annotation.SelectList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据集动作
 * @author Hong
 */
public class Dataset {
    app.hcrm.model.Dataset model;

    public Dataset() {
        model = (app.hcrm.model.Dataset)
                Core.getInstance(app.hcrm.model.Dataset.class);
    }

    @SelectList(conf="hcrm", lang="hcrm", keys={
        "exec_type=DATASET_EXEC_TYPES",
        "exec_wday=DATASET_EXEC_WDAYS",
        "dflag=DATASET_STATS"
    })
    public void actionList(ActionHelper helper)
    throws HongsException {
        Map view = model.getPage(helper.getRequestData());
        helper.back(view);
    }

    @SelectData(conf="hcrm", lang="hcrm", keys={
        "exec_type=DATASET_EXEC_TYPES",
        "exec_hour=DATASET_EXEC_HOURS",
        "exec_wday=DATASET_EXEC_WDAYS",
        "exec_mday=DATASET_EXEC_MDAYS"
    })
    public void actionInfo(ActionHelper helper)
    throws HongsException {
        Map view = model.getInfo(helper.getRequestData());

        // class参数的格式是: datasrc_id+class
        Map info = (Map)view.get( "info" );
        String cls = (String)info.get("class");
        String src = (String)info.get("datasrc_id");
        if (!"0".equals(src)) {
            info.put("class", src+"+"+cls);
        }

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
        // class参数的格式是：datasrc_id+class 或 class
        Map data = helper.getRequestData(  );
        String c = (String)data.get("class");
        String[] a = c.split( "\\+", 2 );
        if (a.length == 2) {
            data.put("class" , a[1]);
            data.put("datasrc_id", a[0]);
        }
        else if (!data.containsKey("datasrc_id")) {
            data.put("datasrc_id",  "0");
        }

        String id = model.save(data);

        String nms = model.getAffectedNames();
        String msg = "保存数据集 "+nms+" 成功";

        helper.back(id , msg);
    }

    @CommitSuccess
    public void actionRemove(ActionHelper helper)
    throws HongsException {
        int num = model.remove(helper.getRequestData());

        String nms = model.getAffectedNames();
        String msg = "删除数据集 "+nms+" 成功";

        helper.back(num, msg);
    }

    public void actionUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.back(rst);
    }
}
