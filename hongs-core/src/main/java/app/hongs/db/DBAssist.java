package app.hongs.db;

import app.hongs.CoreConfig;
import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.FormSet;
import app.hongs.action.MenuSet;
import app.hongs.util.Synt;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/*
import java.util.List;
import java.util.regex.Pattern;
*/

/**
 * 视图助手
 * @author Hongs
 */
public class DBAssist {

    protected DB    db;
    protected Table table;
    protected Model model;

    private  String nmkey;
    private  String title;
    private  Map<String, Map<String, String>> fields;

    public DBAssist(Model model) {
        this.db = model.db;
        this.model = model;
        this.table = model.table;
    }

    public String getIdKey()
    throws HongsException {
        return table.primaryKey ;
    }

    public String getNmKey()
    throws HongsException {
        if (null != nmkey) {
            return  nmkey;
        }

        getFields();
        if (model.listCols.length > 0) {
            nmkey = model.listCols [0];
            return nmkey;
        }
        if (model.findCols.length > 0) {
            nmkey = model.findCols [0];
            return nmkey;
        }
        nmkey  =  "";
        return nmkey;
    }

    public String getTitle()
    throws HongsException {
        if (null != title) {
            return  title;
        }

        CoreLocale trns = CoreLocale.getInstance(db.name);
        MenuSet    site =    MenuSet.getInstance(db.name);
        Map        cell =       site.getMenu    (db.name+"/"+table.name+"/");
        String     disp ;
        if (cell != null && cell.containsKey("disp")) {
            disp = (String) cell.get  ( "disp");
        } else {
            disp = "table."+table.name+".name" ;
        }
        title = trns.translate(disp);
        return  title;

        /*
        String sql = "SHOW TABLE STATUS WHERE name = ?";
        List<Map<String, Object>> rows = db.fetchAll(sql, table.tableName);
        String dsp = (String)rows.get(0).get("Comment");
        if (null == dsp || "".equals(dsp)) {
            dsp = table.name;
        }
        return dsp;
        */
    }

