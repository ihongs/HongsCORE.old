package app.hongs.action.anno;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.SupplyHelper;
import static app.hongs.action.serv.ServWarder.ENTITY;
import static app.hongs.action.serv.ServWarder.MODULE;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * 枚举补充处理器
 * <pre>
 * 参数含义:
 * jd=0 表示不需要数据
 * jd=1 表示要选项数据
 * jd=2 表示要显示数据
 * id=0 表示不需要执行, 此时jd将被置为1
 * </pre>
 * @author Hong
 */
public class SupplyInvoker implements FilterInvoker {
    @Override
    public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno)
    throws HongsException {
        Map   rsp;
        Map   req = helper.getRequestData();
        String id = (String) req.get("id" );
        String jd = (String) req.get("jd" );
        if ("0".equals(id) ) {
            jd  = "1";
            rsp = new HashMap();
        } else {
            chains.doAction(  );
            rsp = helper.getResponseData();
            if (rsp == null || ! (Boolean) rsp.get("ok")
            ||  jd  == null || "".equals(jd) || "0".equals(jd)) {
                return;
            }
        }

        Supply ann  = (Supply) anno;
        String unit = ann.unit();
        String conf = ann.conf();

        if (unit.length() == 0 ) {
            unit = (String) helper.getAttribute(MODULE);
            conf = (String) helper.getAttribute(ENTITY);
        }

        // 填充数据
        SupplyHelper sup = new SupplyHelper().addEnumsByUnit(conf, unit);
        sup.supply ( rsp, Short.parseShort(jd) );
        
        // 返回数据
        helper.reply(rsp);
    }

}
