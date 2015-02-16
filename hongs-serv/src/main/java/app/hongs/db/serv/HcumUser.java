package app.hongs.db.serv;

import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Model;
import app.hongs.db.FetchCase;
import app.hongs.db.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户基础信息模型
 * @author Hongs
 */
public class HcumUser
extends Model {

    public HcumUser()
    throws HongsException {
        this(DB.getInstance("hcum").getTable("user"));
    }

    public HcumUser(Table table)
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
    if (id == null || id.length() == 0)
      id = this.add(rd);
    else
      this.put(id , rd);
    return id;
  }

    public Set<String> getRoles(String userId)
    throws HongsException {
        if (userId == null) throw new HongsException(0x10000, "User Id required!");

        Table       asoc;
        FetchCase   caze;
        List<Map>   rows;
        Set<String> roles = new HashSet();
        Set<String> depts = new HashSet();

        asoc = this.db.getTable("a_hcum_user_dept");
        caze = new FetchCase( );
        caze.select(".dept_id")
            .where (".user_id = ?", userId);
        rows = asoc.fetchMore(caze);
        for (Map row : rows) {
            depts.add((String) row.get("dept_id") );
        }

        asoc = this.db.getTable("a_hcum_dept_role");
        caze = new FetchCase();
        caze.select(".role"  )
            .where (".dept_id = ?", depts );
        rows = asoc.fetchMore(caze);
        for (Map row : rows) {
            roles.add((String) row.get("role"));
        }

        asoc = this.db.getTable("a_hcum_user_role");
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
         * 则关联a_hcum_user_detp来约束范围
         */
        if (req.containsKey("dept_id")) {
            caze.join ("a_hcum_user_dept", "depts")
                .on   (".user_id = :id")
                .where("dept_id = ?" , req.get( "dept_id" ));
        }
    }

}
