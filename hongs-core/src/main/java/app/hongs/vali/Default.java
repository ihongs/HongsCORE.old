package app.hongs.vali;

import app.hongs.util.Synt;

public class Default extends Rule {
    @Override
    public Object verify(Object value) {
        if (/*DEF*/ value == null || Synt.declare(params.get("alwayset"), false)) { // 无论有没有都设置
            if (helper.isUpdate() && Synt.declare(params.get("increate"), false)) { // 仅创建的时候设置
                return INVAL;
            }
            value = params.get("default");
            if ( "$now".equals( value ) ) {
                value = new java.util.Date();
            }
        }
        return  value;
    }
}
