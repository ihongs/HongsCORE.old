package app.hongs.veri;

import java.util.Collection;
import java.util.Map;

public class Required extends Rule {
    @Override
    public Object verify(Object value) throws Wrong {
        if (value  ==  null ) {
            if (helper.isUpdate()) {
                return INVAL;
            }
            throw new Wrong("fore.form.required");
        }
        if ("".equals(value)) {
            throw new Wrong("fore.form.required");
        }
        if ((value instanceof Object[ ] ) && ((Object[ ] ) value).length==0) {
            throw new Wrong("fore.form.requreid");
        }
        if ((value instanceof Collection) && ((Collection) value).isEmpty()) {
            throw new Wrong("fore.form.requreid");
        }
        if ((value instanceof Map ) && ((Map ) value).isEmpty()) {
            throw new Wrong("fore.form.requreid");
        }
        return value;
    }
}
