package app.hcum.model;

import app.hongs.HongsException;
import app.hongs.db.AbstractTreeModel;
import app.hongs.db.FetchMore;
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
      super("hcum", "a_hcum_dept");
  }

    public Set<String> getGroups(String deptId)
    throws HongsException {
        if (deptId == null) throw new HongsException(0x10000, "Dept Id required!");

        Table asoc = this.db.getTable("a_hcum_dept_group");
        FetchMore more = new FetchMore();
        more.select(".group_key")
            .where(".dept_id = ?", deptId);

        Set<String> groups = new HashSet();
        List<Map>   rows   = asoc.fetchMore(more);
        for (Map row : rows) {
            groups.add((String)row.get("group_key"));
        }

        return groups;
    }

    @Override
    protected void reqFilter(Map req, FetchMore more)
    throws HongsException {
        super.reqFilter(req, more);

        /**
         * 如果有指定user_id
         * 则关联a_hcum_user_detp来约束范围
         */
        if (req.containsKey("user_id")) {
            more.join ("a_hcum_user_dept", ".dept_id = :id")
                .where("user_id = ?" , req.get( "user_id" ));
        }
    }

}
