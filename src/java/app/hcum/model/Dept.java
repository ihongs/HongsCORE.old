package app.hcum.model;

import app.hongs.HongsException;
import app.hongs.db.AbstractTreeModel;
import app.hongs.db.FetchBean;
import app.hongs.db.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门基础信息树模型
 * @author Hongs
 */
public class Dept
extends AbstractTreeModel {

  public Dept() throws HongsException {
      super("hcum", "au_dept_base_info");
  }

    public Set<String> getGroups(String deptId)
    throws HongsException {
        if (deptId == null) throw new HongsException(0x10000, "Dept Id required!");

        Table asoc = this.db.getTable("au_dept_group_asoc");
        FetchBean fa = new FetchBean();
        fa.setSelect(".group_name")
          .setWhere(".dept_id = ?", deptId);

        List<Map> rows = asoc.fetchMore(fa);
        Set<String> groups = new HashSet( );
        for (Map row : rows) {
            groups.add((String)row.get("group_name"));
        }

        return groups;
    }

    @Override
    protected void getFilter(Map req, FetchBean fa)
    throws HongsException {
        super.getFilter(req, fa);

        /**
         * 如果有指定user_id
         * 则关联a_user_detp_asoc来约束范围
         */
        if (req.containsKey("user_id")) {
            fa.join("au_user_dept_asoc",
                    ".dept_id = ..id")
              .where("user_id = ?", req.get("user_id"));
        }
    }

}
