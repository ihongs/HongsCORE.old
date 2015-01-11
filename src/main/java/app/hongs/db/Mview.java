package app.hongs.db;

import app.hongs.HongsException;
import app.hongs.action.StructConfig;
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
        field .put("type", "hidden");
        field .put("disp", "#");

        Map assocs2 = table.assocs;

        Iterator it1 = assocs2.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry et = (Map.Entry)it1.next( );
            Map       vd = (Map) et.getValue();
            String  type = (String)vd.get("type");

            if (!"BLS_TO".equals(type)) {
                continue;
            }

            String fkey, disp, tn, vk, tk, bn;

            tn   = (String) vd.get("name");app.hongs.util.Data.dumps(vd);
            bn   = (String) vd.get("tableName" );
            vk   = (String) vd.get("foreignKey");
            fkey = vk;

            Model hm = db.getModel(bn != null ? bn : tn);
            Mview hb =  new  Mview(hm);
            tk   = hb.getNmKey();
            disp = hb.getTitle();

            field = new HashMap();
            fields.put(fkey, field);
            field.put("disp", disp);
            field.put("type","pick");
            field.put("data-tn", tn);
            field.put("data-tk", tk);
            field.put("data-vk", vk);
        }

        String sql = "SHOW FULL FIELDS FROM `"+table.tableName+"`";
        List<Map<String, Object>> rows = db.fetchAll(sql);
        for (Map<String, Object>  row  : rows) {
            String fkey     = (String)row.get( "Field" );
            String type     = (String)row.get( "Type"  );
            String disp     = (String)row.get("Comment");
            String required = "NO" .equals(row.get("Null")) ? "required" : "";

            if (table.primaryKey.equals(fkey)) {
                continue;
            }
            if (fields.containsKey(fkey)) {
                fields.get(fkey).put("required", required);
                continue;
            }

            if (disp == null || "".equals(disp)) {
                disp  = fkey;
            }

            if (Pattern.compile("(decimal|numeric|integer|tinyint|smallint|float|double).*", Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                type = "number";
            } else
            if (Pattern.compile("(datetime|timestamp).*", Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                type = "datetime";
            } else
            if (Pattern.compile("(date)"  , Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                type = "date";
            } else
            if (Pattern.compile("(time)"  , Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                type = "time";
            } else
            if (Pattern.compile("(text).*", Pattern.CASE_INSENSITIVE).matcher(type).matches()) {
                type = "textarea";
            } else {
                type = "text";
            }

            field = new HashMap();
            fields.put(fkey, field);
            field.put("disp", disp);
            field.put("type", type);
            field.put("required", required);
        }

        Iterator it2 = assocs2.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry et = (Map.Entry)it2.next( );
            Map       vd = (Map)et.getValue();
            String  type = (String)vd.get("type");

            if (!"HAS_ONE".equals(type) && !"HAS_MANY".equals(type)) {
                continue;
            }
            if (!vd.containsKey("assocs")) {
                continue;
            }

            String fkey, disp, tn, vk, tk, an, bn;

            Map ad = (Map) ((Map) vd.get("assocs")).values().toArray()[0];
            an   = (String) ad.get("name");
            bn   = (String) ad.get("tableName" );
            tn   = (String) vd.get("name");
            vk   = (String) vd.get("foreignKey");
            fkey = tn + ("HAS_ONE".equals(type) ? "." : "..") + vk;

            Model hm = db.getModel(bn != null ? bn : tn);
            Mview hb =  new  Mview(hm);
            tk   = an + "." + hb.getNmKey();
            disp = /** ** **/ hb.getTitle();

            field = new HashMap();
            fields.put(fkey, field);
            field.put("disp", disp);
            field.put("type","pick");
            field.put("data-tn", tn);
            field.put("data-tk", tk);
            field.put("data-vk", vk);
        }

        //** 从结构配置中追加字段 **/

        Map form  = StructConfig.getInstance(db.name).getForm(table.name);
        if (form != null)
        for(Object o : ((Map)form.get("items")).entrySet()) {
            Map.Entry e = (Map.Entry) o;
            String n = (String) e.getKey();
            if (n.startsWith("_")) {
                continue;
            }

            String t;
            Map m = (Map ) e.getValue();
            field = new HashMap();

            t = (String) m.get("_type");
            if (t != null && !"".equals(t)) {
                field.put("type",  t  );
            }

            t = (String) m.get("_disp");
            if (t != null && !"".equals(t)) {
                field.put("disp",  t  );
            }

            // Form 类型暂不处理
            if ("form".equals(t)) {
                continue;
            }

            for (Object o2 : m.entrySet( )) {
                Map.Entry  e2 = (Map.Entry) o2;
                String     k2 = (String) e2.getKey();
                if (k2.startsWith("fore.")) {
                    field.put(k2.substring(5), e2.getValue().toString());
                }
            }

            Dict.putPoint(fields, field, n);
        }

        return fields;
    }

}
