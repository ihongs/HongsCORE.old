package app.hongs.veri;

import app.hongs.HongsException;
import java.util.Map;

public interface Veri {

    public Veri     addRule (String name, Rule... rule);
    public boolean  isUpdate();
    public boolean  isPrompt();
    public void     isUpdate(boolean update);
    public void     isPrompt(boolean prompt);
    public Map      verify  (  Map   values) throws Wrongs, HongsException;

}
