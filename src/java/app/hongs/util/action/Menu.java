package app.hongs.util.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionConfig;
import app.hongs.action.ActionHelper;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 菜单
 * @author Hong
 */
public class Menu {
    public void actionList(ActionHelper helper) throws HongsException {
        String name  = helper.getParameter("name" );
        String level = helper.getParameter("level");
        String depth = helper.getParameter("depth");
        int l, d;
        if (name  == null || name .length() == 0) {
            name  = "default";
        }
        if (level == null || level.length() == 0) {
            l = 1;
        }
        else {
            l = Integer.parseInt(level);
        }
        if (depth == null || depth.length() == 0) {
            d = 1;
        }
        else {
            d = Integer.parseInt(depth);
        }

        CoreLanguage lang = (CoreLanguage)
        Core.getInstance("app.hongs.CoreLanguage");
        ActionConfig conf = new ActionConfig(name);

        helper.printJSON(buildMenu(lang, conf.pages, l, d, 0));
    }

    private List buildMenu(CoreLanguage lang, Map<String,Map> pages, int level, int depth, int i) {
        List list = new ArrayList();

        if (i >= level + depth || pages == null) {
            return list;
        }

        for (Map.Entry item : pages.entrySet( )) {
            Map v = (Map)item.getValue();
            Map p = (Map)v.get("pages" );

            if (i >= level) {
                List lst = buildMenu(lang, p, level, depth, i + 1);
                String u = (String)item.getKey();
                String n = (String)v.get("name");
                Map page = new HashMap();
                n = lang.translate(n);
                page.put("uri"   , u);
                page.put("name"  , n);
                page.put("menus" , lst );
                list.add( page );
            }
            else {
                List lst = buildMenu(lang, p, level, depth, i + 1);
                list.addAll(lst);
            }
        }

        return list;
    }

}
