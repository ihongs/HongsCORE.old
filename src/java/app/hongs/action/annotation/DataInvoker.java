package app.hongs.action.annotation;

import app.hongs.action.ActionHelper;
import app.hongs.action.DatumsConfig;
import app.hongs.util.Tree;
import java.util.Map;
import java.util.HashMap;
import java.lang.annotation.Annotation;

/**
 * 数据追加处理器
 * @author Hong
 */
public class DataInvoker {
    public static void invoke(ActionHelper helper, ActionChain chain, Annotation anno)
    throws Throwable {
        Data datums       = (Data) anno;
        Data.TYPES   type = datums.type();
        String       conf = datums.conf();
        String[]     keys = datums.keys();

        Map<String,Object> map = new HashMap();

        DatumsConfig cnf = DatumsConfig.getInstance(conf);
        for(String key : keys) {
            String val;
            String[] arr = key.split( "=", 2 );
            key = arr[0];
            val = arr.length>1 ? arr[1] : key ;
            Object dat = cnf.getDataByKey(val);
            if ( dat != null ) {
                Tree.setArrayValue(map, key, dat);
            }
        }

        if (type == Data.TYPES.REQ) {
            Map data = helper.getRequestData();
            Tree.putAllDeep(data, map);
            chain.doAction();
        }
        else {
            chain.doAction();
            Map data = helper.getResponseData();
            if (data == null || (Boolean)data.get("__success__") == false) {
                return;
            }
            Tree.putAllDeep(data, map);
        }
    }
}
