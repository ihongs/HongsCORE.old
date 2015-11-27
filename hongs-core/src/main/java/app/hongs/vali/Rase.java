package app.hongs.vali;

/**
 * 扔掉此值
 */
public class Rase extends Rule {
    @Override
    public Object verify(Object value) {
        return SKIP ;
    }
}
