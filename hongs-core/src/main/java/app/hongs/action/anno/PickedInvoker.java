package app.hongs.action.anno;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.FormSet;
import app.hongs.action.anno.FilterInvoker;
import app.hongs.action.anno.Supply;
import app.hongs.dl.MergeMore;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 关联选项处理
 * 需要遵守以下规则:
 * 选择字段命名必须为 XXX_id, XXX 为关联数据名, 获取到数据后将加入当前对象的 XXX 字段下
 * 资源的获取法则为
 * @author Hongs
 */
public class PickedInvoker implements FilterInvoker {

    @Override
    public void invoke(ActionHelper helper, ActionRunner runner, Annotation anno)
    throws HongsException {
        runner.doAction();
        Map rsp = helper.getResponseData();

        Supply ann  = (Supply) anno;
        String conf = ann.conf();
        String form = ann.form();

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
            if (! "form".equals(type)) {
                continue;
            }

            Object  data = Dict.get(rsp , name);
            if (null == data || "".equals(data)
            ||  data instanceof Map && ((Map) data).isEmpty()
            ||  data instanceof Collection && ((Collection) data).isEmpty()) {
                continue;
            }

            // 提取的路径
            String pa = (String) mt.get("data-pa");
            String tn = (String) mt.get("data-tn");
            if (null == tn || "".equals(tn)) {
                tn = name.replace("_id$","");
            }
            if (null == pa || "".equals(pa)) {
                String c = (String) mt.get("conf");
                String f = (String) mt.get("form");
                if (null == c || "".equals(c)) c = conf;
                if (null == f || "".equals(f)) f =  tn ;
                pa  =  c + "/" + f +  "/retrieve" ;
            }

            // 通常只需要获取值和名字即可
            Set sf = new HashSet();
            String vk = (String) mt.get("data-vk");
            String tk = (String) mt.get("data-tk");
            if (null != vk && !"".equals(vk)
            &&  null != tk && !"".equals(tk)) {
                sf.add( vk);
                sf.add( tk);
            } else {
                sf.add("-");
            }

            // 调用关联资源的动作获取资源
            Map rd = new HashMap();
            rd.put("-for-pick", 1);
            rd.put("rn", 0 );
            rd.put("sf", sf);
            rd.put("id", rsp.get( name ));
            ActionHelper hlp = helper.clone(  );
                         hlp.setRequestData(rd);
            ActionRunner run = new ActionRunner(pa, hlp);
                         run.doInvoke(  );
            Map sd = hlp.getRequestData();

            // 提取出数据
            Object  zd;
            boolean rp = Synt.declare(mt.get("__repeated__"), true);
            if (rp) {
                zd = sd.get("list");
            } else if (sd.containsKey("info")) {
                zd = sd.get("info");
            } else {
                zd = Dict.get( sd, "list", 0 );
            }

            if (zd == null) {
                continue;
            }

            if (data instanceof List) {
                MergeMore mm = new MergeMore((List) data);
                if (rp) {
                    mm.append((List) zd, vk, name, tn);
                } else {
                    mm.extend((List) zd, vk, name, tn);
                }
            } else {
                ((Map) data).put(tn, zd);
            }
        }
    }

}
