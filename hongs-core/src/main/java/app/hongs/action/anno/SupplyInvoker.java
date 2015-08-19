package app.hongs.action.anno;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.SupplyHelper;
import app.hongs.util.Synt;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * 枚举补充处理器
 * <pre>
 * 参数含义:
 * jd=0 表示不需要执行, jd将被置为1
 * jd=1 表示要选项数据
 * jd=2 表示要显示数据
 * </pre>
 * @author Hong
 */
public class SupplyInvoker implements FilterInvoker {
    @Override
    public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno)
    throws HongsException {
        Map   rsp;
        Map   req = helper.getRequestData();
        Object jd = req.get("jd");

        if ("0".equals(jd)) {
            jd  = "1";
            rsp = new HashMap();
        } else {
            chains.doAction(  );
            rsp = helper.getResponseData(  );
            if ( jd == null || "".equals(jd) || "-".equals(jd)
            ||  rsp == null || !Synt.declare(rsp.get("ok"), false)) {
                return;
            }
        }

        Supply ann  = (Supply) anno;
        String form = ann.form();
        String conf = ann.conf();

        if (form.length( ) == 0) {
            conf = chains.getAction( );
            int j = conf.lastIndexOf('/'/**/);
            int i = conf.lastIndexOf('/',j-1);
            form = conf.substring(1+i, j);
            conf = conf.substring(0,i);
        }

        // 填充数据
        try {
            SupplyHelper sup;
            sup = new SupplyHelper().addEnumsByForm(conf, form);
            sup.supply ( rsp , Short.parseShort(jd.toString()));
        } catch (HongsException  ex) {
            int  ec  = ex.getCode( );
            if  (ec != 0x10e8 && ec != 0x10e9 && ec != 0x10ea ) {
                throw  ex;
            }
        }

        // 返回数据
        helper.reply(rsp);
    }

}
