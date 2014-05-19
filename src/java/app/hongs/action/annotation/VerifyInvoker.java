package app.hongs.action.annotation;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.action.ActionHelper;
import app.hongs.action.DatumsConfig;
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
        Verify       ann  = (Verify) anno;
        String       lang = ann.lang();
        String       data = ann.data();
        String[]    rules = ann.rules();

        CoreLanguage lng = CoreLanguage.getInstance(lang);
        Map<String, Object> dat = helper.getRequestData();
        if (data.length() > 0) {
            dat = (Map<String, Object>)Tree.getValue(dat, data);
        }
        Verifier ver = new Verifier();
        ver.setLang (lng  );
        ver.setRules(rules);
        Map<String, List<String>> errors = ver.verify4RD( dat );
        
        if (errors.isEmpty()) {
            chain.doAction();
        }
        
        dat = new HashMap();
        dat.put("__message__", lng.translate("js.form.invalid"));
        dat.put("__success__", false);
        dat.put("errors", errors);
        helper.back(dat);
    }
}
