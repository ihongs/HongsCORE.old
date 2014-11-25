package app.hongs.scheme;

import java.util.Map;

/**
 * 实体模型
 * @author Hongs
 */
public interface Entity {
    
    public Scheme getScheme();
    
    public String create(Map req);
    
    public int update(Map req);
    
    public int delete(Map req);
    
    public Map retrieve(Map req);
    
}
