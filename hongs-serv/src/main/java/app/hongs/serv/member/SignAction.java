package app.hongs.serv.member;

import app.hongs.Core;
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
import java.util.Map;
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

        DB        db = DB.getInstance("member");
        Table     tb;
        FetchCase fc;
        Map       ud;
        Map       xd;

        // 验证密码
        tb = db.getTable("user");
        fc = new FetchCase( )
            .from   (tb.tableName, tb.name)
            .select (".password, .id, .name")
            .where  (".username = ?", username);
        ud = db.fetchLess(fc);
        if (!password.equals(ud.get("password"))) {
            CoreLocale lang = CoreLocale.getInstance( "member" );
            Map m = new HashMap();
            Map e = new HashMap();
            m.put("password", new Wrong(lang.translate("core.username.or.password.invalid")));
            e.put("errors", new Wrongs(m).getErrors());
            e.put("msg", lang.translate("core.sign.in.invalid"));
            e.put("ok", false);
            ah.reply(e);
            return;
        }

        String appid = Synt.declare( ah.getParameter( "appid"), "web");
        String token = Core.getUniqueId();

        // 设置登录
        tb = db.getTable( "user_sign" );
        tb.delete("`user_id` = ? AND `type` = ?", ud.get("id"), appid);
        xd = new HashMap();
        xd.put("user_id", ud.get("id"));
        xd.put("type", appid);
        xd.put("sign", token);
        tb.insert(xd);

        // 设置会话
        HttpSession sess = ah.getRequest().getSession(true);
        sess.setAttribute("user", ud.get( "id" ));
        sess.setAttribute("name", ud.get("name"));
        sess.setAttribute("sign_code", token);
        sess.setAttribute("sign_type", appid);

        // 返回数据
        ud = new HashMap();
        ud.put("appid", appid);
        ud.put("token", token);
        ud.put("jsessionid", sess.getId());
        ah.reply(ud);
    }

    @Action("delete")
    public void delete(ActionHelper ah) throws HongsException {
        String appid = Synt.declare(ah.getParameter("appid"), "web");
        String token = Synt.declare(ah.getParameter("token"),  ""  );

        // 清除登录
        DB.getInstance("member")
          .getTable("user_sign")
          .delete("`code` = ? AND `type` = ?", token, appid);

        // 清除会话
        ah.getRequest()
          .getSession()
          .invalidate();

        ah.reply("");
    }

}
