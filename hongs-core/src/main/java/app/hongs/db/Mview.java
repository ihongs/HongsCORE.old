package app.hongs.db;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.FormSet;
import app.hongs.action.NaviMap;
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
 * 模型视图
 * @author Hongs
 */
public class Mview implements Core.Destroy {

    protected DB    db;
    protected Table table;
    protected Model model;

    private  String nmkey = null;
    private  String title = null;
    private  Map<String, Map<String, String>> fields = null;

    public Mview(Model model) {
        this.db = model.db;
        this.model = model;
        this.table = model.table;
    }

    public CoreConfig getConf() {
        CoreConfig conf = CoreConfig.getInstance().clone();
        conf.loadIgnrFNF(db.name);
        return conf;
    }

    public CoreLocale getLang() {
        CoreLocale lang = CoreLocale.getInstance().clone();
        lang.loadIgnrFNF(db.name);
        return lang;
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

        getFields( );

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

        do {
            Map item;

            // 先从表单取名字
            FormSet form = FormSet.getInstance(db.name);
            item = form.getForm(              table.name     );
            if (item != null  && item.containsKey(   "@"    )) {
                item  = ( Map  ) item.get(   "@"    );
            if (item != null  && item.containsKey("__disp__")) {
                title = (String) item.get("__disp__");
                break;
            }
            }

            // 再从菜单取名字
            NaviMap menu = NaviMap.getInstance(db.name);
            item = menu.getMenu(db.name +"/"+ table.name +"/");
            if (item != null  && item.containsKey(  "disp"  )) {
                title = (String) item.get(  "disp"  );
                break;
            }

            // 最后配置取名字
            title = "core.table."+table.name+".name";
        } while (false);

        title = getLang().translate(title);
        return   title ;

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

        CoreLocale lang = getLang();
        fields = FormSet.getInstance(db.name)
               .getFormTranslated(table.name);
        if (fields == null) {
            fields  =  new  LinkedHashMap(  );
        }

        List<String> findCols = new ArrayList(   );
        List<String> listCols = new ArrayList(   );

        Map<String, String> conf = fields.get("@");
        Set findable = null;
        if (conf == null || !Synt.declare(conf.get("dont.auto.add.findable.fields"), false)) {
            findable = new HashSet(Arrays.asList(Synt.declare(
                CoreConfig.getInstance()
               .get("core.findable.types"),
                    "string,search,text,email,url,tel,textarea"
            ).split(",")));
        }
        Set sortable = null;
        if (conf == null || !Synt.declare(conf.get("dont.auto.add.sortable.fields"), false)) {
            sortable = new HashSet(Arrays.asList(Synt.declare(
                CoreConfig.getInstance()
               .get("core.sortable.types"),
                    "string,search,text,email,url,tel,number,range,onoff,date,time,datetime,enum,select,radio,check"
            ).split(",")));
        }
        Set listable = null;
        if (conf == null || !Synt.declare(conf.get("dont.auto.add.listable.fields"), false)) {
            listable = new HashSet(Arrays.asList(Synt.declare(
                CoreConfig.getInstance()
               .get("core.listable.types"),
                    "string,search,text,email,url,tel,number,range,onoff,date,time,datetime,enum,select,radio,check,form,picker"
            ).split(",")));
        }

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

            if (!field.containsKey("__required__") || "".equals(field.get("__required__"))) {
//              field.put("__required__", "NO".equals(row.get("Null")) ? "yes" : "");
                field.put("__required__", rqrd ? "yes" : "");
            }

            if (!field.containsKey("__disp__") || "".equals(field.get("__disp__"))) {
//              if (disp!=null && !"".equals(disp)) {
                if (lang.containsKey(disp)) {
                    disp = lang.translate(disp);
                    field.put("__disp__", disp);
                } else {
                    field.put("__disp__", name);
                }
            }

            if (!field.containsKey("__type__") || "".equals(field.get("__type__"))) {
                if (name.equals(table.primaryKey) || name.endsWith("_id")) {
                    field.put("__type__", "hidden");
                } else
                if (type == Types.DATE) {
                    field.put("__type__", "date");
                } else
                if (type == Types.TIME) {
                    field.put("__type__", "time");
                } else
                if (type == Types.TIMESTAMP) {
                    field.put("__type__", "datetime");
                } else
                if (type == Types.LONGVARCHAR || type == Types.LONGNVARCHAR) {
                    field.put("__type__", "textarea");
                } else
                if (type == Types.INTEGER || type == Types.TINYINT || type == Types.BIGINT || type == Types.SMALLINT
                ||  type == Types.NUMERIC || type == Types.DECIMAL || type == Types.DOUBLE || type == Types.FLOAT) {
                    field.put("__type__", "number");
                } else
                {
                    field.put("__type__", "string");
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

            // 特定类型才能搜索、列举、排序
            String ft = (String) field.get("__type__");
            if (!field.containsKey("findable") && findable != null && findable.contains(ft)) {
                field.put("findable", "yes");
            }
            if (!field.containsKey("listable") && listable != null && listable.contains(ft)) {
                field.put("listable", "yes");
            }
            if (!field.containsKey("sortable") && sortable != null && sortable.contains(ft)) {
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
        while (  it.hasNext( )  ) {
            Map.Entry et = (Map.Entry)it.next();
            Map       vd = (Map ) et.getValue();
            String  type = (String) vd.get("type");

            String  name, disp, vk, tk, ak, ek, tn;

            if ("BLS_TO".equals(type)) {
                ak   = (String) vd.get("name");
                tn   = (String) vd.get("tableName" );
                vk   = (String) vd.get("foreignKey");
                name = vk;

                Model hm = db.getModel(tn != null ? tn : ak);
                Mview hb =  new  Mview(hm);
                tk   = hb.getNmKey();
                disp = hb.getTitle();
            } else {
                if (!vd.containsKey("assocs")) {
                    continue;
                }

                Map ad = (Map) ((Map) vd.get("assocs")).values().toArray()[0];
                ak   = (String) vd.get("name");
                ek   = (String) ad.get("name");
                tn   = (String) ad.get("tableName" );
                vk   = (String) vd.get("foreignKey");
                name = ak + ("HAS_ONE".equals(type) ? "." : "..") + vk;

                Model hm = db.getModel(tn != null ? tn : ek);
                Mview hb =  new  Mview(hm);
                tk   =ek+"."+hb.getNmKey();
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
                field.put("data-ak", ak);
                field.put("data-vk", vk);
                field.put("data-tk", tk);
            }

            // 特定类型才能搜索、列举、排序
            String ft = (String) field.get("__type__");
            if (!field.containsKey("findable") && findable != null && findable.contains(ft)) {
                field.put("findable", "yes");
            }
            if (!field.containsKey("listable") && listable != null && listable.contains(ft)) {
                field.put("listable", "yes");
            }
            if (!field.containsKey("sortable") && sortable != null && sortable.contains(ft)) {
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

    @Override
    public void destroy() throws Throwable {
        nmkey  = null;
        title  = null;
        fields = null;
    }

}
