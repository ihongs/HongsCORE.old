package app.hongs.serv;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.Sitemap;
import app.hongs.action.anno.Action;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用动作
 * @author Hong
 */
@Action("common")
public class CommonAction {

    @Action("goto")
    public void action_goto(ActionHelper helper)
    throws HongsException {
        String m = helper.getParameter("m");
        if (null == m ||  "".equals(m)) m = "default";
        String x = helper.getParameter("x");
        if (null != x) {
            x  = "common/goto.act?m=" + m + "&x=" + x;
        } else {
            x  = "common/goto.act?m=" + m;
        }

        Sitemap site  =  Sitemap.getInstance(m);
        Map<String, Map> page = site.getPage(x);
        if (page != null  && page.containsKey("pages")) {
            Map<String, Map> pages = (Map) page.get("pages");
            for (Map.Entry et : pages.entrySet()) {
                String uri2 = (String)et.getKey();
                if (site.chkAuth(uri2)) {
                    helper.redirect( Core.BASE_HREF + uri2 );
                    return;
                }
            }
        }

        helper.redirect(Core.BASE_HREF + "/");
    }

    @Action("menu")
    public void menu(ActionHelper helper)
    throws HongsException {
        String name  = helper.getParameter("m");
        String level = helper.getParameter("l");
        String depth = helper.getParameter("d");

        int l, d;
        if (name  == null || name .length() == 0) {
            name  = "default";
        }
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

        List list = Sitemap.getInstance(name).getMenu(l, d);
        Map data = new HashMap();
        data.put( "list", list );
        helper.reply(data);
    }

}
