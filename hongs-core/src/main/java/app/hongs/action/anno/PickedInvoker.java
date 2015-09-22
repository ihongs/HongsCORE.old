package app.hongs.action.anno;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.PickedHelper;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 关联选择处理器
 * @author Hongs
 */
public class PickedInvoker implements FilterInvoker {

    @Override
    public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno)
    throws HongsException {
        /*Just do it*/ chains.doAction();
        Map     rsp  = helper.getResponseData();

        Supply  ann  = (Supply) anno;
        String  conf = ann.conf();
        String  form = ann.form();

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
            PickedHelper sup ;
            sup = new PickedHelper().addItemsByForm(conf,form);
            sup.picked ( rsp);
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
