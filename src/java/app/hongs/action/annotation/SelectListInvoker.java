package app.hongs.action.annotation;

import app.hongs.CoreLanguage;
import app.hongs.action.ActionHelper;
import app.hongs.action.DatumsConfig;
import app.hongs.util.Tree;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.annotation.Annotation;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理列表中的选择数据
 * @author Hong
 */
public class SelectListInvoker {
    public static void invoke(ActionHelper helper, ActionChain chain, Annotation anno)
    throws Throwable {
        HttpServletResponse rsp2;
        DatumsWrapper rsp3;
        rsp2 = helper.response;
        rsp3 = new DatumsWrapper(rsp2);
        helper.response = rsp3;
        chain.doAction();
        helper.response = rsp2;

        Map data = helper.getResponseData();
        if (data == null || (boolean)data.get("__success__") == false)
        {
            helper.print( rsp3.toString() );
            return;
        }
        if (data.containsKey("list") == false)
        {
            return;
        }
        List<Map> list = (List) data.get("list");
        Map<String, Object> maps = new HashMap();

        /** 追加数据 **/

        SelectList datums = (SelectList) anno;
        String       conf = datums.conf();
        String       lang = datums.lang();
        String[]     keys = datums.keys();

        DatumsConfig cnf = DatumsConfig.getInstance(conf);
        CoreLanguage lng = CoreLanguage.getInstance(lang);

        for(String key : keys) {
            String val;
            String[] arr = key.split( "=", 2 );
            key = arr[0];
            val = arr.length>1 ? arr[1] : key ;
            Object dat = cnf.getDataByKey(val);
            if ( dat != null ) {
                Map map = new HashMap();
                maps.put(  key , map  );

                // 翻译选择文本
                List lst1 = (List) dat;
                for (Object obj2 : lst1) {
                    List   lst2 = (List)   obj2;
                    String val2 = lst2.get(0).toString();
                    String txt2 = lst2.get(1).toString();
                    map.put( val2, lng.translate(txt2) );
                }
            }
        }

        for (Map info : list) {
            for (Map.Entry et : maps.entrySet()) {
                String key = (String) et.getKey();
                Map    map = (Map)    et.getValue();
                String val = Tree.getArrayValue(info, key).toString(  ); // 都转成字符串, 避免匹配失败
                if (val != null) {
                    Tree.setArrayValue(info, key+"_disp", map.get(val));
                }
            }
        }

        helper.printJSON(helper.getResponseData());
    }
}
