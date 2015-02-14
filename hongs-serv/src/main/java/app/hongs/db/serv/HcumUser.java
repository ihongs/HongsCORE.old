package app.hongs.db.serv;

import app.hongs.HongsException;
import app.hongs.action.SiteMap;
import app.hongs.db.DB;
import app.hongs.db.Model;
import app.hongs.db.FetchCase;
import app.hongs.db.Table;
import java.util.ArrayList;
import java.util.HashMap;
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

        Table asoc = this.db.getTable("a_hcum_user_role");
        FetchCase caze = new FetchCase();
        caze.select(".role")
            .where (".user_id = ?", userId);

        Set<String> roles = new HashSet();
        List<Map>   rows  = asoc.fetchMore(caze);
        for (Map row : rows) {
            roles.add((String)row.get("role"));
        }

        return roles;
    }

    public static List getPageGroups(String name)
    throws HongsException {
        List pageGroups = new ArrayList();
        SiteMap ac = new SiteMap(name);

        Map<String, Map> pages1 = ac.pages;
        for (Map.Entry et1 : pages1.entrySet()) {
            Map page1 = (Map)et1.getValue();
            if (!page1.containsKey("pages")) {
                continue;
            }

            Map<String, Map> pages2 = (Map<String, Map>)page1.get("pages");
            for (Map.Entry et2 : pages2.entrySet()) {
                Map page2 = (Map)et2.getValue();
                if (!page2.containsKey("pages")) {
                    continue;
                }

            // 读取第2层的页面信息
            Map page_a = page2;
            Map page_b = new HashMap();
            List pages = new ArrayList();
            page_b.put("uri" , page_a.get("uri" ));
            page_b.put("name", page_a.get("name"));
            page_b.put("pages", pages);
            pageGroups.add(page_b);

                Map<String, Map> pages3 = (Map<String, Map>)page2.get("pages");
                for (Map.Entry et3 : pages3.entrySet()) {
                    Map page3 = (Map)et3.getValue();
                    if (!page3.containsKey("groups")) {
                        continue;
                    }

            // 读取第3层的页面信息
            Map page_c = page3;
            Map page_d = new HashMap();
            page_d.put("uri" , page_c.get("uri" ));
            page_d.put("name", page_c.get("name"));
            pages.add(page_d);

            // 读取页面分组信息
            List groups = new ArrayList();
            page_d.put("groups",  groups);
            Set<String> groupz = (Set)page_c.get("groups");
            for (String k : groupz) {
                Map group1 = ac.getRole(k);
                Map group2 = new HashMap( );
                groups.add( group2 );
                group2.put("name", group1.get("name" ));
                group2.put("disp", group1.get("disp"));
                group2.put("groups", ac.getMoreRoles(k).keySet());
            }

                }
            }
        }

        return pageGroups;
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
