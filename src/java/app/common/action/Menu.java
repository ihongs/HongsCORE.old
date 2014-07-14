package app.common.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionConfig;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单
 * @author Hong
 */
@Action
public class Menu {
    public void actionList(ActionHelper helper) throws HongsException {
        String name  = helper.getParam("c");
        String level = helper.getParam("l");
        String depth = helper.getParam("d");
        helper.printJSON(getList(name, level, depth));
    }

    public static List getList(String name, String level, String depth) throws HongsException
    {
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

        CoreLanguage lang = (CoreLanguage)
            Core.getInstance(CoreLanguage.class  );
        ActionConfig conf = new ActionConfig(name);

        return getList(lang, conf.pages, l, d, 0 );
    }

    public static List getList(CoreLanguage lang, Map<String,Map> pages, int level, int depth, int i) {
        List list = new ArrayList();

        if (i >= level + depth || pages == null) {
            return list;
        }

        for (Map.Entry item : pages.entrySet( )) {
            Map v = (Map)item.getValue();
            Map p = (Map)v.get("pages" );

            List lst = getList(lang, p, level, depth, i + 1);
            if (i >= level) {
                String u = (String)item.getKey();
                String n = (String)v.get("name");
                Map page = new HashMap();
                n = lang.translate(n);
                page.put("uri"   , u);
                page.put("name"  , n);
                page.put("menus" , lst );
                list.add( page );
            } else {
                list.addAll(lst);
            }
        }

        return list;
    }

}
