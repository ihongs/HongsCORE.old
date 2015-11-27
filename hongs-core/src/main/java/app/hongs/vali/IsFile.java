package app.hongs.vali;

import app.hongs.Core;
import app.hongs.action.UploadHelper;
import app.hongs.util.Synt;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class IsFile extends Rule {
    @Override
    public Object verify(Object value) throws Wrong {
        if (value == null || "".equals(value)) {
            return   null; // 允许为空
        }

        // 忽略远程地址
        if (Synt.declare(params.get("pass-remote"), false)) {
            if (value.toString().matches("^https?://")) {
                return value;
            }
        }

        // 下载远程文件
        if (Synt.declare(params.get("down-remote"), null)) {
            if (value.toString().matches("^https?://")) {
                String x = (String) params.get("temp");
                if (x == null && "".equals(x)) {
                    x  = Core.DATA_PATH  + "/upload/";
                }
                stores( value.toString() , x );
            }
        }

        String name = Synt.declare(params.get("name"), String.class);
        if (name == null || "".equals(name)) {
            name = Synt.declare(params.get("__name__"), "");
        }

        UploadHelper u = new UploadHelper();
        u.setUploadName(name);
        String x;
        x = (String) params.get( "href" );
        if (x != null) u.setUploadHref(x);
        x = (String) params.get( "path" );
        if (x != null) u.setUploadPath(x);
        x = (String) params.get( "type" );
        if (x != null) u.setAllowTypes(x.split(","));
        x = (String) params.get( "extn" );
        if (x != null) u.setAllowExtns(x.split(","));

        x = (String) params.get( "temp" );
        if (x != null && !"".equals( x )) {
            u.upload(x, value.toString());
        } else {
            u.upload(   value.toString());
        }

        return u.getResultHref();
    }

    private void stores(String href, String path) throws Wrong {
        URL url = null;
        try {
            url = new URL(href);
        } catch (MalformedURLException ex) {
            throw new Wrong(ex, "file.url.error", href);
        }

           URLConnection cnn ;
             InputStream ins = null;
        FileOutputStream out = null;
        try {
            cnn = url.openConnection( );
            ins = cnn.getInputStream( );
            out = new FileOutputStream(path);

            byte[] buf = new byte[1204];
            int    ovr ;
            while((ovr = ins.read(buf))!=-1) {
                out.write(buf, 0, ovr );
            }
        } catch (IOException ex) {
            throw new Wrong(ex, "file.can.not.down" , href, path);
        } finally {
        if (out != null) {
        try {
            out.close( );
        } catch (IOException ex) {
            throw new Wrong(ex, "file.can.not.close", path);
        }
        }
        if (ins != null) {
        try {
            ins.close( );
        } catch (IOException ex) {
            throw new Wrong(ex, "file.can.not.close", href);
        }
        }
        }
    }
}
