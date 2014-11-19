package app.hongs.annotation;

import app.hongs.action.ActionCaller;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.CollConfig;
import app.hongs.util.Tree;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 追加表单中的选择数据
 * @author Hong
 */
public class InFormInvoker implements ActionInvoker {
    @Override
    public void invoke(ActionHelper helper, ActionCaller chains, Annotation anno)
    throws HongsException {
        chains.doAction();

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

        CollConfig cnf = CollConfig.getInstance(conf);
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
