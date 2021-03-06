package app.hongs.action;

import app.hongs.Core;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import app.hongs.util.Tool;
import app.hongs.veri.Wrong;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.fileupload.util.Streams;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    static {
        MimeUtil.registerMimeDetector(MagicMimeMimeDetector.class.getName());
    }

    public UploadHelper setUploadName(String name) {
        this.uploadName = name;
        return this;
    }
    public UploadHelper setUploadPath(String path) {
        this.uploadPath = path;
        return this;
    }
    public UploadHelper setUploadHref(String href) {
        this.uploadHref = href;
        return this;
    }
    public UploadHelper setUploadLink(String href) {
        ActionHelper helper = Core.getInstance(ActionHelper.class);
        String hp = helper.getRequest().getProtocol(  );
        String hn = helper.getRequest().getServerName();
        int    pt = helper.getRequest().getServerPort();
        String hu = hp +"://"+ hn;
        if (pt != 80 && pt != 443) {
            hu += ":" + pt ;
        }
            hu += Core.BASE_HREF ;
        if (!href.startsWith("/")) {
            hu += "/" ;
        }
        return setUploadHref(hu + href);
    }
    public UploadHelper setAllowTypes(String... type) {
        this.allowTypes = new HashSet(Arrays.asList(type));
        return this;
    }
    public UploadHelper setAllowExtns(String... extn) {
        this.allowExtns = new HashSet(Arrays.asList(extn));
        return this;
    }

    private void chkTypeOrExtn(String type, String extn) throws Wrong {
        /**
         * 检查文件类型
         */
        if (this.allowTypes != null
        && !this.allowTypes.contains(type)) {
            // 文件类型不对
            throw new Wrong("fore.form.unallowed.types", this.allowTypes.toString());
        }

        /**
         * 检查扩展名
         */
        if (this.allowExtns != null
        && !this.allowExtns.contains(extn)) {
            // 扩展名不对
            throw new Wrong("fore.form.unallowed.extns", this.allowExtns.toString());
        }
    }

    private void setResultName(String fame, String extn) {
        String famc = upname(fame);
        if (extn != null && !extn.equals("")) {
            famc +=  "."  +  extn ;
        }
        this.resultName   =  famc ;
    }

    private String getResultPath(String path) {
        Map m = new HashMap();
        m.put("BASE_PATH", Core.BASE_PATH);
        m.put("CORE_PATH", Core.CORE_PATH);
        m.put("CONF_PATH", Core.CONF_PATH);
        m.put("DATA_PATH", Core.DATA_PATH);
        path = Tool.inject(path, m);
        if (! new File(path).isAbsolute()) {
            path = Core.BASE_PATH+"/"+path;
        }
        return path;
    }

    public String getResultPath() {
        String path = this.resultName;
        if (this.uploadPath != null) {
            path = this.uploadPath + "/" + path;
        }
        return getResultPath( path );
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
     * @throws Wrong
     */
    public File upload(InputStream xis, String type, String extn, String fame) throws Wrong {
        if (extn.contains( "." )) {
            extn = MimeUtil.getExtension(extn);
        }
        chkTypeOrExtn(type, extn);
        setResultName(fame, extn);

        try {
            String path = this.getResultPath();
            File   file = new File(path);
            /**/FileOutputStream fos = new /**/FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos );
            BufferedInputStream  bis = new BufferedInputStream (xis );
            Streams.copy(bis, bos, true);
            return  file;
        } catch (IOException ex) {
            throw new Wrong(ex, "fore.form.upload.failed");
        }
    }

    /**
     * 检查文件流并写入目标目录
     * @param xis
     * @param type
     * @param extn
     * @return
     * @throws Wrong
     */
    public File upload(InputStream xis, String type, String extn) throws Wrong {
        return  upload(xis, type, extn, Core.getUniqueId());
    }

    /**
     * 检查已上传的文件并从临时目录移到目标目录
     * @param path
     * @param fame 指定文件ID
     * @return
     * @throws Wrong
     */
    public File upload(String path, String fame) throws Wrong {
        if (fame == null) {
            fame  = Core.getUniqueId( );
        }
        path = this.getResultPath(path);
        File file = new File(path);
        File temp = null;
        String type;
        String extn;

        if (file.exists()) {
            extn = MimeUtil.getExtension(file);
            type = MimeUtil.getMimeTypes(file).toString();
        } else {
            temp = new File(path+".tnp");

            /**
             * 从上传信息中提取类型和扩展名
             */
            try {
                FileInputStream fs;
                fs = new FileInputStream(temp );
                try(InputStreamReader sr = new InputStreamReader(fs);
                    BufferedReader    fr = new BufferedReader   (sr))
                {
                    extn = fr.readLine().trim();
                    type = fr.readLine().trim();
                    int p  = extn.lastIndexOf('.');
                    if (p  > 1) {
                        extn = extn.substring(p+1);
                    } else {
                        extn = "";
                    }
                }   fs.close();
            } catch (FileNotFoundException ex ) {
                throw new Wrong(ex, "fore.form.upload.failed");
            } catch (IOException ex ) {
                throw new Wrong(ex, "fore.form.upload.failed");
            }

            file = new File(path+".tmp");
        }

        chkTypeOrExtn(type, extn);
        setResultName(fame, extn);

        String disp = this.getResultPath();
        File   dist = new File(disp);
        // 原始文件与目标文件不一样才需要移动
        if(!dist.getAbsolutePath().equals(file.getAbsolutePath())) {
            File dirt  =  dist.getParentFile();
            if (!dirt.isDirectory()) {
                 dirt.mkdirs( );
            }
            file.renameTo(dist);
            if ( temp !=  null) {
                 temp.delete( );
            }
        }

        return dist;
    }

    /**
     * 检查已上传的文件并从临时目录移到目标目录
     * @param fame 指定文件ID
     * @return
     * @throws Wrong
     */
    public File upload(String fame) throws Wrong {
        if (fame == null || fame.length( ) == 0) {
            setResultName("", null);
            return  null;
        }

        /*
         * 如果直接给的路径
         * 则从路径中取名称
         * 如果路径没有改变
         * 则不变更
         * 否则拷贝
         */
        int i  = fame.lastIndexOf('/'  );
        if (i != -1) {
            String name, extn = "";
            int j  = fame.indexOf('.',i);
            if (j == -1) {
                name = fame.substring(i + 1);
            } else {
                extn = fame.substring(j + 1);
                name = fame.substring(i + 1 , j);
            }
            setResultName(name, extn);
            String href = getResultHref();
            String path = UploadHelper.this.getResultPath();
            if  (  fame.equals (href)) {
                return new File(path); // 不变
            }
            return upload(fame, null); // 拷贝
        }

        return upload(Core.DATA_PATH + "/upload/" + fame, fame);
    }

    /**
     * 上传ID扩展为上传路径名
     * 因文件系统对目录下的文件数量有限制
     * 故按照文件ID组成规则拆解成多级目录
     * 以使单个目录内的文件不会超出额定值
     * @param fame
     * @return
     */
    public static String upname(String fame) {
        if (fame.length() >= 8) {
            fame = fame.substring(0, 4) // 前4位36进制时间戳
           +"/"+ fame.substring(4, 8) // 后4位36进制时间戳
           +"/"+ fame;
        }
        return   fame;
    }

    /**
     * 批量处理上传数据
     * @param request
     * @param uploads
     * @throws Wrong
     */
    public static void upload(Map<String, Object> request, UploadHelper... uploads) throws Wrong {
        for(UploadHelper upload : uploads) {
            String n =   upload.uploadName != null? upload.uploadName: "file";
            Object v = Dict.getParam(request, null, n);
            String u ;
            File   f ;

            //** 单个文件 **/

            if(!(v instanceof Collection)
            && !(v instanceof Map   )   ) {
                 u = Synt.declare (v, "");
                 f = upload.upload(u);
                if (f != null) {
                    u  = upload.getResultHref(   );
                    Dict.setParam(request, u , n );
                } else {
                    Dict.setParam(request, "", n );
                }
                continue;
            }

            //** 多个文件 **/

            List s = new ArrayList( );
            List a = Synt.declare (v, List.class );
            n = n.replaceFirst("(\\.|\\[\\])$","");
            for (Object x : a) {
                 u = Synt.declare (x, "");
                 f = upload.upload(u);
                if (f != null) {
                    s.add(upload.getResultHref( ));
                }
            }
            Dict.setParam(request, s, n );
        }
    }

}
