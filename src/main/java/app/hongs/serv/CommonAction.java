package app.hongs.serv;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import java.util.Map;

/**
 * 通用动作
 * @author Hong
 */
@Action("common")
public class CommonAction {

    public void action_menu(ActionHelper helper)
    throws HongsException {
        String name  = helper.getParameter("c");
        String level = helper.getParameter("l");
        String depth = helper.getParameter("d");

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

        helper.print(conf.getMenu(l, d));
    }

    public void action_goto(ActionHelper helper)
    throws HongsException {
        String c = helper.getParameter("c");
        if (null == c || "".equals(c)) c = "default";
        String q = helper.getRequest().getQueryString();
        if (null == q || "".equals(q)) q = "";
        else   q = "?"+q;
        String u = Core.ACTION_NAME.get()+q;

        AuthConfig conf = AuthConfig.getInstance(c);
        Map<String, Map> page = conf.getPage(u);
        if (page != null  && page.containsKey("pages")) {
            Map<String, Map> pages = (Map) page.get("pages");
            for (Map.Entry et : pages.entrySet()) {
                String uri2 = (String)et.getKey();
                if (conf.chkAuth(uri2)) {
                    helper.redirect(Core.BASE_HREF+"/"+uri2);
                    return;
                }
            }
        }

        helper.redirect(Core.BASE_HREF + "/");
    }

}
