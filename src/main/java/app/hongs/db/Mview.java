package app.hongs.db;

import app.hongs.HongsException;
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
        this.model = model;
        this.table = model.table;
        this.db = model.db;
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
        return table.name;
    }

    public Map<String, Map<String, String>> getFields()
    throws HongsException {
        String sql = "SHOW FULL FIELDS FROM `"+table.tableName+"`";
        List<Map<String, Object>> rows = db.fetchAll(sql);

        Map<String, Map<String, String>> fields = new LinkedHashMap();
        Map<String, String> field = new HashMap();
        fields.put(table.primaryKey, field);
        field .put("type", "hidden");

        Map assocs2 = table.assocs;

        Iterator it1 = assocs2.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry et = (Map.Entry)it1.next( );
            Map       vd = (Map) et.getValue();
            String  type = (String)vd.get("type");

            if (!"BLS_TO".equals(type)) {
                continue;
            }

            String fkey, text, tn, vk, tk, bn;

            tn   = (String) vd.get("name");
            bn   = (String) vd.get("tableName" );
            vk   = (String) vd.get("foreignKey");
            fkey = vk;

            Model hm = db.getModel(bn != null ? bn : tn);
            Mview hb =  new  Mview(hm);
            tk   = hb.getNmKey();
            text = hb.getTitle();

            field = new HashMap();
            fields.put(fkey, field);
            field.put("text", text);
            field.put("type","pick");
            field.put("data-tn", tn);
            field.put("data-tk", tk);
            field.put("data-vk", vk);
        }

        for (Map<String, Object> row : rows) {
            String fkey     = (String)row.get( "Field" );
            String type     = (String)row.get( "Type"  );
            String text     = (String)row.get("Comment");
            String required = "NO" .equals(row.get("Null")) ? "required" : "";

            if (table.primaryKey.equals(fkey)) {
                continue;
            }
            if (fields.containsKey(fkey)) {
                fields.get(fkey).put("required", required);
                continue;
            }

            if (text == null || "".equals(text)) {
                text = (String)row.get("Field");
            }

            if (Pattern.compile("(int|float|double|number|numeric|decimal)", Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                type = "number";
            } else
            if (Pattern.compile("(datetime)", Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                type = "datetime";
            } else
            if (Pattern.compile("(date)", Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                type = "date";
            } else
            if (Pattern.compile("(time)", Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                type = "time";
            } else
            if (Pattern.compile("(text)", Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                type = "textarea";
            } else {
                type = "text";
            }

            field = new HashMap();
            fields.put(fkey, field);
            field.put("text", text);
            field.put("type", type);
            field.put("required", required);
        }

        Iterator it2 = assocs2.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry et = (Map.Entry)it2.next( );
            Map       vd = (Map)et.getValue();
            String  type = (String)vd.get("type");

            if (!"HAS_ONE".equals(type) || !"HAS_MANY".equals(type)) {
                continue;
            }
            if (!vd.containsKey("assocs")) {
                continue;
            }

            String fkey, text, tn, vk, tk, an, bn;

            Map ad = (Map) ((Map) vd.get("assocs")).values().toArray()[0];
            an   = (String) ad.get("name");
            bn   = (String) ad.get("tableName" );
            tn   = (String) vd.get("name");
            vk   = (String) vd.get("foreignKey");
            fkey = tn + ("HAS_ONE".equals(type) ? "." : "..") + vk;

            Model hm = db.getModel(bn != null ? bn : tn);
            Mview hb =  new  Mview(hm);
            tk   = an + "." + hb.getNmKey();
            text = /** ** **/ hb.getTitle();

            field = new HashMap();
            fields.put(fkey, field);
            field.put("text", text);
            field.put("type","pick");
            field.put("data-tn", tn);
            field.put("data-tk", tk);
            field.put("data-vk", vk);
        }

        return fields;
    }

}
