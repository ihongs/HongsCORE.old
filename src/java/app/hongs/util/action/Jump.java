package app.hongs.util.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionConfig;
import app.hongs.action.ActionHelper;
import java.util.Map;

/**
 * 跳转
 * @author Hongs
 */
public class Jump {
    public void actionTo(ActionHelper helper)
    throws HongsException {
        String p = helper.getParameter("page");
        String c = helper.getParameter("conf");
        if ( c == null || "".equals(c) ) c = "default" ;
        String uri = Core.ACTION_PATH.get()+"?page="+p ; 
        ActionConfig conf = ActionConfig.getInstance(c);

        Map<String, Map> page = conf.getPage(uri);
        if (page != null  && page.containsKey("pages")) {
            Map<String, Map> pages = (Map) page.get("pages");
            for (Map.Entry et : pages.entrySet()) {
                String uri2 = (String)et.getKey();
                if (conf.chkAuth(uri2)) {
                    helper.print302Code(Core.BASE_HREF+uri2);
                    return;
                }
            }
        }

        helper.print302Code(Core.BASE_HREF + "/");
    }
}
