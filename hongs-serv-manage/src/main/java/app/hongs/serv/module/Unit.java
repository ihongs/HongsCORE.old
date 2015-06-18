package app.hongs.serv.module;

import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Model;
import app.hongs.db.Table;
import java.util.Map;

/**
 *
 * @author Hongs
 */
public class Unit extends Model {
    
    public Unit() throws HongsException {
        this(DB.getInstance("module").getTable("unit"));
    }

    public Unit(Table table)
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
    public String save(Map rd) throws HongsException {
      String id = (String) rd.get(this.table.primaryKey);
      if (id == null || id.length() == 0) {
          id = this.add(rd);
      } else {
          this.put(rd , id);
      }
      return id;
    }

    public void updateOrCreateMenuSetFile() {
        
    }
    
}
