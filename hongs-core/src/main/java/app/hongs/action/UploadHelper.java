package app.hongs.action;

import app.hongs.Core;
import app.hongs.util.Dict;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.fileupload.util.Streams;

/**
 * 上传助手
 * @author Hongs
 */
public class UploadHelper {
    private String uploadPath = "upload";
    private String uploadHref = "upload";
    private String uploadDate = "yyyy/MM/";
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
    public UploadHelper setUploadDate(String date) {
        this.uploadDate = date;
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

    private String getUploadExtn(String name) {
        int pos = name.lastIndexOf('.');
        if (pos > 1) {
          return  name.substring(pos+1);
        }
        else {
          return  "";
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
     * @param stream
     * @param type
     * @param extn
     * @param fame 指定文件名
     * @return
     * @throws app.hongs.action.VerifyHelper.Wrong 
     */
    public File upload(InputStream stream, String type, String extn, String fame) throws VerifyHelper.Wrong {
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

        /**
         * 将路径放入数据中
         */
        String famc = fame+"."+extn;
        if (this.uploadDate != null) {
            famc = new SimpleDateFormat(this.uploadDate).format(new Date()) + famc;
        }
        this.resultName = famc;
        String path = this.getResultPath();
        if (! new File(path).isAbsolute()) {
            path = Core.BASE_PATH + "/../" + path;
        }

        try {
            File   file = new File(path);
            /**/FileOutputStream fos = new /**/FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos );
            BufferedInputStream  bis = new BufferedInputStream (stream);
            Streams.copy(bis, bos, true);
            return file;
        } catch (IOException ex) {
            throw new VerifyHelper.Wrong(ex, "fore.form.upload.failed");
        }
    }

    /**
     * 检查文件流并写入目标目录
     * @param stream
     * @param type
     * @param extn
     * @return
     * @throws app.hongs.action.VerifyHelper.Wrong 
     */
    public File upload(InputStream stream, String type, String extn) throws VerifyHelper.Wrong {
        return upload(stream, type, extn, Core.getUniqueId());
    }

    /**
     * 检查已上传的文件并从临时目录移到目标目录
     * @param fame
     * @return
     * @throws app.hongs.action.VerifyHelper.Wrong 
     */
    public File upload(String fame) throws VerifyHelper.Wrong {
        File file = new File(Core.VARS_PATH + "/upload/" + fame + ".tmp");
        File info = new File(Core.VARS_PATH + "/upload/" + fame + ".txt");

        FileInputStream fs = null;
        try {
            String type;
            String extn;
            fs = new FileInputStream(info);
            try(InputStreamReader sr = new InputStreamReader(fs);
                BufferedReader fr = new BufferedReader(sr)) {
                type = fr.readLine().trim();
                extn = fr.readLine().trim();
                extn = getUploadExtn(extn);
            }
            fs.close();

            fs = new FileInputStream(file);
            return upload(fs , type, extn);
        } catch (FileNotFoundException ex) {
            throw new VerifyHelper.Wrong(ex, "fore.form.upload.failed");
        } catch (IOException ex) {
            throw new VerifyHelper.Wrong(ex, "fore.form.upload.failed");
        } finally {
            try {
                if (fs != null) {
                    fs.close( );
                }
            } catch (IOException ex) {
                throw new VerifyHelper.Wrong(ex, "fore.form.upload.failed");
            }
        }
    }

    /**
     * 批量处理上传数据
     * @param requestData
     * @param uploads
     * @throws app.hongs.action.VerifyHelper.Wrong 
     */
    public static void upload(Map<String, Object> requestData, UploadHelper... uploads) throws VerifyHelper.Wrong {
        for(UploadHelper upload : uploads) {
            String v = null;
            String n = upload.uploadName;
            v = Dict.getParam(requestData, v, n);
            if (v != null) {
                upload.upload(v);
                v = upload.getResultHref();
                Dict.setParam(requestData, v, n);
            }
        }
    }

}
