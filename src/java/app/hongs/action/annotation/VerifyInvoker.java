package app.hongs.action.annotation;

import app.hongs.CoreLanguage;
import app.hongs.action.ActionHelper;
import app.hongs.util.Tree;
import app.hongs.util.Verifier;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据追加处理器
 * @author Hong
 */
public class VerifyInvoker {
    public static void invoke(ActionHelper helper, ActionChain chain, Annotation anno)
    throws Throwable {
        Verify ann  = (Verify) anno;
        String lang = ann.lang();
        String conf = ann.conf();
        String form = ann.rule();
        String data = ann.data();

        // 准备数据
        Map<String, Object> dat = helper.getRequestData();
        if (data.length() > 0) {
            dat = (Map<String, Object>) Tree.getValue(dat, data);
        }

        // 开始校验
        CoreLanguage lng = CoreLanguage.getInstance(lang);
        Verifier ver = new Verifier( lng ).setRule( conf, form );
        Map<String, List<String>> errors = ver.verify4RD( dat  );

        // 返回错误
        if (! errors.isEmpty()) {
            dat = new HashMap();
            dat.put("__message__", lng.translate("js.form.invalid"));
            dat.put("__success__", false);
            dat.put("errors", errors);
            helper.back(dat);
            return;
        }

        chain.doAction();
    }
}
