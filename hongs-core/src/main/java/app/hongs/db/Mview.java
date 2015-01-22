package app.hongs.db;

import app.hongs.HongsException;
import app.hongs.action.FormSet;
import app.hongs.util.Dict;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 视图工具
 * @author Hongs
 */
public class Mview {

    protected DB    db;
    protected Table table;
    protected Model model;

    public Mview(Model model) {
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
        return model.findCols[0];
    }

    public String getTitle()
    throws HongsException {
        String sql = "SHOW TABLE STATUS WHERE name = ?";
        List<Map<String, Object>> rows = db.fetchAll(sql, table.tableName);
        String dsp = (String)rows.get(0).get("Comment");
        if (null == dsp || "".equals(dsp)) {
            dsp = table.name;
        }
        return dsp;
    }

    public Map<String, Map<String, String>> getFields()
    throws HongsException {
        Map<String, Map<String, String>> fields = new LinkedHashMap();

        // 主键
        Map<String, String> field = new HashMap();
        fields.put(table.primaryKey, field);
        field .put("widget", "hidden");
        field .put("__disp__", "#");

        Map assocs2 = table.assocs;

        String sql = "SHOW FULL FIELDS FROM `"+table.tableName+"`";
        List<Map<String, Object>> rows = db.fetchAll(sql);
        for (Map<String, Object>  row  : rows) {
            String disp     = (String)row.get("Comment");
            String fkey     = (String)row.get( "Field" );
            String widget   = (String)row.get( "Type"  );
            String required = "NO" .equals(row.get("Null")) ? "required" : "";

            if (table.primaryKey.equals(fkey)) {
                continue;
            }

            if (disp == null || "".equals(disp)) {
                disp  = fkey;
            }

            if (fkey.endsWith("_id")) {
                widget = "hidden";
            } else
            if (Pattern.compile("(decimal|numeric|integer|tinyint|smallint|float|double).*", Pattern.CASE_INSENSITIVE).matcher(widget).matches()) {
                widget = "number";
            } else
            if (Pattern.compile("(datetime|timestamp).*", Pattern.CASE_INSENSITIVE).matcher(widget).matches()) {
                widget = "datetime";
            } else
            if (Pattern.compile("(date)"  , Pattern.CASE_INSENSITIVE).matcher(widget).matches()) {
                widget = "date";
            } else
            if (Pattern.compile("(time)"  , Pattern.CASE_INSENSITIVE).matcher(widget).matches()) {
                widget = "time";
            } else
            if (Pattern.compile("(text).*", Pattern.CASE_INSENSITIVE).matcher(widget).matches()) {
                widget = "textarea";
            } else {
                widget = "text";
            }

            field = new HashMap();
            fields.put(fkey, field);
            field.put("widget", widget);
            field.put("__disp__", disp);
            field.put("__required__", required);
        }

        Iterator it2 = assocs2.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry et = (Map.Entry)it2.next( );
            Map       vd = (Map)et.getValue();
            String  type = (String)vd.get("type");

            String  fkey, disp, tn, vk, tk, an, bn;

            if ("BLS_TO".equals(type)) {
                tn   = (String) vd.get("name");
                bn   = (String) vd.get("tableName" );
                vk   = (String) vd.get("foreignKey");
                fkey = vk;

                Model hm = db.getModel(bn != null ? bn : tn);
                Mview hb =  new  Mview(hm);
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
                fkey = tn + ("HAS_ONE".equals(type) ? "." : "..") + vk;

                Model hm = db.getModel(bn != null ? bn : tn);
                Mview hb =  new  Mview(hm);
                tk   =an+"."+hb.getNmKey();
                disp =/****/ hb.getTitle();
            }

            field = new HashMap();
            fields.put(fkey, field);
            field.put("widget", "pick");
            field.put("__disp__", disp);
            field.put("data-tn", tn);
            field.put("data-tk", tk);
            field.put("data-vk", vk);
        }

        //** 从结构配置中追加字段 **/

        FormSet form = FormSet.getInstance(   db.name);
        Map items = form.getFormTranslated(table.name);
        if (items != null)
        for(Object o : items.entrySet( ) ) {
            Map.Entry e = (Map.Entry) o;
            String n = (String) e.getKey();
            Map    m = (Map ) e.getValue();

            field = new HashMap();

            String t = (String) m.get("__type__");
            if ("number".equals(t)) {
                field.put("widget", "number");
            } else
            if ("date".equals(t)) {
                field.put("widget", "date");
            } else
            if ("file".equals(t)) {
                field.put("widget", "file");
            } else
            if ("enum".equals(t)) {
                field.put("widget", "select");
            } else
            if ("form".equals(t)) {
                continue; // 表单类型暂不处理
            } else {
                field.put("widget", "text");
            }

            for (Object o2 : m.entrySet( )) {
                Map.Entry  e2 = (Map.Entry) o2;
                String k2 = (String) e2.getKey();
                String v2 = (String) e2.getValue();
                if (!k2.contains("-")) {
                    field.put(k2, v2);
                }
            }
            
            Dict.setValues(fields, field, n);
        }

        return fields;
    }

}
