package app.hongs.serv.member;

import app.hongs.Core;
import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.NaviMap;
import app.hongs.action.UploadHelper;
import app.hongs.action.anno.Action;
import app.hongs.db.DB;
import app.hongs.action.anno.CommitSuccess;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;

/**
 * 用户动作接口
 * @author Hongs
 */
@Action("manage/member/user")
public class UserAction {

    private app.hongs.serv.member.User model;

    public UserAction()
    throws HongsException {
        model = (User) DB.getInstance("member").getModel("user");
    }

    @Action("list")
    public void getList(ActionHelper helper)
    throws HongsException {
        Map data = model.getList(helper.getRequestData());

        // Remove the password field, don't show password in page
        List<Map> list = (List)data.get("list");
        for (Map  info :  list) {
            info.remove("password");
        }

        helper.reply(data);
    }

    @Action("info")
    public void getInfo(ActionHelper helper)
    throws HongsException {
        String id = helper.getParameter("id");
        Map data;
        if (id  != null && id.length( ) != 0) {
            data = model.getInfo(helper.getRequestData());
        } else {
            data = new HashMap();
        }

        // Remove the password field, don't show password in page
        Map info  = (Map) data.get("info");
        if (info != null) {
            info.remove("password");
        }

        // With all roles
        if (Synt.declare(helper.getParameter("-with-roles"), false)) {
            Dict.put(data, NaviMap.getInstance("manage").getRoleTranslates(), "enum", "roles..role");
        }

        helper.reply(data);
    }

    @Action("save")
    public void doSave(ActionHelper helper)
    throws HongsException {
        Map data = helper.getRequestData( );
        // Ignore empty password in update
        if("".equals(data.get("password"))) {
            data.remove("password");
        }

        // 上传头像
        UploadHelper.upload(
            data,
            new UploadHelper()
                .setUploadName("head")
                .setUploadHref("upload/member/head")
                .setUploadPath("upload/member/head")
                .setAllowExtns("jpg", "png", "gif" )
                .setAllowTypes("image/jpeg", "image/png", "image/gif")
        );

        // 缩略头像
        try {
            String fn = data.get( "head" ).toString( );
            String fm = fn.replaceFirst("\\..*?$", "");
            Builder<File> img = Thumbnails.of(fn).outputFormat("jpg");
            if ( ! fn.endsWith(".jpg")) {
                img.toFile(fm +".jpg");
            }
            img.size(16, 16).toFile(fm +"_xs.jpg");
            img.size(32, 32).toFile(fm +"_sm.jpg");
            img.size(64, 64).toFile(fm +"_md.jpg");
            img.size(96, 96).toFile(fm +"_lg.jpg");
        } catch (IOException  ex) {
            throw new HongsException.Common(ex);
        }

        CoreLocale lang = CoreLocale.getInstance().clone( );
        lang.load("member");

        String  id  = model.save(data);
        String  msg = lang.translate("core.save.user.success");

        Map info = new HashMap();
        info.put( "id" , id);
        info.put("name", data.get("name"));
        helper.reply(msg , info);
    }

    @Action("delete")
    @CommitSuccess
    public void doDelete(ActionHelper helper)
    throws HongsException {
        CoreLocale lang = CoreLocale.getInstance().clone( );
        lang.load("member");
        int     rd  = model.delete(helper.getRequestData());
        String  msg = lang.translate("core.delete.user.success", Integer.toString(rd));
        helper.reply(msg, rd);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        boolean v = model.unique(helper.getRequestData());
        helper.reply(null, v);
    }

    @Action("roles")
    public void getRoles(ActionHelper helper)
    throws HongsException {
        Map data = new HashMap();

        // 全部权限分组
        List roles = SignKit.getRoles("default");
        data.put("role_list", roles);

        // 用户动作分组
        String id = helper.getParameter("id");
        if (id != null) {
            app.hongs.serv.member.User model2 = Core.getInstance(app.hongs.serv.member.User.class);
            Set rolez = model2.getRoles(id);
            data.put("roles", rolez);
        }

        helper.reply(data);
    }
}
