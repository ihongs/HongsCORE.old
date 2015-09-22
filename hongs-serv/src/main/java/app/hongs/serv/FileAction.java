package app.hongs.serv;

import app.hongs.CoreConfig;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.UploadHelper;
import app.hongs.action.anno.Action;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 通用上传动作
 * @author Hongs
 */
@Action("common/file")
public class FileAction {

    @Action("upload")
    public void uploadFile(ActionHelper helper) throws HongsException {
        Map sd = new HashMap();
        Map rd = helper.getRequestData();
        int md = Synt.declare(rd.get("md"), 0);
        Set<String> sk = Synt.declare(helper.getAttribute("__UPLOAD__"), Set.class);

        if (sk == null) {
            helper.reply(sd);
            return;
        }

        if (md ==  2  ) {
            for(String pn : sk) {
                Object pv = Dict.getParam(rd, pn);
                if (pv == null) {
                    helper.fault(pn + " upload failed");
                    return;
                }
                Dict.setParam(sd, pv, pn);
            }
        } else {
            for(String pn : sk) {
                Object pv = Dict.getParam(rd, pn);
                if (pv == null) {
                    helper.fault(pn + " upload failed");
                    return;
                }
                sd.put(pn , pv);
            }
        }

        helper.reply(sd);
    }

    @Action("image/upload")
    public void uploadImage(ActionHelper helper) throws HongsException {
        String v = null;
        CoreConfig c = CoreConfig.getInstance();
        String t , e , u , p;
        t = c.getProperty("fore.upload.image.types", "image/jpeg,image/png,image/gif,image/bmp");
        e = c.getProperty("core.upload.image.extns", "jpg,png,gif,bmp");
        u = c.getProperty("core.upload.image.path" , "upload/image");
        p = c.getProperty("core.upload.image.href" , "upload/image");

        UploadHelper h = new UploadHelper();
        h.setAllowTypes(t.split(","));
        h.setAllowExtns(e.split(","));
        h.setUploadPath(p);
        h.setUploadHref(u);
        h.upload(v);
        u = h.getResultHref();
        
        Map m = new HashMap();
        m.put("href", u);
        helper.reply( m);
    }

}
