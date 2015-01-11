package net.hongs.search;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.anno.Action;
import java.util.Map;
import net.hongs.search.record.Reader;
import net.hongs.search.record.Writer;

/**
 * 搜索动作
 * @author Hongs
 */
@Action("search/engine")
public class SearchAction {

    @Action("index/create")
    public void createIndex(ActionHelper helper) throws HongsException {
        Writer writer = new Writer();
        Map rd = helper.getRequestData();
        writer.update(rd );
        helper.reply(true);
    }
    
    @Action("article/retrieve")
    public void retrieveArticle(ActionHelper helper) throws HongsException {
        Reader reader = new Reader();
        Map rd = helper.getRequestData();
        Map sd = reader.getList(rd);
        reader.close(  );
        helper.reply(sd);        
    }

    @Action("subject/retrieve")
    public void retrieveSubject(ActionHelper helper) throws HongsException {
        Reader reader = new Reader();
        Map rd = helper.getRequestData();
        Map sd = reader.getCnts(rd);
        reader.close(  );
        helper.reply(sd);  
    }

}
