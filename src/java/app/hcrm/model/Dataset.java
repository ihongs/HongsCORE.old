package app.hcrm.model;

import app.hcrm.util.AbstractModel;
import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.DatumsConfig;
import app.hongs.db.DB;
import app.hongs.db.FetchBean;
import app.hongs.util.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        rows = this.db.getTable ("ar_datasrc_base_info")
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
    
    public void createTable(String id) throws HongsException {
        DB baseDB = DB.getInstance( "hcrm_base");

        FetchBean bean = new FetchBean(db.getTable("ar_dataset_cols_info"));
                  bean.where("dataset_id=?", id);
        List<Map> rows = db.fetchMore(bean);
        
        StringBuilder dims = new StringBuilder();
        StringBuilder mets = new StringBuilder();
        
        DatumsConfig conf = new DatumsConfig("hcrm");
        String dct = conf.getDataByKey("DATASET_CREATE_TABLE").toString();
        String dcs = null;

        Map<String , String> rep = new HashMap();
        rep.put( "id" , id );
        rep.put( "pk_set" , "`id` INTEGER(11) NOT NULL AUTO_INCREMENT," );
        
        for (Map row : rows) {
            String col = Core.getUniqueId();
            String name = row.get("name").toString();
            String type = row.get("type").toString();
            String valueType = row.get("value_type").toString();
            String valueSize = row.get("value_size").toString();
            String valueScale = row.get("value_scale").toString();
            StringBuilder sb = null;
            
            switch (type) {
                case "1": sb = dims; break;
                case "2": sb = mets; break;
                default : continue ;
            }
            
            switch (valueType) {
                case "1":
                    valueType = "NUMERIC("+valueSize+","+valueScale+")";
                    break;
                case "2":
                    valueType = "VARCHAR("+valueSize+")";
                    break;
                case "3":
                    valueType = "DATE";
                case "4":
                    valueType = "TIME";
                case "5":
                    valueType = "TIMESTAMP";
            }
            
            sb.append("`C"+col+"` "+valueType+" DEFAULT NULL COMMENT '"+name+"',");
        }
        
        if (dims.length() != 0) {
            rep.put("type", "dim");
            rep.put("cols_set", dims.toString());
            dcs = Text.assign(dct, rep);
            
            baseDB.execute(dcs);
        }
        
        if (mets.length() != 0) {
            rep.put("type", "met");
            rep.put("cols_set", mets.toString());
            dcs = Text.assign(dct, rep);
            baseDB.execute(dcs);
        }
    }
    
    private void createTable(String sql, DB baseDB) throws HongsException {
        Pattern p = Pattern.compile("table `.*?`", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sql);
        String tableName = m.group(1);
        String sql2 = "SELECT COUNT(*) FROM `"+tableName+"`";
        Map row;
        try {
            row = baseDB.fetchOne(sql2);
        }
        catch (HongsException ex) {
            throw ex;
        }
        if (! row.isEmpty()) {
            throw new HongsException(0x1000);
        }
        
        baseDB.execute(sql);
    }
}
