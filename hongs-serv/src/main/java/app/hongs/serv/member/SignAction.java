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
        DB        db = DB.getInstance("member");
        Table     tb;
        FetchCase fc;
        Map       ud;
        Map       xd;

        String authcode = Synt.declare(ah.getParameter("appid"), "_WEB_");
        String username = Synt.declare(ah.getParameter("username"), "");
        String password = Synt.declare(ah.getParameter("password"), "");
               password = SignKit.getCrypt(password);

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
            m.put("password", new Wrong(lang.translate("core.password.invalid")));
            e.put("errors", new Wrongs(m).getErrors());
            e.put("msg", lang.translate("core.sign.in.invalid"));
            e.put("ok", false);
            ah.reply(e);
            return;
        }

        // 验证应用
        if (!authcode.startsWith("_")) {
            fc = new FetchCase( )
                .from   (tb.tableName, tb.name)
                .select (".id")
                .where  (".id = ?", authcode);
            xd = db.fetchLess(fc);
            if (xd.isEmpty()) {
                CoreLocale lang = CoreLocale.getInstance( "member" );
                Map m = new HashMap();
                Map e = new HashMap();
                m.put("platform" , new Wrong(lang.translate("core.platform.invalid")));
                e.put("errors", new Wrongs(m).getErrors());
                e.put("msg", lang.translate("core.sign.in.invalid"));
                e.put("ok", false);
                ah.reply(e);
                return;
            }
        }

        HttpSession sess = ah.getRequest().getSession(true);
        String  sesscode = sess.getId();

        // 设置登录
        tb = db.getTable( "user_sign" );
        tb.delete("`user_id` = ? AND `auth` = ?", ud.get("id"), authcode);
        xd = new HashMap();
        xd.put("user_id", ud.get("id"));
        xd.put("auth", authcode);
        xd.put("sess", sesscode);
        tb.insert(xd);

        // 设置会话
        sess.setAttribute("stime"   , System.currentTimeMillis());
        sess.setAttribute("appid"   , authcode);
        sess.setAttribute("id"      , ud.get( "id" ));
        sess.setAttribute("name"    , ud.get("name"));

        // 返回数据
        ud = new HashMap();
        ud.put("token", sesscode);
        ah.reply(ud);
    }

    @Action("delete")
    public void delete(ActionHelper ah) throws HongsException {
        String authcode = (String) ah.getSessibute("authcode" );
        String userId   = (String) ah.getSessibute( "user_id" );

        if (authcode == null || userId == null) {
            CoreLocale lang = CoreLocale.getInstance("member" );
            ah.fault ( lang.translate("core.sign.out.invalid"));
            return;
        }

        // 清除登录
        DB.getInstance("member")
          .getTable("user_sign")
          .delete("`user_id` = ? AND `auth` = ?", userId, authcode);

        // 清除会话
        ah.getRequest()
          .getSession()
          .invalidate();

        ah.reply("");
    }

}
