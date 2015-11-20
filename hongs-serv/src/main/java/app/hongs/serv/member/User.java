package app.hongs.serv.member;

import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.Model;
import app.hongs.db.Table;
import app.hongs.serv.manage.auth.AuthKit;
import app.hongs.util.Synt;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户基础信息模型
 * @author Hongs
 */
public class User
extends Model {

    public User()
    throws HongsException {
        this(DB.getInstance("member").getTable("user"));
    }

    public User(Table table)
    throws HongsException {
        super(table);
    }

    @Override
    public String add(Map data) throws HongsException {
        // 加密密码
        if (data.containsKey("password")) {
            data.put("password", AuthKit.getCrypt((String) data.get("password")));
        }

        // 权限限制, 仅能赋予当前登录用户所有的权限
        if (data.containsKey( "roles"  )) {
            data.put("rtime", System.currentTimeMillis() / 1000);
            AuthKit.clnRoles( Synt.declare(data.get("roles"), List.class), null );
        }

        return super.add(data);
    }

    @Override
    public int put(String id, Map data, FetchCase caze) throws HongsException {
        // 加密密码
        if (data.containsKey("password")) {
            data.put("password", AuthKit.getCrypt((String) data.get("password")));
        }

        // 权限限制, 仅能赋予当前登录用户所有的权限
        if (data.containsKey( "roles"  )) {
            data.put("rtime", System.currentTimeMillis() / 1000);
            AuthKit.clnRoles( Synt.declare(data.get("roles"), List.class),  id  );
        }

        return super.put(id, data, caze);
    }

    public Set<String> getRoles(String userId)
    throws HongsException {
        if (userId == null) throw new HongsException(0x10000, "User Id required!");

        Table       asoc;
        FetchCase   caze;
        List<Map>   rows;
        Set<String> roles = new HashSet();
        Set<String> depts = new HashSet();

        asoc = this.db.getTable("a_member_user_dept");
        caze = new FetchCase( );
        caze.select(".dept_id")
            .where (".user_id = ?", userId);
        rows = asoc.fetchMore(caze);
        for (Map row : rows) {
            depts.add((String) row.get("dept_id") );
        }

        asoc = this.db.getTable("a_member_dept_role");
        caze = new FetchCase();
        caze.select(".role"  )
            .where (".dept_id = ?", depts );
        rows = asoc.fetchMore(caze);
        for (Map row : rows) {
            roles.add((String) row.get("role"));
        }

        asoc = this.db.getTable("a_member_user_role");
        caze = new FetchCase();
        caze.select(".role"  )
            .where (".user_id = ?", userId);
        rows = asoc.fetchMore(caze);
        for (Map row : rows) {
            roles.add((String) row.get("role"));
        }

        return roles;
    }

    @Override
    protected void filter(FetchCase caze, Map req)
    throws HongsException {
        super.filter(caze, req);

        /**
         * 如果有指定dept_id
         * 则关联a_member_user_dept来约束范围
         */
        Object deptId = req.get("dept_id");
        if (null != deptId && !"".equals(deptId)) {
            caze.join ("a_member_user_dept", "depts")
                .on   (".user_id = :id")
                .where(".dept_id IN (?)",deptId);
        }
    }

}
