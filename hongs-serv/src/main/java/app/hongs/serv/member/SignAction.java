package app.hongs.serv.member;

import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.VerifyHelper.Wrong;
import app.hongs.action.VerifyHelper.Wrongs;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.Verify;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.Table;
import app.hongs.util.Synt;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpSession;

/**
 * 登录动作
 * @author Hongs
 */
@Action("manage/member/sign")
public class SignAction {

    @Action("create")
    @Verify(conf="sign",form="form")
    public void create(ActionHelper ah) throws HongsException {
        String appid    = Synt.declare(ah.getParameter("appid"), "_WEB_");
        String place    = Synt.declare(ah.getParameter("place"),    "");
        String username = Synt.declare(ah.getParameter("username"), "");
        String password = Synt.declare(ah.getParameter("password"), "");
               password = SignKit.getCrypt(password);

        DB        db = DB.getInstance("member");
        Table     tb = db.getTable("user");
        FetchCase fc;
        Map       ud;

        // 验证密码
        fc = new FetchCase( )
            .from   (tb.tableName)
            .select ("password, id, name")
            .where  ("username = ?", username);
        ud = db.fetchLess(fc);
        if ( ud.isEmpty  (  )) {
            CoreLocale lang = CoreLocale.getInstance( "member" );
            Map m = new HashMap();
            Map e = new HashMap();
            m.put("username", new Wrong(lang.translate("core.username.invalid")));
            e.put("errs", new Wrongs(m).getErrors());
            e.put("msg", lang.translate("core.sign.in.invalid"));
            e.put("ok", false);
            ah.reply(e);
            return;
        }
        if (!password.equals(ud.get("password"))) {
            CoreLocale lang = CoreLocale.getInstance( "member" );
            Map m = new HashMap();
            Map e = new HashMap();
            m.put("password", new Wrong(lang.translate("core.password.invalid")));
            e.put("errs", new Wrongs(m).getErrors());
            e.put("msg", lang.translate("core.sign.in.invalid"));
            e.put("ok", false);
            ah.reply(e);
            return;
        }

        String usrid = ud.get( "id" ).toString();
        String uname = ud.get("name").toString();

        // 验证区域
        Set rs = RoleSet.getInstance(usrid);
        if (0 != place.length() && !rs.contains(place)) {
            CoreLocale lang = CoreLocale.getInstance("member" );
            ah.fault ( lang.translate("core.sign.uri.invalid"));
            return;
        }

        ah.reply(SignKit.userSign(ah, appid, usrid, uname));
    }

    @Action("delete")
    public void delete(ActionHelper ah) throws HongsException {
        HttpSession sess = ah.getRequest().getSession();
        if (null == sess) {
            CoreLocale lang = CoreLocale.getInstance("member" );
            ah.fault ( lang.translate("core.sign.out.invalid"));
            return;
        }

        // 清除登录
        DB.getInstance("member")
          .getTable("user_sign")
          .delete("`sesid` = ?", sess.getId());

        // 清除会话
        ah.getRequest()
          .getSession()
          .invalidate();

        ah.reply("");
    }

}