    public Map<String, Map<String, String>> getFields()
    throws HongsException {
        if (null != fields) {
            return  fields;
        }

        CoreLocale trns = CoreLocale.getInstance(db.name);
                 fields =    FormSet.getInstance(db.name)
                           .getFormTranslated(table.name);
        if (fields == null) {
            fields =  new  LinkedHashMap();
        }

        List<String> findCols = new ArrayList();
        List<String> listCols = new ArrayList();

        // 默认可搜索的类型
        Set findable = new HashSet(Arrays.asList(Synt.declare(
            CoreConfig.getInstance()
           .get("core.findable.types"),
                "string,search,text,textarea,email,url,tel"
        ).split(",")));
        // 默认不能排序的类型
        Set sortable = new HashSet(Arrays.asList(Synt.declare(
            CoreConfig.getInstance()
           .get("core.sortable.types"),
                "string,search,text,email,url,tel,number,range,onoff,date,time,datetime,enum,select,radio,check"
        ).split(",")));
        // 默认可列举的类型
        Set listable = new HashSet(Arrays.asList(Synt.declare(
            CoreConfig.getInstance()
           .get("core.listable.types"),
                "string,search,text,email,url,tel,number,range,onoff,date,time,datetime,enum,select,radio,check,form,picker"
        ).split(",")));

        /*
        String sql = "SHOW FULL FIELDS FROM `"+table.tableName+"`";
        List<Map<String, Object>> rows = db.fetchAll(sql);
        for (Map<String, Object>  row  : rows) {
            String disp = (String)row.get("Comment");
            String name = (String)row.get( "Field" );
            String type = (String)row.get( "Type"  );
        */
        Map<String, Map> cols = table.getFields();
        for(Map.Entry<String, Map> ent : cols.entrySet()) {
            Map     col  = ent.getValue();
            String  name = ent.getKey(  );
            Integer type = (Integer) col.get("type");
            Boolean rqrd = (Boolean) col.get("required");
            String  disp = "field."+ table.name +"."+ name +".name";

            Map field = (Map) fields.get(name);
            if (field == null) {
                field =  new HashMap( );
                fields.put(name, field);
            }

            if (!field.containsKey("__required__")) {
//              field.put("__required__", "NO".equals(row.get("Null")) ? "yes" : "");
                field.put("__required__", rqrd ? "yes" : "");
            }

            if (!field.containsKey("__disp__")) {
//              if (disp!=null && !"".equals(disp)) {
                if (trns.containsKey(disp)) {
                    disp = trns.translate(disp);
                    field.put("__disp__", disp);
                } else {
                    field.put("__disp__", name);
                }
            }

            if (!field.containsKey("__type__")) {
                if (name.endsWith("_id") || name.equals(table.primaryKey)) {
                    field.put("__type__", "hidden");
                } else
                if (type == Types.INTEGER || type == Types.TINYINT || type == Types.BIGINT || type == Types.SMALLINT
                ||  type == Types.NUMERIC || type == Types.DECIMAL || type == Types.DOUBLE || type == Types.FLOAT) {
                    field.put("__type__", "number");
                } else
                if (type == Types.LONGVARCHAR || type == Types.LONGNVARCHAR) {
                    field.put("__type__", "textarea");
                } else
                if (type == Types.TIMESTAMP) {
                    field.put("__type__", "datetime");
                } else
                if (type == Types.DATE) {
                    field.put("__type__", "date");
                } else
                if (type == Types.TIME) {
                    field.put("__type__", "time");
                }
                /*
                if (Pattern.compile("(decimal|numeric|integer|tinyint|smallint|float|double).*", Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                    field.put("__type__", "number");
                } else
                if (Pattern.compile("(datetime|timestamp).*", Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                    field.put("__type__", "datetime");
                } else
                if (Pattern.compile("(date)"  , Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                    field.put("__type__", "date");
                } else
                if (Pattern.compile("(time)"  , Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                    field.put("__type__", "time");
                } else
                if (Pattern.compile("(text).*", Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                    field.put("__type__", "textarea");
                }
                */
            }

            // 特定类型的不能列举和排序
            if (!field.containsKey("findable") && findable.contains(field.get("__type__"))) {
                field.put("findable", "yes");
            }
            if (!field.containsKey("listable") && listable.contains(field.get("__type__"))) {
                field.put("listable", "yes");
            }
            if (!field.containsKey("sortable") && sortable.contains(field.get("__type__"))) {
                field.put("sortable", "yes");
            }

            // 提取搜索和列举字段
            if (Synt.declare(field.get("findable"), false)) {
                findCols.add(name);
            }
            if (Synt.declare(field.get("listable"), false)) {
                listCols.add(name);
            }
        }

        if (table.assocs != null) {
        Iterator it = table.assocs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry et = (Map.Entry)it.next();
            Map       vd = (Map ) et.getValue();
            String  type = (String) vd.get("type");

            String  name, disp, tn, vk, tk, an, bn;

            if ("BLS_TO".equals(type)) {
                tn   = (String) vd.get("name");
                bn   = (String) vd.get("tableName" );
                vk   = (String) vd.get("foreignKey");
                name = vk;

                Model hm = db.getModel(bn != null ? bn : tn);
                DBAssist hb =  new  DBAssist(hm);
                tk   = hb.getNmKey();
                disp = hb.getTitle();
            } else {
                if (!vd.containsKey("assocs")) {
                    continue;
                }

                Map ad = (Map) ((Map) vd.get("assocs")).values().toArray()[0];
                an   = (String) ad.get("name");
                tn   = (String) vd.get("name");
                bn   = (String) ad.get("tableName" );
                vk   = (String) vd.get("foreignKey");
                name = tn + ("HAS_ONE".equals(type) ? "." : "..") + vk;

                Model hm = db.getModel(bn != null ? bn : tn);
                DBAssist hb =  new  DBAssist(hm);
                tk   =an+"."+hb.getNmKey();
                disp =/****/ hb.getTitle();
            }

            Map field = (Map) fields.get(name);
            if (field == null) {
                field =  new HashMap( );
                fields.put(name, field);

                field = new HashMap();
                fields.put(name, field);
                field.put("__type__","pick");
                field.put("__disp__", disp );
                field.put("data-tn", tn);
                field.put("data-tk", tk);
                field.put("data-vk", vk);
            }

            // 特定类型的不能列举、排序
            if (!field.containsKey("findable") && findable.contains(field.get("__type__"))) {
                field.put("findable", "yes");
            }
            if (!field.containsKey("listable") && listable.contains(field.get("__type__"))) {
                field.put("listable", "yes");
            }
            if (!field.containsKey("sortable") && sortable.contains(field.get("__type__"))) {
                field.put("sortable", "yes");
            }

            // 提取搜索和列举字段
            if (Synt.declare(field.get("findable"), false)) {
                findCols.add(name);
            }
            if (Synt.declare(field.get("listable"), false)) {
                listCols.add(name);
            }
        }
        }

        // 设置搜索和列举字段
        model.findCols = findCols.toArray(new String[0]);
        model.listCols = listCols.toArray(new String[0]);

        return fields;
    }

}
