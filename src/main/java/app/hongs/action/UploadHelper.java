package app.hongs.action;

import app.hongs.Core;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
        this.allowTypes = new LinkedHashSet(Arrays.asList(type));
        return this;
    }
    public UploadHelper setAllowExtns(String... extn) {
        this.allowExtns = new LinkedHashSet(Arrays.asList(extn));
        return this;
    }

    private String getUploadExtn(String name) {
        // 如果有指定扩展名就依次去匹配
        if (this.allowExtns != null) {
          for (String ext : allowExtns) {
            if (name.endsWith("." + ext)) {
              return  ext;
            }
          }
        }

        // 否则取最后的句点后作为扩展名
        name = new File(name).getName();
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

    public File upload(InputStream stream, String fame, String name, String type) throws VerifyHelper.Wrong {
        /**
         * 检查文件类型
         */
        if (this.allowTypes != null
        && !this.allowTypes.contains(type)) {
            // 文件类型不对
            throw new VerifyHelper.Wrong("fore.form.unallowed.types", this.allowTypes.toString());
        }

        String extn = this.getUploadExtn(name);

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
        String famc = Core.getUniqueId() + "." + extn;
        if (this.uploadDate != null) {
            famc = new SimpleDateFormat(this.uploadDate).format(new Date()) + famc;
        }
        this.resultName = famc;
        String path = this.getResultPath();
        if (! new File(path).isAbsolute()) {
            path = Core.BASE_PATH + "/../" + path;
        }

        try {
            File file = new File(path);
            BufferedOutputStream bos = new BufferedOutputStream(
                                       new     FileOutputStream(file));
            BufferedInputStream  bis = new BufferedInputStream(stream);
            Streams.copy(bis,bos,true);
            return file;
        } catch (IOException ex) {
            throw new VerifyHelper.Wrong(ex, "fore.form.upload.failed");
        }
    }

    public static Map<String, File> upload(HttpServletRequest request, UploadHelper... uploads) throws VerifyHelper.Wrong {
        Map<String,       File  > uploadm = new LinkedHashMap();
        Map<String, UploadHelper> uploadz = new LinkedHashMap();
        for (UploadHelper upload: uploads) {
            uploadz.put(upload.uploadName, upload);
        }

        try {
            FileItemIterator fit = new ServletFileUpload().getItemIterator(request);
            while (fit.hasNext()) {
                FileItemStream fis = fit.next( );
                if (fis.isFormField()) {
                    continue;
                }
                String fame = fis.getFieldName();
                UploadHelper upload = uploadz.get(fame);
                if ( upload == null  ) {
                    continue;
                }
                String name = fis.getName();
                String type = fis.getContentType();
                InputStream ins = fis.openStream();
                uploadm.put(fame, upload.upload(ins, fame, name, type));
            }
            return uploadm;
        } catch (FileUploadException ex) {
            throw new VerifyHelper.Wrong(ex, "fore.form.upload.failed");
        } catch (IOException ex) {
            throw new VerifyHelper.Wrong(ex, "fore.form.upload.failed");
        }
    }

}
