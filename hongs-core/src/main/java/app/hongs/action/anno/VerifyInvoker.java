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
 * 参数含义:
 * md=1  一般错误结构
 * md=2  层级错误结构
 * 默认仅取第一个错误
 * 如果 action 不是 create/update
 * 则需通过参数 id 来判断是创建还是更新
 * 或设置 save 标识
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
        byte    mode = ann.mode();
        byte    mods = ann.save();
        boolean tidy = ann.tidy();

        // 准备数据
        Map<String, Object> dat = helper.getRequestData();
        Object  id  = dat.get(Cnst.ID_KEY);
        String  act = chains.getAction(  );
        if (mode == -1) {
            mode = Synt.declare(helper.getParameter( "md" ) , ( byte ) -1 );
        }
        if (mods == -1) {
            mods = act.endsWith("/update") || (null != id && !"".equals(id))
                   ? ( byte ) 1 : ( byte ) 0;
        }
        boolean prp = mode != 1 && mode != 2;
        boolean upd = mods == 1 ;

        // 识别路径
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

        // 执行校验
        try {
            VerifyHelper ver = new VerifyHelper();
            ver.addRulesByForm(conf, form);
            ver.isPrompt(prp);
            ver.isUpdate(upd);
            Map vls = ver.verify(dat);
            if (tidy) dat.clear (   );
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
                dat.put("errs",  ers  );
                dat.put("err", "Er400");
                dat.put("msg", CoreLocale.getInstance ( )
                        .translate("fore.form.invalid" ));
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
