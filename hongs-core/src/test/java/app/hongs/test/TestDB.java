package app.hongs.test;

import app.hongs.db.FetchCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Hongs
 */
public class TestDB {
    
    public TestDB() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testFetchCase() {
        FetchCase caze = new FetchCase();
        caze.from   ("t")
            .select ("a,`b`,.`c`,`x`.a AS a1,`x`.`b` AS `b1`,x.c `c1`,*,c `x`")
            .select ("COUNT(d),COUNT(`e`),COUNT(.`f`),COUNT(`x`.`y`) AS `z`, 456 + c `v1`, 456 * c `v2`, 456 / c `v3`")
            .join   ("x")
            .on     ("`id` = :`id`")
            .by     (FetchCase.INNER)
            .where  ("`id` = ? AND `de` IN (?) AND `fi` like ?", 123, 456, "abc%")
            .orderBy("!`x` DESC");
        System.out.println(caze.toString());
        
        caze = new FetchCase();
        caze.from   ("a_member_user", "user")
            .where  ("`state` != 1 AND dept_id IN (?)", 1)
            .join   ("a_member_user_dept", "depts")
            .on     ("`user_id` = :`user_id`")
            .by     (FetchCase.LEFT)
            .where  ("dept_id IN (?)", 0);
        System.out.println(caze.toString());
    }

}
