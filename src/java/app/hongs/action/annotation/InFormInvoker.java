package app.hongs.action.annotation;

import app.hongs.CoreLanguage;
import app.hongs.action.ActionHelper;
import app.hongs.action.DatumsConfig;
import app.hongs.util.Tree;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.lang.annotation.Annotation;

/**
 * 追加表单中的选择数据
 * @author Hong
 */
public class InFormInvoker {
    public static void invoke(ActionHelper helper, ActionChain chain, Annotation anno)
    throws Throwable {
        chain.doAction();

        Map data = helper.getResponseData();
        if (data == null || (Boolean)data.get("__success__") == false)
        {
            return;
        }
        if (data.containsKey("data") == false)
        {
            data.put("data", new HashMap());
        }
            data = (Map) data.get( "data" );

        /** 追加数据 **/

        InForm   ann  = (InForm) anno;
        String   conf = ann.conf();
        String   lang = ann.lang();
        String[] keys = ann.keys();

        DatumsConfig cnf = DatumsConfig.getInstance(conf);
        CoreLanguage lng = CoreLanguage.getInstance(lang);

        for(String key : keys) {
            String val;
            String[] arr = key.split( "=", 2 );
            key = arr[0];
            val = arr.length>1 ? arr[1] : key ;
            Object dat = cnf.getDataByKey(val);
            if ( dat != null ) {
                List lst = new ArrayList((List) dat);
                Tree.setValue(data , key , lst);

                // 翻译选择文本
                for (Object obj2 : lst) {
                    List   lst2 = (List) obj2;
                    String txt2 = lst2.get(1).toString();
                    lst2.set( 1, lng.translate( txt2 ) );
                }
            }
        }
    }
}
