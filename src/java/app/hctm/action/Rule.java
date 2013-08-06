package app.hctm.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import java.util.Map;

/**
 * 规则动作
 * @author Hong
 */
public class Rule {
    app.hctm.model.Rule model;

    public Rule() {
        model = (app.hctm.model.Rule)
            Core.getInstance(app.hctm.model.Code.class);
    }

    public void actionList(ActionHelper helper) throws HongsException {
        Map data = model.getPage(helper.getRequestData());
        helper.back(data);
    }

    public void actionInfo(ActionHelper helper) throws HongsException {
        Map data = model.getInfo(helper.getRequestData());
        helper.back(data);
    }

    public void actionSave(ActionHelper helper) throws HongsException {
        String id = model.save(helper.getRequestData());
        helper.back(id);
    }

    public void actionRemove(ActionHelper helper) throws HongsException {
        Number rn = model.remove(helper.getRequestData());
        helper.back(rn);
    }

    public void actionUnique(ActionHelper helper) throws HongsException {
        boolean ub = model.unique(helper.getRequestData());
        helper.back(ub);
    }
}
