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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 管理命令
 * @author Hongs
 */
@Cmdlet("system")
public class SystemCmdlet {

    /**
     * 设置/更新数据库
     * @param args
     * @throws HongsException 
     */
    @Cmdlet("setup")
    public static void setup(String[] args) throws HongsException {
        String dir;
        if ( 0  < args.length ) {
            dir = args[0];
        } else {
            dir = Core.VARS_PATH + "/setup";
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

        Pattern pan = Pattern.compile("^(\\d+\\.)?(.*?)\\.sql$" ); // 文件名为 01.xxxx.sql 这样的
        Pattern pat = Pattern.compile("^--\\s*DB:\\s*([^\\s*]+)"); // 在第一行指定适用的数据库配置
        Collections.sort(fxs, new FileComparator());
        for ( File  fo  : fxs ) {
            Matcher man = pan.matcher(fo.getName());
            if (!man.matches()) {
                continue;
            }

            try {
                byte[] buff;
                try (FileInputStream in = new FileInputStream(fo)) {
                    buff = new byte[ in.available( ) ];
                    in.read(buff);
                }

                String  sql = new String(buff,"UTF-8");
                String  dbn;
                Matcher mat = pat.matcher(sql);
                if (mat.find()) {
                    dbn = mat.group(1).trim( );
                } else {
                    dbn = man.group(2).trim( );
                }
                DB  db  =  DB.getInstance(dbn);

                CmdletHelper.println("Run '"+fo.getName()+"' for '"+dbn+"'...");

                // 清理注释并分割成单条的 SQL 语句
                sql = sql.replaceAll("(^|[\r\n])\\s*(--|/\\*\\!).*", "");
                String[] a = sql.split(";\\s*[\r\n]");

                StringBuilder e = new StringBuilder();
                long st = System.currentTimeMillis( );
                int  al = a.length;
                int  ok = 0;
                int  er = 0;
                for(String s : a) {
                    s = s.trim();
                    if (s.length() == 0) {
                        CmdletHelper.printELeft(st, al, ++ok, er);
                        continue;
                    }
                    try {
                        db.execute(s.trim());
                        CmdletHelper.printELeft(st, al, ++ok, er);
                    } catch (HongsException ex) {
                        CmdletHelper.printELeft(st, al, ok, ++er);
                        e.append("\r\n").append(ex.getMessage( ));
                    }
                }
                if (e.length() > 0) {
                    CmdletHelper.println("Excute error:" + e);
                }
            } catch (FileNotFoundException ex) {
                throw HongsException.common(null, ex);
            } catch (IOException ex) {
                throw HongsException.common(null, ex);
            }
        }
    }

    /**
     * 从 form 配置创建 db 配置
     * @param args 
     */
    public static void build(String[] args) {
        Map<String, Object> opts = CmdletHelper.getOpts(args,
            "conf:s", "form:s", "help:b");
    }
    
    private static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File f1 , File f2) {
            return f1.getName().compareTo(f2.getName());
        }
    }

}
