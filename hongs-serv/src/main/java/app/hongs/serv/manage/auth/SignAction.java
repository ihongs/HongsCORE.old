package app.hongs.serv.manage.auth;

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
import app.hongs.serv.member.User;
import app.hongs.serv.member.UserAction;
import app.hongs.util.Synt;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpSession;

/**
 * 登录动作
 * @author Hongs
 */
@Action("manage/sign")
public class SignAction {

    @Action("create")
    @Verify(conf="sign",form="sign")
    public void create(ActionHelper ah) throws HongsException {
        String appid    = Synt.declare(ah.getParameter("appid"), "_WEB_");
        String place    = Synt.declare(ah.getParameter("place"),    "");
        String username = Synt.declare(ah.getParameter("username"), "");
        String password = Synt.declare(ah.getParameter("password"), "");
               password = AuthKit.getCrypt(password);

        DB        db = DB.getInstance("member");
        Table     tb = db.getTable("user");
        FetchCase fc;
        Map       ud;

        // 验证密码
        fc = new FetchCase( )
            .from   (tb.tableName)
            .select ("password, id, name, head, mtime")
            .where  ("username = ?", username);
        ud = db.fetchLess(fc);
        if ( ud.isEmpty() ) {
            CoreLocale lang = CoreLocale.getInstance( "member" );
            Map m = new HashMap();
            Map e = new HashMap();
            m.put("username", new Wrong("core.username.invalid").setLocalizedSection("member"));
            e.put("errs", new Wrongs(m).getErrors());
            e.put("msg", lang.translate("core.sign.in.invalid"));
            e.put("ok", false);
            ah.reply(e);
            return;
        }
        if (! password.equals(ud.get("password")) ) {
            CoreLocale lang = CoreLocale.getInstance( "member" );
            Map m = new HashMap();
            Map e = new HashMap();
            m.put("password", new Wrong("core.password.invalid").setLocalizedSection("member"));
            e.put("errs", new Wrongs(m).getErrors());
            e.put("msg", lang.translate("core.sign.in.invalid"));
            e.put("ok", false);
            ah.reply(e);
            return;
        }

        String usrid = (String) ud.get( "id" );
        String uname = (String) ud.get("name");
        String uhead = (String) ud.get("head");
        long   utime = Synt.declare(ud.get("mtime"), 0L) * 1000;

        // 验证区域
        Set rs = RoleSet.getInstance(usrid);
        if (0 != place.length() && !rs.contains(place)) {
            CoreLocale lang = CoreLocale.getInstance("member" );
            ah.fault ( lang.translate("core.sign.uri.invalid"));
            return;
        }

        ah.reply(AuthKit.userSign(ah, appid, usrid, uname, uhead, utime));
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

    @Action("user/update")
    @Verify(conf="sign",form="info",save=1,clean=true)
    public void mineUpdate(ActionHelper helper)
    throws HongsException {
        Map rd = helper.getRequestData();
        String id = (String) helper.getSessibute("uid");

        // 禁止危险修改
        rd.put( "id", id );
        rd.remove("roles");
        rd.remove("rtime");
        rd.remove("mtime");
        rd.remove("ctime");
        rd.remove("state");

        // 验证原始密码
        String pw = (String) rd.get("password");
        String po = (String) rd.get("passolde");
        if (pw != null && !"".equals(pw)) {
            Map xd = new HashMap();
            Map ed = new HashMap();
            xd.put("errs", ed);
            xd.put("ok", false);
            xd.put("msg", CoreLocale.getInstance().translate("fore.form.invalid"));
            if (po != null && !"".equals(po)) {
                Map row = DB.getInstance("member").getTable("user").fetchCase()
                    .where ("id = ?", id)
                    .select( "password" )
                    .one();
                po = AuthKit.getCrypt(po);
                if (! po.equals(row.get("password")) ) {
                    ed.put("passolde", "旧密码不正确");
                    helper.reply(xd);
                    return;
                }
            } else {
                    ed.put("passolde", "请填写旧密码");
                    helper.reply(xd);
                    return;
            }
        }

        UserAction ua = new UserAction();
        ua.doSave(helper);
    }

    @Action("user/retrieve")
    public void mineRetrieve(ActionHelper helper)
    throws HongsException {
        Map rd = helper.getRequestData();
        String id = (String) helper.getSessibute("uid");
        rd.put( "id", id );

        UserAction ua = new UserAction();
        ua.getInfo(helper);
    }

}
