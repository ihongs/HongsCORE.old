package app.hcrm.model;

import app.hcrm.util.AbstractModel;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.DatumsConfig;
import java.util.ArrayList;
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

    /**
     * 获取类别选择列表
     * @return
     * @throws HongsException
     */
    public List getClassSelect() throws HongsException {
        List<Map > rows;
        List<List> list = new ArrayList();
        DatumsConfig conf = new DatumsConfig("hcrm");
        CoreLanguage lang = new CoreLanguage("hcrm");

        // 获取类
        rows = (List)conf.getDataByKey("LOADER_CLASSES");
        for (Map row : rows) {
            if (!row.containsKey("datasrc")) continue;
            String v = (String)row.get("class");
            String t = lang.translate("user.js.hcrm.loader."+v);
            List a = new ArrayList();
            list.add(a); a.add(v); a.add(t);
        }

        return list;
    }

    /**
     * 获取类别详细配置
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
