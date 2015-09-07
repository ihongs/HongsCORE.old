package app.hongs.dl.lucene;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsException;
import app.hongs.util.Async;

import java.io.Serializable;

/**
 * 索引队列
 * @author Hongs
 */
public class SearchIndexr extends Async<SearchIndexr.Indexr> implements Core.GlobalSingleton {

    public static interface Indexr extends Serializable {
        public void run();
    }

    private SearchIndexr() throws HongsException {
        super(SearchIndexr.class.getName(),
            CoreConfig.getInstance().getProperty("serv.lucene.max.tasks", Integer.MAX_VALUE),
            CoreConfig.getInstance().getProperty("serv.lucene.max.servs", 1/*Single Mode*/));
    }

    @Override
    public void run(SearchIndexr.Indexr idx) {
        idx.run();
    }

    public static SearchIndexr getInstance() throws HongsException {
        String name =  SearchIndexr.class.getName();
        SearchIndexr inst = (SearchIndexr) Core.GLOBAL_CORE.got(name);
        if (inst == null) {
            inst =  new SearchIndexr();
            Core.GLOBAL_CORE.put(name , inst);
        }
        return inst;
    }

}
