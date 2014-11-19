package app.hongs.serv;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;

/**
 *
 * @author Hongs
 */
public class HaimCommon extends AbstractBaseModel {
    public HaimCommon(String dbConf, String tableName) throws HongsException {
        super(dbConf, tableName);
    }
}
