package app.hongs.serv.manage;

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
               password = Sign.getCrypt(password);

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

        ah.reply(Sign.userSign(ah, appid, usrid, uname));
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
    public void doUpdate(ActionHelper helper)
    throws HongsException {
        Map rd = helper.getRequestData();
        String id = (String) helper.getSessibute("uid");

        // 禁止危险修改
        rd.put("id", id);
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
                po = Sign.getCrypt(po);
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
}
