package app.hongs.serv;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;

/**
 *
 * @author Hongs
 */
public class HaimBottom extends AbstractBaseModel {
    public HaimBottom(String dbConf, String tableName) throws HongsException {
        super(dbConf, tableName);
    }
}
