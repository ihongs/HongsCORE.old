package app.hongs.action.anno;

import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.VerifyHelper;
import app.hongs.action.VerifyHelper.Wrongs;
import static app.hongs.action.ServWarder.ENTITY;
import static app.hongs.action.ServWarder.MODULE;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据校验处理器
 * <pre>
 * 如果 action 不是 create/update, 则需要通过参数 id 来判断是创建还是更新
 * 参数 jd 为 1 则将错误数据为复杂的层级结构
 * </pre>
 * @author Hong
 */
public class VerifyInvoker implements FilterInvoker {
    @Override
    public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno)
    throws HongsException {
        Verify ann  = (Verify) anno;
        String form = ann.form();
        String conf = ann.conf();
        boolean clean = ann.clean();

        if (form.length() == 0 ) {
            form = (String) helper.getAttribute(ENTITY);
            conf = (String) helper.getAttribute(MODULE);
        }

        // 准备数据
        Map<String, Object> dat = helper.getRequestData();
        Object  id  = dat.get("id");
        Object  jd  = dat.get("jd");
        String  act = chains.getAction();
        boolean upd = act.endsWith("update")
          || (null != id && ! "".equals(id));

        // 执行校验
        VerifyHelper ver  =  new VerifyHelper();
        if (null != conf &&  null != form) {
            ver.addRulesByForm(conf, form);
        }
        try {
            Map vls = ver.verify(dat, upd);
            if (clean) dat.clear();
            dat.putAll( vls );
            chains.doAction();
        } catch (Wrongs ex) {
            Map ers;
            if ("1".equals(jd)) {
                ers = ex.getErrmap();
            } else {
                ers = ex.getErrors();
            }

            dat = new HashMap();
            dat.put("ok",false);
            CoreLanguage  lng  =  CoreLanguage.getInstance( );
            dat.put("err",lng.translate("fore.form.invalid"));
            dat.put("errors" , ers );
            helper.reply( dat );
        }
    }
}
