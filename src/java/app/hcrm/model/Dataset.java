package app.hcrm.model;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;

/**
 * 数据集模型
 * @author Hong
 */
public class Dataset
extends AbstractBaseModel {

    public Dataset()
    throws HongsException {
        super("hcrm", "ar_dataset_base_info");
    }

}
