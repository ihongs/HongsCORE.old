package app.hongs.serv.member;

import app.hongs.Core;
import app.hongs.CoreSerial;
import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.Table;
import app.hongs.util.Synt;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户组记录
 * @author Hongs
 */
public class RoleSet extends CoreSerial {

    public String       userId;
    public Set<String>  roles;
    public int          rtime;
    
    public RoleSet(String userId) throws HongsException {
        this.userId = userId;
        
        String  n;
        File    f;
        int     i;
        int     j;
        
        n = Core.VARS_PATH
          + File.separator + "serial"
          + File.separator + "hongs"
          + File.separator + "roles";
        for(i = 0, j = userId.length(); i < j; i += 2 ) {
            n += File.separator + userId.substring(i, j >= i+2 ? i+2 : i+1);
        }
        f = new File(n + ".ser");
        
        if (f.exists()) {
            this.load(f, 0); // 从文件加载
        } else {
            this.load(f, 1); // 从库表加载
            return;
        }
        
        DB        db;
        Table     tb;
        Table     td;
        FetchCase fc;
        Map       rs;
        int       rt;

        db = DB.getInstance("member");

        tb = db.getTable("user");
        fc = new FetchCase( )
                .select (".rtime")
                .where  (".user_id = ?",userId)
                .from   (tb.tableName, tb.name);
        rs = tb.fetchLess(fc);
        rt = Synt.declare(rs.get( "rtime" ), 0);
        if (rt > rtime) {
            load(f, 1); // 从库表加载
            return;
        }

        tb = db.getTable("dept");
        td = db.getTable("user_dept");
        fc = new FetchCase( )
                .select (".rtime")
                .orderBy(".rtime DESC")
                .from   (tb.tableName, tb.name)
                .join   (td.tableName, td.name)
                .by     (FetchCase.INNER)
                .on     (".dept_id = :dept_id")
                .where  (".user_id = ?",userId);
        rs = db.fetchLess(fc);
        rt = Synt.declare(rs.get( "rtime" ), 0);
        if (rt > rtime) {
            load(f, 1); // 从库表加载
            return;
        }
    }
    
    @Override
    protected void imports() throws HongsException {
        roles.clear();
        
        DB        db;
        Table     tb;
        Table     td;
        FetchCase fc;
        List<Map> rz;
        
        db = DB.getInstance("member");
        
        //** 查询用户权限 **/
        
        tb = db.getTable("user_role");
        fc = new FetchCase( )
                .select (".role")
                .from   (tb.tableName, tb.name)
                .where  (".user_id = ?",userId);
        rz = db.fetchMore(fc);
        for (Map rm : rz) {
            roles.add((String) rm.get("role"));
        }
        
        //** 查询部门权限 **/
        
        tb = db.getTable("dept_role");
        td = db.getTable("user_dept");
        fc = new FetchCase( )
                .select (".role")
                .from   (tb.tableName, tb.name)
                .join   (td.tableName, td.name)
                .by     (FetchCase.INNER)
                .on     (".dept_id = :dept_id")
                .where  (".user_id = ?",userId);
        rz = tb.fetchMore(fc);
        for (Map rm : rz) {
            roles.add((String) rm.get( "role" ));
        }
        
        //** 当前保存时间 **/
        
        rtime = (int) (System.currentTimeMillis() / 1000);
    }
    
}
