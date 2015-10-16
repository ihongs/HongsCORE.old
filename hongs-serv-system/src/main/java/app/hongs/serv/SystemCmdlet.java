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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 管理命令
 * @author Hongs
 */
@Cmdlet("system")
public class SystemCmdlet {

    /**
     * 设置数据库
     * @param args
     * @throws HongsException
     */
    @Cmdlet("setup")
    public static void setup(String[] args) throws HongsException {
        if (args.length == 0) {
            args = new String[] { Core.CORE_PATH + "/bin/_setup_" };
        }

        List<File> fxs = new ArrayList();
        File[/**/] fos = new File(args[0]).listFiles();
        for (File  fo  : fos) {
            if (fo.isDirectory() || fo.isHidden()
            || !fo.getName( ).endsWith( ".sql" )) {
                continue;
            }
            fxs.add (fo);
        }
        Collections.sort(fxs, new FileComparator());

        Pattern pet = Pattern.compile("^(.*?\\.)?(.*?)\\.sql$");    // 文件名为 01.xxxx.sql 这样的
        Pattern pat = Pattern.compile("--\\s*DB:\\s*(\\S+)");       // 指定数据库
//      Pattern pot = Pattern.compile("--\\s*TO:\\s*(\\S+)");       // 指定输出区
        Pattern pct = Pattern.compile("--.*?[\r\n]");               // 注释
        Pattern pxt = Pattern.compile("\\{\\{(.+?)\\}\\}");         // 日期
        Date    dxt = new Date();

        for (File fo : fxs) {
            Matcher met = pet.matcher(fo.getName());
            if (!met.matches()) {
                continue;
            }

            try {
                byte[] buff;
                try (FileInputStream in = new FileInputStream(fo)) {
                    buff = new byte[ in.available( ) ];
                    in.read(buff);
                }

                String  sql = new String(buff,"UTF-8");
                String  dbn ;
                Matcher mat ;
                mat = pat.matcher(sql);
                if (mat.find()) {
                    dbn = mat.group(1).trim();
                } else {
                    dbn = met.group(2).trim();
                }
                DB  db  = DB.getInstance(dbn);

                CmdletHelper.println("Run '"+fo.getName()+"' for '"+dbn+"'...");

                // 清理注释
                sql = pct.matcher(sql).replaceAll("");

                // 设置时间
                Matcher      mxt = pxt.matcher( sql );
                StringBuffer mxb = new StringBuffer();
                while (mxt.find()) {
                    mxt.appendReplacement(mxb, Matcher.quoteReplacement(
                        new SimpleDateFormat(mxt.group(1)).format(dxt)));
                }
                mxt.appendTail(mxb);
                sql= mxb.toString();

                /*
                StringBuilder e = new StringBuilder();
                */
                String[] a = sql.split(";\\s*[\r\n]");
                long st = System.currentTimeMillis( );
                int  al = a.length;
                int  ok = 0;
                int  er = 0;
                for(String s : a) {
                    s = s.trim( );
                    try {
                        if (0 < s.length()) {
                            db.execute(s );
                        }
                        CmdletHelper.progres(st, al, ++ok, er);
                    } catch (HongsException ex) {
                        CmdletHelper.progres(st, al, ok, ++er);
                        CmdletHelper.progred( );
                        throw ex;
                        /*
                        e.append("\r\n").append(ex.getMessage());
                        */
                    }
                }
                /*
                if (e.length() > 0) {
                    CmdletHelper.println("Excute error:" + e);
                }
                */
            } catch (FileNotFoundException ex) {
                throw new HongsException.Common(ex);
            } catch (IOException ex) {
                throw new HongsException.Common(ex);
            }
        }
    }

    /**
     * 清理数据库
     * @param args
     */
    @Cmdlet("clean")
    public static void clean(String[] args) throws HongsException {
        if (args.length == 0) {
            args = new String[] { Core.CORE_PATH + "/bin/_clean_" };
        }
        setup(args);
    }

    /**
     * 从 db 构建 form 和 menu
     * @param args
     */
    public static void build(String[] args) {
    }

    private static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File f1 , File f2) {
            return f1.getName().compareTo(f2.getName());
        }
    }

}
