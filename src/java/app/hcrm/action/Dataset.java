package app.hcrm.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import java.util.Map;

/**
 * 数据集动作
 * @author Hong
 */
public class Dataset {
    app.hcrm.model.Dataset model;

    public Dataset() {
        model = (app.hcrm.model.Dataset)
                Core.getInstance("app.hcrm.model.Dataset");
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

    public void actionSave(ActionHelper helper)
    throws HongsException {
        String id = model.save(helper.getRequestData());
        helper.back(id);
    }

    public void actionRemove(ActionHelper helper)
    throws HongsException {
        int num = model.remove(helper.getRequestData());
        helper.back(num);
    }

    public void actionUnique(ActionHelper helper)
    throws HongsException {
        boolean rst = model.unique(helper.getRequestData());
        helper.back(rst);
    }
}
