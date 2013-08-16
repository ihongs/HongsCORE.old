package app.hcrm.model;

import app.hcrm.util.AbstractModel;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.DatumsConfig;
import app.hongs.db.DB;
import app.hongs.db.FetchBean;
import app.hongs.util.Str;
import java.util.ArrayList;
import java.util.HashMap;
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
        super("hcrm", "a_hcrm_dataset");
    }

    @Override
    public String add(Map<String, Object> data)
    throws HongsException {
        String id = super.add(data);

        buildDataTable(id);

        return id;
    }

    @Override
    public String put(String id, Map<String, Object> data)
    throws HongsException {
        /**
         * 获取旧的列特征
         */
        List<String> oldColsFeature = getColsFeature(id);

        id = super.put(id, data);

        /**
         * 获取新的列特征, 如果不一致则重建数据表
         */
        List<String> newColsFeature = getColsFeature(id);
        if (! oldColsFeature.equals(newColsFeature)) {
            buildDataTable(id);
        }

        return id;
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

        // 分割栏
        if (!rows.isEmpty()) {
            List a = new ArrayList();
            list.add(a); a.add(""); a.add(lang.translate("user.js.hcrm.loader.datasrc"));
        }

        // 获取数据源
        rows = this.db.getTable ("a_hcrm_datasrc")
                      .fetchMore(new FetchBean());
        for (Map row : rows) {
            String c = (String)row.get("class");
            String v = (String)row.get("id")+"+"+c;
            String t = (String)row.get("name" );
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
                return (List)row.get("dataset");
            }
        }

        return null;
    }

    private List<String> getColsFeature(String id) throws HongsException {
        FetchBean bean = new FetchBean(db.getTable("a_hcrm_dataset_cols"));
                  bean.where("dataset_id=?", id)
                      .orderBy("name,type,value_type,value_size,value_scale");
        List<Map> rows = db.fetchMore(bean);

        List<String> feature = new ArrayList();

        for (Map row : rows) {
            feature.add(row.get("name").toString());
            feature.add(row.get("type").toString());
            feature.add(row.get("value_type").toString());
            feature.add(row.get("value_size").toString());
            feature.add(row.get("value_scale").toString());
        }

        return feature;
    }

    private void buildDataTable(String id) throws HongsException {
        FetchBean bean = new FetchBean(db.getTable("a_hcrm_dataset_cols"));
                  bean.where("dataset_id=?", id)
                      .orderBy("type,name");
        List<Map> rows = db.fetchMore(bean);

        DatumsConfig conf =  new DatumsConfig("hcrm");
        String dct = conf.getDataByKey("DATASET_CREATE_TABLE").toString( );

        Map<String , String> rep = new HashMap();
        StringBuilder colsSet = new StringBuilder();
        StringBuilder idxsSet = new StringBuilder();

        for (Map row : rows) {
            String col  = row.get("id").toString();
            String name = row.get("name").toString();
            String type = row.get("type").toString();
            String valueType = row.get("value_type").toString();
            String valueSize = row.get("value_size").toString();
            String valueScale = row.get("value_scale").toString();

            switch (valueType) {
                case "1":
                    valueType = "VARCHAR("+valueSize+") NOT NULL DEFAULT ''";
                    break;
                case "2":
                    valueType = "DECIMAL("+valueSize+","+valueScale+") NOT NULL DEFAULT 0";
                    break;
                case "3":
                    valueType = "DATE DEFAULT";
                    break;
                case "4":
                    valueType = "TIME DEFAULT";
                    break;
                case "5":
                    valueType = "DATETIME DEFAULT";
                    break;
            }
            colsSet.append("`C"+col+"` "+valueType+" COMMENT '"+name+"',");

            if ("1".equals(type)) {
                idxsSet.append("INDEX I"+col+" (C"+col+"),");
            }
        }

        rep.put("table_name", "m_hcrm_data_"+id);
        rep.put("cols_set", colsSet.toString( ));
        rep.put("idxs_set", idxsSet.toString( ));
        rep.put("pk_set", "`id` INTEGER(11) NOT NULL AUTO_INCREMENT,");

        /**
         * 先检查表是否存在
         * 再检查表里是否有记录
         * 如已经有记录, 则报错
         * 如表存在而没记录, 则删除表
         * 最后重建表
         */

        String tab = rep.get("table_name").toString();
        DB baseDB = DB.getInstance("hcrm_base");
        Map row = db.fetchOne("SHOW TABLES LIKE '"+tab+"'");
        if (! row.isEmpty()) {
            row = db.fetchOne("SELECT * FROM `"+tab+"`");
            if (! row.isEmpty()) {
                throw new HongsException(0x10012 );
            }
            baseDB.execute("DROP TABLE `"+tab+"`");
        }

        baseDB.execute(Str.inject(dct, rep));
    }

}
