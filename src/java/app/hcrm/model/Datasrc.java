package app.hcrm.model;

import app.hcrm.util.AbstractModel;
import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;
import app.hongs.db.FetchBean;
import java.util.List;
import java.util.Map;

/**
 * 数据源模型
 * @author Hong
 */
public class Datasrc
extends AbstractModel {

    public Datasrc()
    throws HongsException {
        super("hcrm", "ar_datasrc_base_info");
    }

}
