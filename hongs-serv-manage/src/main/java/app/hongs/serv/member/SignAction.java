package app.hongs.serv.member;

import app.hongs.CoreLocale;
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
import javax.servlet.http.HttpSession;

/**
 * 登录动作
 * @author Hongs
 */
@Action("hongs/member/sign")
public class SignAction {

    @Action("create")
    public void create(ActionHelper ah) throws HongsException {
        String username = Synt.declare(ah.getParameter("username"), "");
        String password = Synt.declare(ah.getParameter("password"), "");

        password = Sign.getCrypt(password);
        ah.getRequestData().put("password", password);

        FetchCase fc = new FetchCase();
        fc.select(".password, .id, .name");
        fc.where (".username = ?", username );
        fc.setOption("ASSOCS", new HashSet());

        Table um = DB.getInstance("member").getTable("user");
        Map   ud = um.fetchLess(fc);
        if (!password.equals(ud.get("password"))) {
            CoreLocale lang = CoreLocale.getInstance("member");
            Map m = new HashMap();
            Map e = new HashMap();
            m.put("password", new Wrong(lang.translate("core.username.or.password.invalid")));
            e.put("errors", new Wrongs(m).getErrors());
            e.put("msg", lang.translate("core.sign.in.invalid"));
            e.put("ok", false);
            ah.reply(e);
            return;
        }

        // 获取用户权限
        User user = new User();
        Set<String> roles = user.getRoles((String) ud.get("id"));

        // 设置用户会话
        HttpSession sess = ah.getRequest().getSession(true);
        sess.setAttribute("user", ud.get( "id" ));
        sess.setAttribute("name", ud.get("name"));
        sess.setAttribute("roles", roles);

        Map data = new HashMap();
        data.put("jsessionid", sess.getId());
        ah.reply(data);
    }

    @Action("delete")
    public void delete(ActionHelper ah) {
        // 清除用户会话
        HttpSession sess = ah.getRequest().getSession();
        sess.invalidate( );
        ah.reply("");
    }

}
