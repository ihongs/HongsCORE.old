package app.common.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.AuthConfig;
import app.hongs.action.annotation.Action;
import java.util.Map;

/**
 * 菜单
 * @author Hong
 */
@Action
public class Menu {

    public void actionList(ActionHelper helper)
    throws HongsException {
        String name  = helper.getParameter("c");
        String level = helper.getParameter("l");
        String depth = helper.getParameter("d");
        String langName = helper.getParameter("ln");
        String langType = helper.getParameter("lt");

        int l , d;
        if (level == null || level.length() == 0) {
            l = 1;
        } else {
            l = Integer.parseInt(level);
        }
        if (depth == null || depth.length() == 0) {
            d = 1;
        } else {
            d = Integer.parseInt(depth);
        }

        if (name  == null || name .length() == 0) {
            name  = "default";
        }
        AuthConfig conf = AuthConfig.getInstance(name);

        if (langType != null && langType.length() != 0) {
            langType = CoreLanguage.getAcceptLanguage(langType);
        }
        if (langType == null || langType.length() == 0) {
            langType = Core.ACTION_LANG.get();
        }
        if (langName == null || langName.length() == 0) {
            langName = "default";
        }
        CoreLanguage lang = new CoreLanguage(langName,langType);

        helper.print(conf.getNavList(lang, l, d));
    }

    public void actionGoto(ActionHelper helper)
    throws HongsException {
        String c = helper.getParameter("c");
        if (null == c || "".equals(c)) c = "default";
        String q = helper.getRequest().getQueryString();
        if (null == q || "".equals(q)) q = "";
        else   q = "?"+q;
        String u = Core.ACTION_PATH.get()+q;

        AuthConfig conf = AuthConfig.getInstance(c);
        Map<String, Map> page = conf.getPage(u);
        if (page != null  && page.containsKey("pages")) {
            Map<String, Map> pages = (Map) page.get("pages");
            for (Map.Entry et : pages.entrySet()) {
                String uri2 = (String)et.getKey();
                if (conf.chkAuth(uri2)) {
                    helper.print302(Core.BASE_HREF+uri2);
                    return;
                }
            }
        }

        helper.print302(Core.BASE_HREF + "/");
    }

}
