package app.hongs.action.anno;

import app.hongs.Cnst;
import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.VerifyHelper;
import app.hongs.action.VerifyHelper.Wrongs;
import app.hongs.util.Synt;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

/**
 * 数据校验处理器
 * <pre>
 * 如果 action 不是 create/update, 则需要通过参数 id 来判断是创建还是更新
 * 参数含义:
 * md=1  一般错误结构
 * md=2  层级错误结构
 * 默认仅取第一个错误
 * </pre>
 * @author Hong
 */
public class VerifyInvoker implements FilterInvoker {
    @Override
    public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno)
    throws HongsException {
        Verify  ann  = (Verify) anno;
        String  form = ann.form();
        String  conf = ann.conf();
        short   mode = ann.mode();
        boolean tidy = ann.tidy();

        if (mode == -1) {
            mode = Synt.declare(helper.getParameter("md"), (short) -1);
        }

        if (form.length() == 0 || conf.length() == 0) {
            String s; int i;
            s = chains.getAction( );
            i = s.lastIndexOf ('/');
            s = s.substring (0 , i);
            i = s.lastIndexOf ('/');
            if (form.length() == 0)
                form = s.substring(i + 1);
            if (conf.length() == 0)
                conf = s.substring(0 , i);
        }

        // 准备数据
        Map<String, Object> dat = helper.getRequestData();
        Object  id  = dat.get(Cnst.ID_KEY);
        String  act = chains.getAction(  );
        boolean prp = mode != 1 && mode != 2;
        boolean upd = act.endsWith("/update") || (null != id && !"".equals(id));

        // 执行校验
        try {
            VerifyHelper ver = new VerifyHelper( ).addRulesByForm( conf, form );
            ver.isPrompt(prp);
            ver.isUpdate(upd);
            Map vls = ver.verify(dat);
            if (tidy) dat.clear();
            dat.putAll(  vls);
        } catch (Wrongs  err) {
            dat = new HashMap();
            dat.put("ok",false);

            if (prp) {
                dat.put("err", "Er400");
                dat.put("msg", err.getLocalizedMessage());
            } else {
                Map ers;
                if (mode == 2) {
                    ers = err.getErrmap();
                } else {
                    ers = err.getErrors();
                }
                dat.put("errors", ers );
                dat.put("err", "Er400");
                CoreLocale lng = CoreLocale.getInstance();
                dat.put("msg", lng.translate("fore.form.invalid"));
            }

            helper.reply(dat);

            // Servlet 环境下设置状态码为 400 (错误的请求)
            if (helper.getResponse() != null) {
                helper.getResponse().setStatus(SC_BAD_REQUEST);
            }

            return;
        } catch (HongsException  ex) {
            int  ec  = ex.getCode( );
            if  (ec != 0x10e8 && ec != 0x10e9 && ec != 0x10ea) {
                throw  ex;
            }
        }

        chains.doAction();
    }
}
