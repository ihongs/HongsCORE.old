package app.hongs.veri;

public class Optional extends Rule {
     @Override
     public Object verify(Object value) throws Wrong {
         try {
             Required rule = new Required();
             rule.setHelper(helper);
             rule.setParams(params);
             rule.setValues(values);
             value = rule.verify(value);
         }   catch (Wrong w) {
             return INVAL;
         }
         return value;
     }
 }
