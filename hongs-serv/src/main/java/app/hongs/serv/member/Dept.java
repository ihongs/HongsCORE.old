package app.hongs.serv.member;

import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Mtree;
import app.hongs.db.FetchCase;
import app.hongs.db.Table;
import app.hongs.serv.manage.auth.AuthKit;
import app.hongs.util.Synt;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门基础信息树模型
 * @author Hongs
 */
public class Dept
extends Mtree {

    public Dept()
    throws HongsException {
        this(DB.getInstance("member").getTable("dept"));
    }

    public Dept(Table table)
    throws HongsException {
        super(table);
    }

    /**
     * 添加/修改记录
     *
     * @param rd
     * @return 记录ID
     * @throws app.hongs.HongsException
     */
    public String save(Map rd)
      throws HongsException
    {
      String id = (String)rd.get(this.table.primaryKey);
      if (rd.containsKey("roles")) {
          rd.put("rtime", System.currentTimeMillis() / 1000);
      }
      if (id == null || id.length() == 0) {
          id = this.add(rd);
      } else {
          this.put(rd , id);
      }
      return id;
    }

    @Override
    public String add(Map data) throws HongsException {
        // 权限限制, 仅能赋予当前登录用户所有的权限
        if (data.containsKey("roles") ) {
            AuthKit.clnRoles( Synt.declare(data.get("roles"), List.class), null );
        }

        return super.add(data);
    }

    @Override
    public int put(Map data, String id, FetchCase caze) throws HongsException {
        // 权限限制, 仅能赋予当前登录用户所有的权限
        if (data.containsKey("roles") ) {
            data.put("rtime", System.currentTimeMillis() / 1000);
            AuthKit.clnRoles( Synt.declare(data.get("roles"), List.class),  id  );
        }

        return super.put(data, id, caze);
    }

    public Set<String> getRoles(String deptId)
    throws HongsException {
        if (deptId == null) throw new HongsException(0x10000, "Dept Id required!");

        Table       asoc;
        FetchCase   caze;
        List<Map>   rows;
        Set<String> roles = new HashSet();

        asoc = this.db.getTable("a_member_dept_role");
        caze = new FetchCase();
        caze.select(".role"  )
            .where (".dept_id = ?", deptId);
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
         * 如果有指定user_id
         * 则关联a_member_user_dept来约束范围
         */
        Object userId = req.get("user_id");
        if (null != userId && !"".equals(userId)) {
            caze.join ("a_member_user_dept", "users")
                .on   (".dept_id = :id")
                .where(".user_id IN (?)",userId);
        }
    }

}
