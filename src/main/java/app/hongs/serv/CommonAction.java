package app.hongs.serv;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.SourceConfig;
import app.hongs.action.anno.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用动作
 * @author Hong
 */
@Action("common")
public class CommonAction {

    public void action_menu(ActionHelper helper)
    throws HongsException {
        String m = helper.getParameter("m");
        if (null == m || "".equals(m)) m = "default";

        SourceConfig conf = SourceConfig.getInstance(m );
        Map menu  =  conf.getUnit("__MENU__") != null
                  ?  conf.getRolesTranslated("__MENU__")
                  :  conf.getUnitsTranslated();

        List list = new ArrayList();
        for (Object o : menu.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            String name = (String) e.getKey();
            Map    unit = (Map ) e.getValue();
            Map info = new HashMap();
            info.put("name", name);
            info.put("disp", unit.get("_disp"));
            info.put("href", unit.get("_href"));
        }

        Map data = new HashMap();
        data.put( "list", list );
        helper.reply(data);
    }

    public void action_goto(ActionHelper helper)
    throws HongsException {
        String m = helper.getParameter("m");
        if (null == m || "".equals(m)) m = "default";
        String n = helper.getParameter("n");
        if (null == n || "".equals(n)) n = "deafult";

        SourceConfig conf = SourceConfig.getInstance(m );
        Map menu  =  conf.getUnit("__MENU__") != null
                  ?  conf.getRolesTranslated("__MENU__")
                  :  conf.getUnitsTranslated();

        if (!menu.isEmpty()) {
            if (menu.containsKey(n)) {
                menu = (Map) menu.get( n );
            } else {
                menu = (Map) new ArrayList(menu.values()).get(0);
            }
            n = (String) menu.get("_href");
        } else {
            n = "";
        }

        helper.redirect(Core.BASE_HREF+"/"+n);
    }

}
