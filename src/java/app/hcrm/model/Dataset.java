package app.hcrm.model;

import app.hcrm.util.AbstractModel;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.DatumsConfig;
import app.hongs.db.FetchBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据集模型
 * @author Hong
 */
public class Dataset
extends AbstractModel {

    public Dataset()
    throws HongsException {
        super("hcrm", "ar_dataset_base_info");
    }

    /**
     * 获取类别选择列表
     * 同Datasrc, 增加了数据源列表
     * @return
     * @throws HongsException
     */
    public List getClassSelect() throws HongsException {
        List<Map > rows;
        List<List> list = new ArrayList();
        DatumsConfig conf = new DatumsConfig("hcrm");
        CoreLanguage lang = new CoreLanguage("hcrm");

        // 获取数据源
        rows = this.db.getTable ("ar_datasrc_base_info")
                      .fetchMore(new FetchBean());
        for (Map row : rows) {
            String v = (String)row.get("id")+"_datasrc";
            String t = (String)row.get("name");
            String c = (String)row.get("class");
            List a = new ArrayList();
            list.add(a); a.add(v); a.add(t); a.add(c); // 多了class值
        }

        // 分割栏
        if (!rows.isEmpty()) {
            List a = new ArrayList();
            list.add(a); a.add(""); a.add(lang.translate("hcrm.loader.class"));
        }

        // 获取类别
        rows = (List)conf.getDataByKey("LOADER_CLASSES");
        for (Map row : rows) {
            if ( row.containsKey("datasrc")) continue;
            if (!row.containsKey("dataset")) continue;
            String v = (String)row.get("class");
            String t = lang.translate("user.js.hcrm.loader."+v);
            List a = new ArrayList();
            list.add(a); a.add(v); a.add(t);
        }

        return list;
    }

    /**
     * 获取类别详细配置
     * 同Datasrc
     * @param c class值
     * @return
     * @throws HongsException
     */
    public List getClassConfig(String c) throws HongsException {
        List<Map> rows;
        DatumsConfig conf = new DatumsConfig("hcrm");

        rows = (List)conf.getDataByKey("LOADER_CLASSES");
        for (Map row : rows) {
            String v = (String)row.get("class");
            if (v.equals(c)) {
                return (List)row.get("datasrc");
            }
        }

        return null;
    }
}
