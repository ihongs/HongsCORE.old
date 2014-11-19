package app.hongs.annotation;

import app.hongs.action.ActionCaller;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.CollConfig;
import app.hongs.util.Tree;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据追加处理器
 * @author Hong
 */
public class InjectInvoker implements ActionInvoker {
    @Override
    public void invoke(ActionHelper helper, ActionCaller chains, Annotation anno)
    throws HongsException {
        Inject       ann  = (Inject) anno;
        Inject.TYPES type = ann.type();
        String       conf = ann.conf();
        String[]     keys = ann.keys();

        CollConfig cnf = CollConfig.getInstance(conf);
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
            Tree.putDepth(data, map);
            chains.doAction();
        }
        else {
            chains.doAction();
            Map data = helper.getResponseData();
            if (data == null || (Boolean)data.get("__success__") == false) {
                return;
            }
            Tree.putDepth(data, map);
        }
    }
}
