package app.hongs.serv.member;

import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.NaviMap;
import app.hongs.action.UploadHelper;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.CommitSuccess;
import app.hongs.db.DB;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;

/**
 * 用户动作接口
 * @author Hongs
 */
@Action("manage/member/user")
public class UserAction {

    private final User model;

    public UserAction()
    throws HongsException {
        model = (User) DB.getInstance("member").getModel("user");
    }

    @Action("list")
    public void getList(ActionHelper helper)
    throws HongsException {
        Map rd = helper.getRequestData();
        rd = model.getList(rd);

        // Remove the password field, don't show password in page
        List<Map> list = (List) rd.get("list");
        for (Map  info :  list) {
            info.remove("password");
        }

        helper.reply(rd);
    }

    @Action("info")
    public void getInfo(ActionHelper helper)
    throws HongsException {
        Map rd = helper.getRequestData();
        String id = helper.getParameter("id");
        String wr = helper.getParameter("-with-roles");

        if ( id != null && id.length() != 0 ) {
            rd = model.getInfo(rd);
        } else {
            rd =  new  HashMap(  );
        }

        // Remove the password field, don't show password in page
        Map info  = (Map) rd.get("info");
        if (info != null) {
            info.remove("password");
        }

        // With all roles
        if (Synt.declare(wr, false)) {
            List rs = NaviMap.getInstance("manage").getRoleTranslated();
            Dict.put(rd, rs, "enum", "roles..role");
        }

        helper.reply(rd);
    }

    @Action("save")
    public void doSave(ActionHelper helper)
    throws HongsException {
        Map rd = helper.getRequestData();

        if (rd.containsKey("head")) {
            // 上传头像
            UploadHelper.upload(rd,
                new UploadHelper()
                    .setUploadName("head")
                    .setUploadHref("upload/member/head")
                    .setUploadPath("upload/member/head")
                    .setAllowExtns("jpg", "png", "gif" )
                    .setAllowTypes("image/jpeg", "image/png", "image/gif")
            );

            // 缩略头像
            try {
                String fn = rd.get( "head" ).toString( );
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
        }

        // Ignore empty password in update
        if ("".equals(rd.get("password"))) {
            rd.remove("password" );
        }

        String id = model.save(rd);

        rd = new HashMap();
        rd.put( "id" , id);
        rd.put("name", rd.get("name"));
        rd.put("head", rd.get("head"));
        rd.put("username", rd.get("username"));

        CoreLocale  ln = CoreLocale.getInstance().clone( );
                    ln.load("member" );
        String ms = ln.translate("core.save.user.success");
        helper.reply(ms, rd);
    }

    @Action("delete")
    @CommitSuccess
    public void doDelete(ActionHelper helper)
    throws HongsException {
        Map rd = helper.getRequestData();
        int rn = model.delete(rd);
        CoreLocale  ln = CoreLocale.getInstance().clone( );
                    ln.load("member" );
        String ms = ln.translate("core.delete.user.success", Integer.toString(rn));
        helper.reply(ms, rn);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        Map rd = helper.getRequestData();
        boolean rv = model.unique(rd);
        helper.reply("", rv);
    }

}
