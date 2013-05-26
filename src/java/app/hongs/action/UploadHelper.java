package app.hongs.action;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.util.Tree;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

/**
 * 上传助手
 * 需要依赖apache的commons-fileupload(1.2)
 * 参考API: http://commons.apache.org/fileupload/apidocs/index.html
 * 默认上传目录: TMPS_PATH + "/uploads";
 * @author Hongs
 */
public class UploadHelper
{

  private HttpServletRequest request;
  private Map<String, Object> requestData;
  private Map<String, List<File>> uploadFiles;
  private Map<String, List<String>> uploadNames;

  private String uploadUri;
  private String uploadDir;

  private long fileSizeMax;
  private long allSizeMax;
  private Set<String> allowTypes;
  private Set<String> allowExts;

  /**
   * 使用request对象构造
   * @param request
   */
  public UploadHelper(HttpServletRequest request)
  {
    this.request      = request;
    this.requestData  = null;
    this.uploadFiles  = null;
    this.uploadNames  = null;

    this.uploadDir    = Core.TMPS_PATH + "/uploads";
    this.uploadUri    = null;

    this.fileSizeMax  = 0;
    this.allSizeMax   = 0;
    this.allowTypes   = null;
    this.allowExts    = null;
  }

  /**
   * 使用动作助手对象构造
   * @param helper
   */
  public UploadHelper(ActionHelper helper)
  {
    this(helper.request);
  }

  /**
   * 设置上传目录
   * @param dir
   */
  public void setUploadDir(String dir)
  {
    this.uploadDir = dir;
  }

  /**
   * 设置上传URI
   * @param uri
   */
  public void setUploadUri(String uri)
  {
    this.uploadUri = uri;
  }

  /**
   * 设置单个文件的最大尺寸
   * 单位: 字节(Byte)
   * @param size
   */
  public void setFileSizeMax(long size)
  {
    this.fileSizeMax = size;
  }

  /**
   * 设置全部文件的最大尺寸
   * 单位: 字节(Byte)
   * @param size
   */
  public void setAllSizeMax(long size)
  {
    this.allSizeMax = size;
  }

  /**
   * 设置许可的文件类型(ContentType)
   * 分号分割, 如: text/plain;text/html
   * @param types
   */
  public void setAllowTypes(String types)
  {
    String[] arr = types.split(";");
    this.allowTypes = new LinkedHashSet();
    this.allowTypes.addAll(Arrays.asList(arr));
  }

  /**
   * 设置许可的文件类型(文件扩展名)
   * 分号分割, 如: html;htm;txt;gif;jpg
   * @param exts
   */
  public void setAllowExts(String exts)
  {
    String[] arr = exts.split(";");
    this.allowExts = new LinkedHashSet();
    this.allowExts.addAll(Arrays.asList(arr));
  }

  /**
   * 获取请求数据
   * @return 请求数据
   */
  public Map<String, Object> getRequestData() throws HongsException
  {
    if (this.requestData == null)
    {
      this.parseRequest();
    }
    return this.requestData;
  }

  /**
   * 获取上传列表
   * @return 上传列表
   */
  public Map<String, List<File>> getUploadFiles() throws HongsException
  {
    if (this.requestData == null)
    {
      this.parseRequest();
    }
    return this.uploadFiles;
  }

  /**
   * 获取多个文件
   * @param name
   * @return 多个文件
   */
  public List<File> getFiles(String name)
  {
    if (this.uploadFiles.containsKey(name))
    {
      return this.uploadFiles.get(name);
    }
    else
    {
      return null;
    }
  }

  /**
   * 获取单个文件
   * @param name
   * @return 单个文件
   */
  public File getFile(String name)
  {
    if (this.uploadFiles.containsKey(name))
    {
      return this.uploadFiles.get(name).get(0);
    }
    else
    {
      return null;
    }
  }

  /**
   * 获取上传列表
   * @return 上传列表
   */
  public Map<String, List<File>> getUploadNames() throws HongsException
  {
    if (this.requestData == null)
    {
      this.parseRequest();
    }
    return this.uploadFiles;
  }

  /**
   * 获取多个名称
   * @param name
   * @return 多个名称
   */
  public List<String> getNames(String name)
  {
    if (this.uploadNames.containsKey(name))
    {
      return this.uploadNames.get(name);
    }
    else
    {
      return null;
    }
  }

  /**
   * 获取单个名称
   * @param name
   * @return 单个名称
   */
  public String getName(String name)
  {
    if (this.uploadNames.containsKey(name))
    {
      return this.uploadNames.get(name).get(0);
    }
    else
    {
      return null;
    }
  }

  /** 工具函数 **/

