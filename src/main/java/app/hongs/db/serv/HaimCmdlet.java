package app.hongs.db.serv;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Table;
import app.hongs.db.sync.TableSync;
import java.util.List;

/**
 * 自动模块工具
 * @author Hongs
 */
public class HaimCmdlet {
    
  private static void diffTable(String tables) throws HongsException {
      String[] ts = tables.split(":");
      String srcTableName = ts[0];
      String dstTableName = ts[1];
      DB db = Core.getInstance(app.hongs.db.DB.class);
      Table srcTable = Table.getInstanceByName(db, srcTableName);
      Table dstTable = Table.getInstanceByName(db, dstTableName);
      TableSync sync = new TableSync(srcTable);
      List<String> sqls = sync.syncSlaverSqls(dstTable, true);
      for (String sql : sqls) {
          System.out.println(sql);
      }
  }
  
}
