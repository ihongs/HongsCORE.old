package app.hongs.db.serv;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.VerifyHelper.Wrong;
import app.hongs.action.VerifyHelper.Wrongs;
import app.hongs.action.anno.Action;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.Table;
import app.hongs.util.Synt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 登录动作
 * @author Hongs
 */
@Action("hcum/sign")
public class HcumSignAction {

    @Action("in")
    public void in(ActionHelper ah) throws HongsException {
        String username = Synt.declare(ah.getParameter("username"), "");
        String password = Synt.declare(ah.getParameter("password"), "");

        FetchCase fc = new FetchCase();
        fc.select(".password, .id");
        fc.where (".username = ?", ah.getParameter( "username" ));
        fc.setOption("ASSOCS", new HashSet());

        Table um = DB.getInstance("hcum").getTable("a_hcum_user");
        Map   ud = um.fetchLess(fc);
        if (!password.equals(ud.get("password"))) {
            Map m = new HashMap();
            m.put("password", new Wrong("用户名或密码错误"));
            throw new Wrongs( m );
        }

        Set<String> r = new HcumUser().getRoles((String) ud.get("id"));

        ah.getRequest().getSession(true).setAttribute("roles", r);
        ah.reply(true );
    }

    @Action("out")
    public void out(ActionHelper ah) {
        ah.getRequest().getSession(/**/).removeAttribute("roles");
        ah.reply(true );
    }

}