  private void parseRequest()
  throws HongsException
  {
    this.requestData = new HashMap();
    this.uploadFiles = new HashMap();
    this.uploadNames = new HashMap();

    if (!ServletFileUpload.isMultipartContent(request)) {
      this.requestData = ActionHelper.parseParams(
                         request.getParameterMap());
      return;
    }

    /**
     * 上传目录不存在则自动创建
     */
    File dir = new File(this.uploadDir);
    if (!dir.isDirectory()) {
      dir.mkdirs();
    }

    try {
      ServletFileUpload upload = new ServletFileUpload();
      if (this.fileSizeMax > 0) {
        upload.setFileSizeMax(this.fileSizeMax);
      }
      if (this.allSizeMax > 0) {
        upload.setSizeMax(this.allSizeMax);
      }

      FileItemIterator iter = upload.getItemIterator(request);
      while (iter.hasNext()) {
        FileItemStream item = iter.next();
        InputStream stream = item.openStream();
        String fame = item.getFieldName();
        String name = item.getName();

        if (item.isFormField()) {
          /**
           * 是普通字段则直接放入请求数据中
           */
          String value = Streams.asString(stream);
          Tree.setArrayValue(requestData, fame, value);
        }
        else {
          /**
           * 没有上传文件则跳过检查
           */
          if (name == null
          ||  name.length() == 0) {
            continue;
          }

          /**
           * 检查文件类型
           */
          String type = item.getContentType();
          if (this.allowTypes != null
          && !this.allowTypes.contains(type)) {
            // 回滚
            this.clearUploads();
            // 文件类型不对
            String ts = this.allowTypes.toString();
            HongsException ex2 = new HongsException(0x10f6,
              "The type of file '"+name+"' is '"+type+"', but allow types: "+ts);
            ex2.setTranslate(name, type, ts);
            throw ex2;
          }

          /**
           * 检查扩展名
           */
          String ext = this.getTheExt(name);
          if (this.allowExts != null
          && !this.allowExts.contains(ext)) {
            // 回滚
            this.clearUploads();
            // 扩展名不对
            String es = this.allowExts.toString();
            HongsException ex2 = new HongsException(0x10f8,
              "The ext of file '"+name+"' is '"+ext+"', but allow exts: "+es);
            ex2.setTranslate(name, ext, es);
            throw ex2;
          }

          /**
           * 将路径放入数据中
           */
          String fname = Core.getUniqueId() +"."+ ext;
          String fpath = this.uploadDir + "/" + fname;
          String value = fname;
          if (this.uploadUri != null) {
                 value = this.uploadUri + "/" + fname;
          }
          Tree.setArrayValue(requestData, fame, value);

          /**
           * 将名称放入列表中
           */
          List<String> names;
          if (!uploadNames.containsKey(fame)) {
            names = new ArrayList();
            uploadNames.put(fame,  names);
          }
          else {
            names = uploadNames.get(fame);
          }
          name = item.getName();
          names.add(name);

          /**
           * 将文件放入列表中
           */
          File file;
          List<File> files;
          if (!uploadFiles.containsKey(fame)) {
            files = new ArrayList();
            uploadFiles.put(fame,  files);
          }
          else {
            files = uploadFiles.get(fame);
          }
          file = new File(fpath);
          files.add(file);

          /**
           * 存入上传目录
           */
          BufferedOutputStream bos = new BufferedOutputStream(
                                     new FileOutputStream(file));
          BufferedInputStream bis = new BufferedInputStream(stream);
          Streams.copy(bis, bos, true);
        }
      }
    }
    catch (FileUploadException ex) {
      // 回滚
      this.clearUploads();
      if (ex instanceof FileUploadBase.FileSizeLimitExceededException) {
        // 单个文件超限
        FileUploadBase.FileSizeLimitExceededException ex1 =
        (FileUploadBase.FileSizeLimitExceededException) ex;
        HongsException ex2 = new HongsException(0x10f4, ex);
        ex2.setTranslate(ex1.getFileName(),
          String.valueOf(ex1.getActualSize()),
          String.valueOf(ex1.getPermittedSize()));
        throw ex2;
      }
      else
      if (ex instanceof FileUploadBase.SizeLimitExceededException) {
        // 全部上传超限
        FileUploadBase.SizeLimitExceededException ex1 =
        (FileUploadBase.SizeLimitExceededException) ex;
        HongsException ex2 = new HongsException(0x10f2, ex);
        ex2.setTranslate(
          String.valueOf(ex1.getActualSize()),
          String.valueOf(ex1.getPermittedSize()));
        throw ex2;
      }
      else {
        throw new HongsException(0x10f0, ex);
      }
    }
    catch (IOException ex) {
      // 回滚
      this.clearUploads();
      // IO异常
      throw new HongsException(0x10f0, ex);
    }
  }

  /**
   * 清除上传数据
   */
  public void clearUploads() {
    for (Map.Entry et : this.uploadFiles.entrySet()) {
      List<File> files = (List<File>)et.getValue();
      for (File  file  :  files) {
        if (file.exists()) {
            file.delete();
        }
      }
    }
    this.uploadFiles.clear();
    this.uploadNames.clear();
  }

  /**
   * 获取扩展名
   * @param name 文件名或文件路径
   * @return     扩展名
   */
  public String getTheExt(String name) {
    // 如果有指定扩展名就依次去匹配
    if (this.allowExts != null) {
      for (String ext : allowExts) {
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

}
