package net.hongs.search;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotaion.Action;
import java.util.Map;

/**
 * 搜索动作
 * @author Hongs
 */
@Action("search/engine")
public class SearchAction {

    Search search;

    public SearchAction() throws HongsException {
        search = new Search();
    }

    @Action("article/create")
    public void indexArticle(ActionHelper helper) throws HongsException {
        Map rd = helper.getRequestData( );
        Object id = rd.get("id");
        if (id != null) {
            /***/search.update(rd);
        } else {
            id = search.create(rd);
        }
        helper.reply("索引成功", id);
    }

    @Action("article/retrieve")
    public void retrieveArticle(ActionHelper helper) throws HongsException {
        Map rd = helper.getRequestData( );
        Map sd = search.searchArticle(rd);
        helper.reply(sd);
    }

    @Action("subject/retrieve")
    public void retrieveSubject(ActionHelper helper) throws HongsException {
        Map rd = helper.getRequestData( );
        Map sd = search.searchSubject(rd);
        helper.reply(sd);
    }

}
