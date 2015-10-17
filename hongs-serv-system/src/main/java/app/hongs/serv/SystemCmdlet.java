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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

/**
 * 管理命令
 * @author Hongs
 */
@Cmdlet("system")
public class SystemCmdlet {

    /**
     * 从 db 构建 form 和 menu
     * @param args
     */
    public static void build(String[] args) {
    }

    /**
     * 设置/维护数据库
     * @param args
     * @throws HongsException
     */
    @Cmdlet("setup")
    public static void setup(String[] args) throws HongsException {
        if (args.length == 0) {
            args = new String[] { Core.CORE_PATH + "/bin/_setup_" };
        }

        Pattern pet = Pattern.compile( "^(.+?\\.)?(.*?)\\.sql$" );  // 文件名为 xx.xxxx.sql 或 xxxx.sql
        Pattern pat = Pattern.compile("--\\s*(DB|DT):\\s*(\\S+)");  // 配置
        Pattern pct = Pattern.compile("--.*?[\r\n]");               // 注释
        Pattern pxt = Pattern.compile("\\{\\{(.+?)(\\|(.+?))?\\}\\}"); // 日期
        Pattern pzt = Pattern.compile("([\\-\\+])(\\d+Y)?(\\d+M)?(\\d+w)?(\\d+d)?(\\d+h)?(\\d+m)?(\\d+s)?$"); // 偏移
        Date    dxt = new Date();
        Date    dzt = dxt ;
        Date    dst ;

        // 获取目录下全部待执行的 sql 文件
        List<File> fxs = new ArrayList();
        File[] fos = new File(args[0]).listFiles();
        for (File  fo  : fos) {
            if (fo.isDirectory() || fo.isHidden( )
            || !fo.getName( ).endsWith( ".sql" ) ) {
                continue;
            }
            fxs.add (fo);
        }
        Collections.sort(fxs, new FileComparator());

        for (File fo : fxs) {
            Matcher met = pet.matcher( fo.getName( ) );
            if (  ! met.matches() ) {
                continue;
            }

            try {
                // 读取代码
                byte[] buf;
                try(FileInputStream in = new FileInputStream(fo)) {
                    buf = new byte[ in.available( ) ];
                    in.read(buf);
                }
                String  sql = new String(buf,"UTF-8");

                // 解析配置
                String  dbn = met.group(2).trim();
                Matcher mat = pat.matcher ( sql );
                while ( mat.find() ) {
                    String key = mat.group(1);
                    if ("DB".equals(key)) {
                        dbn = mat.group(2).trim();
                    } else
                    if ("DT".equals(key)) {
                        dzt = count(dxt, mat.group(2).trim(), pzt);
                    }
                }

                // 清理注释
                sql = pct.matcher(sql).replaceAll("");

                // 设置时间
                StringBuffer sqb = new StringBuffer();
                Matcher      mxt = pxt.matcher( sql );
                String       mxp ;
                while (mxt.find()) {
                    // 时间偏移
                    mxp = mxt.group(3);
                    if (mxp == null) {
                        dst =  dzt ;
                    } else {
                        dst = count(dxt, mxp, pzt);
                    }

                    // 时间格式
                    mxp = mxt.group(1);
                    if ("%S".equals(mxp)) {
                        mxp = String.valueOf(dst.getTime());
                    } else
                    if ("%s".equals(mxp)) {
                        mxp = String.valueOf(dst.getTime( ) / 1000);
                    } else
                    {
                        mxp = new SimpleDateFormat(mxp).format(dst);
                    }

                    mxt.appendReplacement(sqb, Matcher.quoteReplacement(mxp));
                }
                mxt.appendTail(sqb);
                sql= sqb.toString();

                CmdletHelper.println("Run '"+fo.getName()+"' for '"+dbn+"'.");

                // 逐条执行
                /*
                StringBuilder e = new StringBuilder();
                */
                String[] a = sql.split(";\\s*[\r\n]");
                DB   db = DB.getInstance(dbn);
                long st = System.currentTimeMillis( );
                int  al = a.length;
                int  ok = 0;
                int  er = 0;
                for(String s : a) {
                    s = s.trim( );
                    try {
                        if (0 < s.length()) {
                            db.execute( s );
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

    private static Date count(Date dxt, String mxp, Pattern pzt) {
        Matcher mzt = pzt.matcher( mxp );
        String  mzp ;

        if (mzt.find()) {
            Calendar cal = Calendar.getInstance();
            cal.setTime( dxt );

            // 加减符号
            mzp = mzt.group(1);
            boolean add = !"-".equals(mzp);
            int     num ;

            mzp = mzt.group(2);
            if (null != mzp) {
                mzp = mzp.substring(0  , mzp.length( ) - 1);
                num = Integer.valueOf(mzp);
                cal.add(Calendar.YEAR  , add? num: 0 - num);
            }

            mzp = mzt.group(3);
            if (null != mzp) {
                mzp = mzp.substring(0  , mzp.length( ) - 1);
                num = Integer.valueOf(mzp);
                cal.add(Calendar.MONTH , add? num: 0 - num);
            }

            mzp = mzt.group(4);
            if (null != mzp) {
                mzp = mzp.substring(0  , mzp.length( ) - 1);
                num = Integer.valueOf(mzp);
                cal.add(Calendar.WEEK_OF_MONTH, add? num: 0 - num);
            }

            mzp = mzt.group(5);
            if (null != mzp) {
                mzp = mzp.substring(0  , mzp.length( ) - 1);
                num = Integer.valueOf(mzp);
                cal.add(Calendar.DATE  , add? num: 0 - num);
            }

            mzp = mzt.group(6);
            if (null != mzp) {
                mzp = mzp.substring(0  , mzp.length( ) - 1);
                num = Integer.valueOf(mzp);
                cal.add(Calendar.HOUR  , add? num: 0 - num);
            }

            mzp = mzt.group(7);
            if (null != mzp) {
                mzp = mzp.substring(0  , mzp.length( ) - 1);
                num = Integer.valueOf(mzp);
                cal.add(Calendar.MINUTE, add? num: 0 - num);
            }

            mzp = mzt.group(8);
            if (null != mzp) {
                mzp = mzp.substring(0  , mzp.length( ) - 1);
                num = Integer.valueOf(mzp);
                cal.add(Calendar.SECOND, add? num: 0 - num);
            }

            return cal.getTime();
        }

        return dxt;
    }

    private static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File f1 , File f2) {
            return f1.getName().compareTo(f2.getName());
        }
    }

}
