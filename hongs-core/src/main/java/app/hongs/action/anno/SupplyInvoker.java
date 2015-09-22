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
 * md=0  表示不需要执行, jd将被置为1
 * md=1  表示要选项数据
 * md=2  表示要显示数据
 * </pre>
 * @author Hong
 */
public class SupplyInvoker implements FilterInvoker {
    @Override
    public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno)
    throws HongsException {
        Supply  ann  = (Supply) anno;
        String  form = ann.form();
        String  conf = ann.conf();
        short   mode = ann.mode();

        if (mode == -1) {
            mode = Synt.declare(helper.getParameter("md"), (short) -1);
        }

        // 为 0 则不执行, 仅取 enum 数据
        Map rsp;
        if (mode ==  0) {
            mode =   1;
            rsp = new HashMap();
        } else
        if (mode == -1) {
            chains.doAction(  );
            return;
        } else {
            chains.doAction(  );
            rsp = helper.getResponseData(  );
            if (! Synt.declare(rsp.get("ok"), false)) {
                return;
            }
        }

        // 识别路径
        if (form.length() == 0 || conf.length() == 0) {
            String s; int i;
            s = chains.getAction( );
            i = s.lastIndexOf ('/');
            if (form.length() == 0)
            form = s.substring(i+1);
            if (conf.length() == 0)
            conf = s.substring(0,i);
        }

        // 填充数据
        try {
            SupplyHelper sup;
            sup = new SupplyHelper().addEnumsByForm(conf,form);
            sup.supply(rsp, mode);
        } catch (HongsException  ex) {
            int  ec  = ex.getCode( );
            if  (ec != 0x10e8 && ec != 0x10e9 && ec != 0x10ea) {
                throw  ex;
            }
        }

        // 返回数据
        helper.reply(rsp);
    }

}
