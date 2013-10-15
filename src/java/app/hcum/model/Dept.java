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
    protected void getFilter(Map req, FetchMore more)
    throws HongsException {
        super.getFilter(req, more);

        /**
         * 如果有指定user_id
         * 则关联a_hcum_user_detp来约束范围
         */
        if (req.containsKey("user_id")) {
            more.join ("a_hcum_user_dept", ".dept_id = :id")
                .where("user_id = ?" , req.get( "user_id" ));
        }
    }

    public String getAffectedNames() throws HongsException {
        StringBuilder sb = new StringBuilder();
        FetchMore     fm = new FetchMore();
        fm.setOption("FETCH_DFLAG", true );
        fm.select("name").where("id IN (?)", this.affectedIds);
        List<Map> rows = this.table.fetchMore(fm);
        for (Map  row  : rows) {
            sb.append(",").append(row.get("name").toString( ));
        }
        return sb.length()>0 ? sb.substring(1) : sb.toString();
    }

}
