package net.hongs.search.action;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.anno.Action;
import java.util.Map;
import net.hongs.search.Reader;
import net.hongs.search.Writer;

/**
 * 搜索动作
 * @author Hongs
 */
@Action("search")
public class SearchAction {

    @Action("retrieve")
    public void retrieve(ActionHelper helper) throws HongsException {
        Reader reader = new Reader();
        Map rd = helper.getRequestData();
        String id = (String)rd.get("id");
        Map sd;
        if (id == null|| "".equals("id")|| "0".equals("id")) {
            sd = reader.getList(rd);
        } else {
            sd = reader.getInfo(id);
        }
        reader.close(  );
        helper.reply(sd);
    }

    @Action("count/retrieve")
    public void retrieveCount(ActionHelper helper) throws HongsException {
        Reader reader = new Reader();
        Map rd = helper.getRequestData();
        Map sd = reader.getCnts(rd);
        reader.close(  );
        helper.reply(sd);  
    }

    @Action("create")
    public void createIndex(ActionHelper helper) throws HongsException {
        Writer writer = new Writer();
        Map rd = helper.getRequestData();
        writer.update(rd );
        writer.commit();
        writer.close( );
        helper.reply(true);
    }
    
    @Action("delete")
    public void deleteIndex(ActionHelper helper) throws HongsException {
        Writer writer = new Writer();
        Map rd = helper.getRequestData();
        String id = (String)rd.get("id");
        writer.delete(id );
        writer.commit();
        writer.close( );
        helper.reply(true);
    }
    
}
