package app.hongs.db;

import app.hongs.HongsException;
import app.hongs.action.FormSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.HashMap;
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
        Map fields = FormSet.getInstance(db.name).getFormTranslated(table.name);
        if (fields == null) {
            fields =  new  LinkedHashMap();
        }

        String sql = "SHOW FULL FIELDS FROM `"+table.tableName+"`";
        List<Map<String, Object>> rows = db.fetchAll(sql);
        for (Map<String, Object>  row  : rows) {
            String disp = (String) row.get("Comment");
            String name = (String) row.get( "Field" );
            String type = (String) row.get( "Type"  );

            Map field = (Map) fields.get(name);
            if (field == null) {
                field =  new HashMap( );
                fields.put(name, field);
            }

            if (!fields.containsKey("__required__")) {
                field.put("__required__", "NO".equals(row.get("Null")) ? "yes" : "");
            }

            if (!fields.containsKey("__disp__")) {
                if (disp != null && !"".equals(disp)) {
                    field.put("__disp__", disp);
                } else {
                    field.put("__disp__", name);
                }
            }

            if (!fields.containsKey("__type__")) {
                if (name.endsWith("_id") || name.equals(table.primaryKey)) {
                    field.put("__type__", "hidden");
                } else
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
            }
        }

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
                name = tn + ("HAS_ONE".equals(type) ? "." : "..") + vk;

                Model hm = db.getModel(bn != null ? bn : tn);
                Mview hb =  new  Mview(hm);
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
        }

        return fields;
    }

}
