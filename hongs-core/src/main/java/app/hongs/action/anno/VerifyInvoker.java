package app.hongs.action.anno;

import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.VerifyHelper;
import app.hongs.action.VerifyHelper.Wrongs;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据校验处理器
 * <pre>
 * 如果 action 不是 create/update, 则需要通过参数 id 来判断是创建还是更新
 * 参数 jd 为 1 一般错误结构
 * 参数 jd 为 2 层级错误结构
 * 默认仅取第一个错误
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
        boolean clean = ann.clear();

        if (form.length( ) == 0) {
            conf = chains.getAction( );
            int j = conf.lastIndexOf('/'/**/);
            int i = conf.lastIndexOf('/',j-1);
            form = conf.substring(1+i, j);
            conf = conf.substring(0,i);
        }

        // 准备数据
        Map<String, Object> dat = helper.getRequestData();
        Object  id  = dat.get("id");
        Object  jd  = dat.get("jd");
        String  act = chains.getAction();
        boolean prp =  !"1".equals(jd) && !"2".equals(jd);
        boolean upd = act.endsWith("update") || (null != id && ! "".equals(id));

        // 执行校验
        VerifyHelper ver = new VerifyHelper();
        ver.isPrompt(prp);
        ver.isUpdate(upd);
        if (null != conf &&  null != form) {
            ver.addRulesByForm(conf, form);
        }
        try {
            Map vls = ver.verify(dat);
            if (clean) dat.clear(   );
            dat.putAll( vls );
            chains.doAction();
        } catch (Wrongs err ) {
            dat = new HashMap();
            dat.put("ok",false);

            if (prp) {
                dat.put("err", "Er400");
                dat.put("msg", err.getLocalizedMessage());
            } else {
                Map ers;
                if ("2".equals(jd)) {
                    ers = err.getErrmap();
                } else {
                    ers = err.getErrors();
                }
                dat.put("errors", ers );
                dat.put("err", "Er400");
                CoreLocale lng = CoreLocale.getInstance();
                dat.put("msg", lng.translate( "fore.form.invalid" ));
            }

            helper.reply(dat);

            // Servlet 环境下设置状态码为 400(错误的请求)
            if (helper.getResponse() != null) {
                helper.getResponse().setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
}
