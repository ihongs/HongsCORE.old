package app.hongs.db.serv;

import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Mtree;
import app.hongs.db.FetchCase;
import app.hongs.db.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门基础信息树模型
 * @author Hongs
 */
public class HcumDept
extends Mtree {

    public HcumDept()
    throws HongsException {
        this(DB.getInstance("hcum").getTable("dept"));
    }

    public HcumDept(Table table)
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

    public Set<String> getRoles(String deptId)
    throws HongsException {
        if (deptId == null) throw new HongsException(0x10000, "Dept Id required!");

        Table asoc = this.db.getTable("a_hcum_dept_role");
        FetchCase caze = new FetchCase();
        caze.select(".role")
            .where (".dept_id = ?", deptId);

        Set<String> roles = new HashSet();
        List<Map>   rows  = asoc.fetchMore(caze);
        for (Map row : rows) {
            roles.add((String)row.get("role"));
        }

        return roles;
    }

    @Override
    protected void filter(FetchCase caze, Map req)
    throws HongsException {
        super.filter(caze, req);

        /**
         * 如果有指定user_id
         * 则关联a_hcum_user_detp来约束范围
         */
        if (req.containsKey("user_id")) {
            caze.join ("a_hcum_user_dept", ".dept_id = :id")
                .where("user_id = ?" , req.get( "user_id" ));
        }
    }

}
