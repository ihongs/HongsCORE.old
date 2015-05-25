package app.hongs.dl.anno;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.FormSet;
import app.hongs.action.anno.FilterInvoker;
import app.hongs.action.anno.Supply;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 关联选项处理
 * @author Hongs
 */
public class PickedInvoker implements FilterInvoker {

    @Override
    public void invoke(ActionHelper helper, ActionRunner runner, Annotation anno)
    throws HongsException {
        runner.doAction();
        Map rsp = helper.getResponseData();
        
        Supply ann  = (Supply) anno;
        String form = ann.form();
        String conf = ann.conf();

        FormSet cnf = FormSet.getInstance(conf);
        Map map  = cnf.getForm(form);
        if (map == null) return ;

        FormSet dfs = FormSet.getInstance("default");
        Map tps  = dfs.getEnum("__types__");

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry et = (Map.Entry)it.next();
            Map       mt = (Map ) et.getValue();
            String  name = (String) et.getKey();
            String  type = (String) mt.get("__type__");
                    type = (String) tps.get(type); // 类型别名转换
            if (! "enum".equals(type)) {
                continue;
            }
            
            Object  data = rsp.get(name);
            if (null == data || "".equals(data)
            ||  data instanceof Map && ((Map) data).isEmpty()
            ||  data instanceof Collection && ((Collection) data).isEmpty()) {
                continue;
            }
            
            String xonf = (String) mt.get("conf");
            String xorm = (String) mt.get("form");
            if (null == xonf || "".equals( xonf )) xonf = conf;
            if (null == xorm || "".equals( xorm )) xorm = name;
            
            // 调用关联资源的动作获取资源
            Map req = new HashMap();
            req.put("id", rsp.get( name ));
            req.put("rn",  0 );
            req.put("cs", "-");
            String act = xonf + "/" + xorm + "/retrieve" ;
            ActionHelper hlp = new ActionHelper(req, null, null, helper.getResponseWrtr());
            ActionRunner run = new ActionRunner(act, hlp);
                         run.doInvoke(   );
            Map dat = hlp.getRequestData();
            
            // 提取出数据
            Object obj;
            if (Synt.declare(mt.get("__repeated__"), true)) {
                obj =  dat.get("list");
            } else if (dat.containsKey("info")) {
                obj =  dat.get("info");
            } else {
                obj = Dict.get(dat, "list", 0);
            }
            
            map.put(name.replace( "_id$", "" ), obj);
        }
    }
    
}
