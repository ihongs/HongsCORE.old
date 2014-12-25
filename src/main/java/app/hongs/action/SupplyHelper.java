package app.hongs.action;

import app.hongs.HongsException;
import app.hongs.util.Tree;
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

    public SupplyHelper addEnum(String name, Map<String, String> opts) {
        enums.put(name, opts);
        return this;
    }

    public SupplyHelper addEnum(String name, String... args) {
        Map<String,String> opts = new HashMap();
        for(String   arg : args) {
            String[] arr = arg.split( "=" , 2 );
            opts.put(arr[1], arr[0]);
        }
        return addEnum(name, opts);
    }

    public SupplyHelper addEnumsByForm(String coll, String form) throws HongsException {
        CollConfig cnf = CollConfig.getInstance(coll);

        for(Object it : cnf.getForm(form).entrySet()) {
            Map.Entry et = (Map.Entry) it;
            Map    i2 = (Map ) et.getValue();
            String t2 = (String) i2.get("_type");
            if (!"enum".equals(t2)) {
                continue;
            }
            String n2 = (String) et.getKey();
            String e2 = (String) i2.get( "name");
            String c2 = (String) i2.get( "coll");
            if (null == c2 || "".equals(c2)) c2 = coll;
            Map d2  = CollConfig.getInstance(c2).getEnum(e2);
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
            String   key = (String) et.getKey();
            Map      map = (Map)  et.getValue();
            List     lst = new ArrayList();
            Tree.setValue(data, key, lst );

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
            Map.Entry et = (Map.Entry)it.next();
            String   key = (String) et.getKey();
            Map      map = (Map)  et.getValue();
            String   val = Tree.getValue(info, key).toString();
            if (val != null) {
                Tree.setValue(info, key+"_disp", map.get(val));
            }
        }
    }

}
