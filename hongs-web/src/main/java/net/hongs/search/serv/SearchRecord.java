package net.hongs.search.serv;

import app.hongs.HongsException;
import app.hongs.dh.lucene.LuceneRecord;

/**
 * 记录器
 * @author Hongs
 */
public class SearchRecord extends LuceneRecord {

    public SearchRecord(String name) throws HongsException {
        super("search", name);
    }

}
