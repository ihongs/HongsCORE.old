package app.hongs.action.annotation;

import app.hongs.action.ActionHelper;
import app.hongs.action.DatumsConfig;
import app.hongs.util.Tree;
import java.util.Map;
import java.util.HashMap;
import java.lang.annotation.Annotation;
import javax.servlet.http.HttpServletResponse;

/**
 * 数据追加处理器
 * @author Hong
 */
public class DatumsInvoker {
    public static void invoke(ActionHelper helper, ActionChain chain, Annotation anno)
    throws Throwable {
        Datums datums = (Datums) anno;
        Datums.TYPES type = datums.type();
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

        if (type == Datums.TYPES.REQ ) {
            Map data = helper.getRequestData();
            Tree.putAllDeep(data, map);
            chain.doAction ();
            return;
        }

        /** 输出过滤 **/

        HttpServletResponse rsp2;
        DatumsWrapper rsp3;
        rsp2 = helper.response;
        rsp3 = new DatumsWrapper(rsp2);
        helper.response = rsp3;
        chain.doAction();
        helper.response = rsp2;

        Map data = helper.getResponseData( );
        if (data != null
        && (data.get("__success__") == null
        ||  data.get("__success__") == true))
        {
            Tree.putAllDeep (data,map);
            helper.printJSON(  data  );
        }
        else {
            helper.print(  rsp3.toString()  );
        }
    }
}
