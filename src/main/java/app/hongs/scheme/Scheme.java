package app.hongs.scheme;

import java.util.Map;

/**
 * 规划模型
 * @author Hongs
 */
public interface Scheme {
    
    public Scheme getEntity(String entity );
    
    public String create(Map req);
    
    public int update(Map req);
    
    public int delete(Map req);
    
    public Map retrieve(Map req);
    
}
