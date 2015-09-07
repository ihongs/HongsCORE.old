package app.hongs.serv;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.MenuSet;
import app.hongs.action.anno.Action;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用动作
 * @author Hong
 */
@Action("common/menu")
public class MenuAction {

    @Action("list")
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

        List list = MenuSet.getInstance(name).getMenuTranslated(l, d);
        Map data = new HashMap();
        data.put( "list", list );
        helper.reply(data);
    }

    @Action("cell")
    public void jump(ActionHelper helper)
    throws HongsException {
        String m = helper.getParameter("m");
        String n = helper.getParameter("n");
        if (null == m ||  "".equals(m)) {
            m = "default";
        }
        if (null == n) {
            n = "common/menu/cell.act?m=" + m;
        } else {
            n = "common/menu/cell.act?m=" + m + "&n=" + n;
        }

        MenuSet site  =  MenuSet.getInstance(m);
        Map<String, Map> menu = site.getMenu(n);
        if (menu != null &&  menu.containsKey("menus")) {
            Map<String, Map> menus = (Map) menu.get("menus");
            for (Map.Entry et : menus.entrySet()) {
                String u = (String ) et.getKey();
                if (site.chkAuth(u)) {
                    helper.redirect(Core.BASE_HREF +"/"+ u );
                    return;
                }
            }
        }

        helper.redirect(Core.BASE_HREF + "/");
    }

}
