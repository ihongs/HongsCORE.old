package app.hongs.dl.lucene;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.Supply;
import app.hongs.util.Synt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 搜索动作
 * @author Hongs
 */
@Action("hongs/search")
public class SearchAction extends LuceneAction {

    @Action("retrieve")
    @Supply()
    @Override
    public void retrieve(ActionHelper helper) throws HongsException {
        /**
         * 有指定查询条件则按匹配度排序
         */
        Map rd = helper.getRequestData();
        Object wd = rd.get("wd");
        if (wd != null && "".equals(wd)) {
            List ob = Synt.declare(rd.get("ob"), List.class);
            if( ob == null) {
                ob =  new ArrayList();
                rd.put("ob", ob);
                ob.add( 0 , "-");
            } else
            if(!ob.contains("-")) {
                ob.add( 0 , "-");
            }
        }

        super.retrieve(helper);
    }

    @Action("counts/retrieve")
    public void counts(ActionHelper helper) throws HongsException {
        SearchRecord sr = getModel(helper);
        Map rd = helper.getRequestData();
        Map sd = sr.counts(rd);
                 sr.destroy( );
        helper.reply(sd);
    }

    @Override
    public SearchRecord getModel(ActionHelper helper)
    throws HongsException {
        ActionRunner runner = (ActionRunner) helper.getAttribute("__RUNNER__");
        String ent = runner.getAction();
        ent  = ent.substring(0, ent.length() - runner.getMtdAnn().length() -1);
        int    pos = ent.lastIndexOf('/');
        String mod = ent.substring(0,pos);
               ent = ent.substring(pos+1);
        return new SearchRecord(mod, ent);
    }

}
