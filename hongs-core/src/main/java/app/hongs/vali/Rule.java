package app.hongs.vali;

import app.hongs.HongsException;
import java.util.Map;

public abstract class Rule {
    public Map params = null;
    public Map values = null;
    public Vali helper;

    public static final Object SKIP = new Object();

    public void setParams(Map params) {
        this.params = params;
    }
    public void setValues(Map values) {
        this.values = values;
    }
    public void setHelper(Vali helper) {
        this.helper = helper;
    }

    public abstract Object verify(Object value) throws Wrong, Wrongs, HongsException;
}
