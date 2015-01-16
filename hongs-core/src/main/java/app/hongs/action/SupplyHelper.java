package app.hongs.action;

import app.hongs.HongsException;
import app.hongs.util.Dict;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 枚举补充助手
 * @author Hong
 */
public class SupplyHelper {

    private Map<String, Map> enums;

    public SupplyHelper() {
        enums = new LinkedHashMap();
    }

    public SupplyHelper addEnum(String code, Map<String, String> opts) {
        enums.put(code, opts);
        return this;
    }

    public SupplyHelper addEnum(String code, String... args) {
        Map<String, String> opts = new HashMap();
        int i = 0;
        for(String   arg : args) {
            String[] arr = arg.split( "::" , 2 );
            if (arr.length > 1 ) {
                opts.put(arr[0], arr[1]);
            } else {
                opts.put(String.valueOf(++i), arr[0]);
            }
        }
        return addEnum(code, opts);
    }

    public SupplyHelper addEnumsByForm(String conf, String form) throws HongsException {
        Formset cnf = Formset.getInstance(conf);
        Map map  = cnf.getForm(form);
        if (map == null) return this;
        map = (Map) map.get("items");

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry et = (Map.Entry)it.next();
            Map       i2 = (Map ) et.getValue();
            String t2 = (String)i2.get("__type__");
            if (! "enum".equals(t2)) {
                continue;
            }
            String n2 = (String)et.getKey();
            String e2 = (String)i2.get( "name");
            String c2 = (String)i2.get( "conf");
            if (null == c2 || "".equals( c2 )) c2 = conf;
            Map d2  = Formset.getInstance(c2).getEnumTranslated(e2);
            if (d2 != null) {
                enums.put(n2, d2);
            }
        }

        return this;
    }

    /**
     * 填充
     * @param values 返回数据
     * @param action 1 注入data, 2 添加disp
     * @throws HongsException
     */
    public void supply(Map values, short action) throws HongsException {
        if (1 == (1 & action)) {
            Map data = (Map ) values.get("data");
            if (data == null) {
                data =  new LinkedHashMap();
                values.put("data", data);
            }
                injectData(data , enums);
        }

        if (2 == (2 & action)) {
            if (values.containsKey("info")) {
                Map        info = (Map ) values.get("info");
                injectDisp(info , enums);
            }
            if (values.containsKey("list")) {
                List<Map>  list = (List) values.get("list");
                for (Map   info :  list) {
                injectDisp(info , enums);
                }
            }
        }
    }

    private void injectData(Map data, Map maps) throws HongsException {
        Iterator it = maps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry et = (Map.Entry) it.next();
            String   key = (String)  et.getKey();
            Map      map = (Map)   et.getValue();
            List     lst = new ArrayList();
            Dict.setParam(data, lst, key );

            Iterator i = map.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                Object k  = e.getKey(  );
                Object v  = e.getValue();
                List a = new ArrayList();
                a.add( k );
                a.add( v );
                lst.add(a);
            }
        }
    }

    private void injectDisp(Map info, Map maps) throws HongsException {
        Iterator it = maps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry et = (Map.Entry) it.next();
            String   key = (String)  et.getKey();
            Map      map = (Map)   et.getValue();
            Object   val = Dict.getParam(  info , key  );
            if (val != null) {
                Dict.setParam(info, map.get(val), key +"_disp");
            }
        }
    }

}