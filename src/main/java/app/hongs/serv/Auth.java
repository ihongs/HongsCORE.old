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
    
    public Map getAuth(ActionHelper helper, String conf, String sesn) {
        return null;
    }
    
    public Map getAuth(ActionHelper helper, String conf) {
        return getAuth(helper,   conf   , "actions");
    }
    
    public Map getAuth(ActionHelper helper) {
        return getAuth(helper, "default", "actions");
    }
    
    private Map getActs(Map unis, Set sess) {
        Map acts = new HashMap();
        for (Object o : unis.entrySet()) {
            Map.Entry e = (Map.Entry) o;
        }
        return null;
    }
    
}
