package app.hongs.vali;

import app.hongs.CoreLocale;
import app.hongs.HongsException;
import java.util.HashMap;
import java.util.Map;

public class Wrong extends HongsException {
    String name = null;

    public Wrong(Throwable cause, String desc, String... prms) {
        super(HongsException.NOTICE, desc, cause);
        this.setLocalizedSection("default");
        this.setLocalizedOptions(prms);
    }

    public Wrong(String desc, String... prms) {
        super(HongsException.NOTICE, desc );
        this.setLocalizedSection("default");
        this.setLocalizedOptions(prms);
    }

    public Wrong  setLocalizedSegment(String name) {
        this.name = name;
        return this;
    }

    public String getLocalizedSegment() {
        return name;
    }

    @Override
    public String getLocalizedMessage() {
        CoreLocale trns = CoreLocale.getInstance(getLocalizedSection());
        String [ ] rep1 = getLocalizedOptions( );
        Map<String, String> rep2 = new HashMap();
        for( int i = 0; i < rep1.length; i ++  ) {
            rep2.put(String.valueOf(i), rep1[i]);
        }
        if ( null != name ) {
            rep2.put("_" , trns.translate(name));
        }
        return trns.translate(getDesc( ), rep2 );
    }
}
