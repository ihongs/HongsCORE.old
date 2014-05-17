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
public class InjectInvoker {
    public static void invoke(ActionHelper helper, ActionChain chain, Annotation anno)
    throws Throwable {
        Inject       ann  = (Inject) anno;
        Inject.TYPES type = ann.type();
        String       conf = ann.conf();
        String[]     keys = ann.keys();

        DatumsConfig cnf = DatumsConfig.getInstance(conf);
        Map<String,Object> map = new HashMap();
        for(String key : keys) {
            String val;
            String[] arr = key.split( "=", 2 );
            key = arr[0];
            val = arr.length>1 ? arr[1] : key ;
            Object dat = cnf.getDataByKey(val);
            if ( dat != null ) {
                Tree.setValue(map, key, dat);
            }
        }

        if (type == Inject.TYPES.REQ) {
            Map data = helper.getRequestData();
            Tree.setDepth(data, map);
            chain.doAction();
        }
        else {
            chain.doAction();
            Map data = helper.getResponseData();
            if (data == null || (Boolean)data.get("__success__") == false) {
                return;
            }
            Tree.setDepth(data, map);
        }
    }
}
