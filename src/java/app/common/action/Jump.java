package app.common.action;

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
        String c = helper.getParameter("c");
        if (null == c || "".equals(c)) c="default";
        String q = helper.request.getQueryString();
        if (null == q || "".equals(q)) q="";
        else   q = "?"+q;
        String u = Core.ACTION_PATH.get()+q;
      
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
