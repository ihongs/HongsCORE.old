package app.hongs.serv;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.cmdlet.CmdletHelper;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.db.DB;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统设置命令
 * @author Hongs
 */
@Cmdlet("system")
public class SystemCmdlet {

  @Cmdlet("setup")
  public static void setup(String[] args) throws HongsException {
    String dir = "etc/_setup_";
    if ( 0  < args.length ) {
        dir = args[0];
    }

    if (! new File(dir).isAbsolute()) {
        dir = Core.BASE_PATH+"/"+dir;
    }

    List<File> fxs = new ArrayList();
    File[/**/] fos = new File( dir ).listFiles();
    for (File fo : fos) {
        if (fo.isDirectory() || fo.isHidden()
        || !fo.getName( ).endsWith( ".sql" )) {
            continue;
        }
        fxs.add(fo );
    }

    Pattern pat = Pattern.compile("^--\\s*DB:\\s*([^\\s*]+)"); // 在第一行指定适用的数据库配置
    Collections.sort(fxs, new FileComparator());
    for (File fo : fxs) {
        try {
            FileInputStream in=new FileInputStream(fo);
            int    size = in.available();
            byte[] buff = new byte[size];
            in.read (buff);
            in.close(/**/);

            String  sql = new String (buff,"UTF-8");
            Matcher mat = pat.matcher(sql);
            String  dbn = "default";
            if (mat.find()) {
                dbn = mat.group(1).trim( );
            }
            DB  db  =  DB.getInstance(dbn);

            CmdletHelper.println("Run '"+fo.getName()+"' for '"+dbn+"'...");

            // 清理注释并分割成单条的 SQL 语句
            sql = sql.replaceAll("(^|[\r\n])\\s*(--|/\\*\\!).*", "");
            String[] a = sql.split(";\\s*[\r\n]");

            long st = System.currentTimeMillis( );
            int al = a.length;
            int ok = 0;
            for(String s : a) {
                db.execute(s.trim());
                CmdletHelper.printELeft(st, al, ++ ok);
            }
        } catch (FileNotFoundException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }
  }

  private static class FileComparator implements Comparator<File> {
    @Override
    public int compare(File f1 , File f2) {
        return f1.getName().compareTo(f2.getName());
    }
  }

}
