package app.hongs.serv;

import app.hongs.action.ActionHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Hongs
 */
public class Auth {
    
    public Auth(String name) {
        
    }
    
    public Auth() {
        
    }
    
    public Set<String> getActions() {
        return null;
    }
    
    public boolean checkAuth(String action, Set roles) {
        return true;
    }
    
    public static Auth getInstance() {
        return new Auth();
    }
    
    public static Auth getInstance(String name) {
        return new Auth(name);
    }
    
}
