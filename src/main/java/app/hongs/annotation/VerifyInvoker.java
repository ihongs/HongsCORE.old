package app.hongs.annotation;

import app.hongs.action.ActionRunner;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.VerifyHelper;
import app.hongs.util.Tree;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据追加处理器
 * @author Hong
 */
public class VerifyInvoker implements ActionInvoker {
    @Override
    public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno)
    throws HongsException {
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
        VerifyHelper ver = new VerifyHelper(lng).setRule(conf, form);
        Map<String, List<String>> errors = ver.verify4RD(dat);

        // 返回错误
        if (! errors.isEmpty()) {
            dat = new HashMap();
            dat.put("__message__", lng.translate("fore.form.invalid"));
            dat.put("__success__", false);
            dat.put("errors", errors);
            helper.reply(dat);
            return;
        }

        chains.doAction();
    }
}
