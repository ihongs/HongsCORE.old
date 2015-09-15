package app.hongs.action;

import app.hongs.Core;
import app.hongs.util.Dict;
import app.hongs.util.Text;
import eu.medsea.mimeutil.MimeUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.fileupload.util.Streams;

/**
 * 文件上传助手
 * @author Hongs
 */
public class UploadHelper {
    private String uploadHref = "upload";
    private String uploadPath = "upload";
    private String uploadName = null;
    private String resultName = null;
    private Set<String> allowTypes = null;
    private Set<String> allowExtns = null;

    public UploadHelper setUploadPath(String path) {
        this.uploadPath = path;
        return this;
    }
    public UploadHelper setUploadHref(String href) {
        this.uploadHref = href;
        return this;
    }
    public UploadHelper setUploadName(String name) {
        this.uploadName = name;
        return this;
    }
    public UploadHelper setAllowTypes(String... type) {
        this.allowTypes = new HashSet(Arrays.asList(type));
        return this;
    }
    public UploadHelper setAllowExtns(String... extn) {
        this.allowExtns = new HashSet(Arrays.asList(extn));
        return this;
    }

    private String getUploadPath(String path) {
        Map m = new HashMap();
        m.put("BASE_PATH", Core.BASE_PATH);
        m.put("CORE_PATH", Core.CORE_PATH);
        m.put("CONF_PATH", Core.CORE_PATH);
        m.put("DATA_PATH", Core.DATA_PATH);
        path  = Text.inject(path, m);
        if (! new File(path).isAbsolute()) {
            path = Core.BASE_PATH+"/"+path;
        }
        return path;
    }
    
    private String getUploadExtn(File file) {
        String name = file.getName(   );
        int pos = name.lastIndexOf('.');
        if (pos > 1) {
          return  name.substring(pos+1);
        }
        else {
          return  "";
        }
    }

    private String getUploadType(File file) {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        return  MimeUtil.getMimeTypes(file).toString();
    }
    
    private void setResultName(String fame, String extn) {
        String famc = fame+"."+extn;
        int l = Core.SERVER_ID.length();
        if (fame.length() >= l+8) {
            famc = fame.substring(0  , l  )
             +"/"+ fame.substring(l  , l+4)
             +"/"+ fame.substring(l+4, l+8)
             +"/"+ famc;
        }
        this.resultName = famc;
    }
    
    private void chkTypeOrExtn(String type, String extn) throws VerifyHelper.Wrong {
        /**
         * 检查文件类型
         */
        if (this.allowTypes != null
        && !this.allowTypes.contains(type)) {
            // 文件类型不对
            throw new VerifyHelper.Wrong("fore.form.unallowed.types", this.allowTypes.toString());
        }

        /**
         * 检查扩展名
         */
        if (this.allowExtns != null
        && !this.allowExtns.contains(extn)) {
            // 扩展名不对
            throw new VerifyHelper.Wrong("fore.form.unallowed.extns", this.allowExtns.toString());
        }
    }

    public String getResultPath() {
        String path = this.resultName;
        if (this.uploadPath != null) {
            path = this.uploadPath + "/" + path;
        }
        return path;
    }

    public String getResultHref() {
        String href = this.resultName;
        if (this.uploadHref != null) {
            href = this.uploadHref + "/" + href;
        }
        return href;
    }

    /**
     * 检查文件流并写入目标目录
     * @param xis
     * @param type
     * @param extn
     * @param fame 指定文件ID
     * @return
     * @throws app.hongs.action.VerifyHelper.Wrong
     */
    public File upload(InputStream xis, String type, String extn, String fame) throws VerifyHelper.Wrong {
        chkTypeOrExtn(type, extn);
        setResultName(fame, extn);

        try {
            String path = this.getUploadPath(this.getResultPath(  ) );
            File   file = new File(path);
            /**/FileOutputStream fos = new /**/FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos );
            BufferedInputStream  bis = new BufferedInputStream (xis );
            Streams.copy(bis, bos, true);
            return  file;
        } catch (IOException ex) {
            throw new VerifyHelper.Wrong(ex, "fore.form.upload.failed");
        }
    }

    /**
     * 检查文件流并写入目标目录
     * @param xis
     * @param type
     * @param extn
     * @return
     * @throws app.hongs.action.VerifyHelper.Wrong
     */
    public File upload(InputStream xis, String type, String extn) throws VerifyHelper.Wrong {
        return  upload(xis, type, extn, Core.getUniqueId());
    }

    /**
     * 检查已上传的文件并从临时目录移到目标目录
     * @param path
     * @param fame 指定文件ID
     * @return
     * @throws app.hongs.action.VerifyHelper.Wrong
     */
    public File upload(String path, String fame) throws VerifyHelper.Wrong {
        if (fame == null) {
            fame  = Core.getUniqueId( );
        }
        path = this.getUploadPath(path);
        File file = new File(path);
        String type , extn;
        FileInputStream fs;

        if (file.exists()) {
            extn = getUploadExtn( file );
            type = getUploadType( file );
        } else {
            file = new File(path+".txn");

            /**
             * 从上传信息中提取类型和扩展名
             */
            try {
                fs = new FileInputStream(file );
                try(InputStreamReader sr = new InputStreamReader(fs);
                    BufferedReader    fr = new BufferedReader   (sr))
                {
                    type = fr.readLine().trim();
                    extn = fr.readLine().trim();
                }   fs.close();
            } catch (FileNotFoundException ex ) {
                throw new VerifyHelper.Wrong(ex, "fore.form.upload.failed");
            } catch (IOException ex ) {
                throw new VerifyHelper.Wrong(ex, "fore.form.upload.failed");
            }

            file = new File(path+".tmp");
        }

        chkTypeOrExtn(type, extn);
        setResultName(fame, extn);

        String disp = this.getUploadPath (this.getResultPath ( ) );
        File   dist = new File(disp);
        // 原始文件与目标文件不一样才需要移动
        if(!dist.getAbsolutePath().equals(file.getAbsolutePath())) {
            file.renameTo(dist);
        }

        return dist;
    }

    /**
     * 检查已上传的文件并从临时目录移到目标目录
     * @param fame 指定文件ID
     * @return
     * @throws app.hongs.action.VerifyHelper.Wrong
     */
    public File upload(String fame) throws VerifyHelper.Wrong {
        return upload(Core.DATA_PATH + "/upload/" + fame, fame);
    }

    /**
     * 批量处理上传数据
     * @param request
     * @param uploads
     * @throws app.hongs.action.VerifyHelper.Wrong
     */
    public static void upload(Map<String, Object> request, UploadHelper... uploads) throws VerifyHelper.Wrong {
        for(UploadHelper upload : uploads) {
            String v = null;
            String n = upload.uploadName != null ? upload.uploadName : "file";
            v = Dict.getParam(request, v, n);
            if (v  !=  null) {
                upload.upload(v);
                v = upload.getResultHref(  );
                Dict.setParam(request, v, n);
            }
        }
    }

}
