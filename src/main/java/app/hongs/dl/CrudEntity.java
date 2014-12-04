package app.hongs.dl;

import java.util.Map;

/**
 * CRUD 实体
 * @author Hongs
 */
public interface CrudEntity {
    
    public Map  retrieve(Map req);
    
    public String create(Map req);
    
    public int update(Map req);
    
    public int delete(Map req);
    
}
