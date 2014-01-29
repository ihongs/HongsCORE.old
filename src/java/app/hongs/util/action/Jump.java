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
        String[] args = helper.getRequestArgs();
        String p = args.length > 0 ? args[0] : "";
        String c = args.length > 1 ? args[1] : "";
        String u = Core.ACTION_PATH.get()+"/"+p;
        if ("".equals(c)) c = "default";
        ActionConfig conf = ActionConfig.getInstance(c);

        Map<String, Map> page = conf.getPage(u);
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
