package net.hongs.search.serv;

import net.hongs.search.Writer;
import app.hongs.HongsException;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.FetchNext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;

/**
 * 搜索模型
 * @author Hongs
 */
@Cmdlet("search")
public class SearchCmdlet {

    @Cmdlet("index-all")
    public static void indexAll(String[] args) throws HongsException, IOException, ServletException {
        DB db = DB.getInstance("zhb");
        String sql = "SELECT uid, uname, cname, ename, intro, identity, province, trade_id FROM ts_user";
        if (args.length > 0) {
            sql += " WHERE " + args[0];
        }
        FetchNext next = db.query(sql, 0, 0);

        Writer writer = new Writer();

        Map row;
        while ((row = next.fetch()) != null) {
            String uname = (String) row.get("uname");
            if (uname == null) continue;
            String cname = (String) row.get("cname");
            if (cname == null) cname = "";
            String ename = (String) row.get("ename");
            if (ename == null) ename = "";
            String intro = (String) row.get("intro");
            if (intro == null) intro = "";

            Map rd = new HashMap();

            rd.put("id", row.get("uid"));
            rd.put("wd", uname + " " + cname + " " + ename + " "+ intro);
            rd.put("name", uname + ("".equals(ename)?"":" ("+ename+")"));
            rd.put("note", intro);
            
            Map data = new HashMap();
            rd.put("data", data );
            data.put("uname", uname);
            data.put("cname", cname);
            data.put("ename", ename);

            String x;
            x = (String) row.get("identity");
            if (x != null) rd.put("identity", x);
            x = (String) row.get("province");
            if (x != null) rd.put("province", x);
            x = (String) row.get("trade_id");
            if (x != null) rd.put("trade_id", new String[] {x});

            writer.update(rd);
            writer.commit(  );

            //app.hongs.util.Data.dumps(rd);
        }

        writer.close();
    }

}
