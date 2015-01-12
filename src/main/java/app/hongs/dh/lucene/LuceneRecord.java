/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package app.hongs.dh.lucene;

import app.hongs.HongsException;
import app.hongs.action.SourceConfig;
import app.hongs.dh.IRecord;
import app.hongs.util.Dict;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.hongs.search.Reader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;

/**
 *
 * @author Hongs
 */
public class LuceneRecord implements IRecord {

    Map items;

    public LuceneRecord(String confName, String unitName) throws HongsException {
        this.items = SourceConfig.getInstance(confName).getItems(unitName);
    }

    public Map retrieve(Map rd) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String[] create(Map rd) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int update(Map rd) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int delete(Map rd) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Sort getSort(Map rd) throws HongsException {
        Set<String> ob = Dict.deem4Def(rd.get("ob"), new HashSet());
        List<SortField> of = new ArrayList();

        for (String fn: ob) {
            boolean rv;
            if (fn.startsWith("-") ) {
                fn = fn.substring(1);
                rv = true ;
            } else {
                rv = false;
            }

            Map fm = (Map ) items.get(fn);
            if (fm == null) {
                continue;
            }

            SortField.Type st;
            if ("number".equals(fm.get("_type"))) {
                Object   nt  =  fm.get( "type");
                if ("int".equals(nt)) {
                    st = SortField.Type.INT;
                } else
                if ("long".equals(nt)) {
                    st = SortField.Type.LONG;
                } else
                if ("float".equals(nt)) {
                    st = SortField.Type.FLOAT;
                } else
                {
                    st = SortField.Type.DOUBLE;
                }
            } else {
                    st = SortField.Type.STRING;
            }

            of.add( new SortField(fn, st, rv));
        }

        return of.isEmpty() ? null : new Sort(of.toArray(new SortField[0]));
    }

    private Query getQuery(Map rd) throws HongsException {
        BooleanQuery query = new BooleanQuery();

        for (Object o : rd.entrySet( )) {
            Map.Entry e = (Map.Entry) o;
            String k = (String) e.getKey();
            Object v = e.getValue();

            Map    m = (Map) items.get("k");
            if (null == m) {
                continue;
            }

            // 存储类型
            String g = Dict.deem4Def(m.get("field_genre"), "");
            if ("stored".equals(g)) {
                continue;
            }

            // 字段类型
            String t = Dict.deem4Def(m.get("_type"), "");
            if ("number".equals(t)) {
                // 数字类型
                String l = Dict.deem4Def(m.get("type"), "");
                if ("int".equals(l)) {
                    addQuery(query, k, v, new AddIntQuery());
                } else
                if ("long".equals(l)) {
                    addQuery(query, k, v, new AddLongQuery());
                } else
                if ("float".equals(l)) {
                    addQuery(query, k, v, new AddFloatQuery());
                } else
                {
                    addQuery(query, k, v, new AddDoubleQuery());
                }
            } else
            {
                if ("text".equals(g)) {
                    addQuery(query, k, v, new AddTextQuery());
                } else
                {
                    addQuery(query, k, v, new AddStringQuery());
                }
            }
        }

        return  query;
    }

    private void addQuery(BooleanQuery qs, String k, Object v, AddQuery q) {
        Map m;
        if (v instanceof Map) {
            m = (Map) v ;
        } else {
            if (null==v || "".equals(v)) {
                return;
            }
            m = new HashMap();
            if (v instanceof Collection) {
                m.put("-in", v);
            } else {
                m.put("-eq", v);
            }
        }

        if (m.containsKey("-eq")) {
            Object n = m.get("-eq");
            qs.add(q.add(k, n), BooleanClause.Occur.MUST);
        }
        if (m.containsKey("-ne")) {
            Object n = m.get("-eq");
            qs.add(q.add(k, n), BooleanClause.Occur.MUST_NOT);
        }
        if (m.containsKey("-or")) {
            int n = Dict.deem4Def(m.get("-eq"), 0);
            qs.add(q.add(k, n), BooleanClause.Occur.SHOULD);
        }
        if (m.containsKey("-in")) {
            BooleanQuery qz = new BooleanQuery();
            Set a = Dict.deem4Def(m.get("-in"), new HashSet());
            for(Object x : a) {
                int n = Dict.deem4Def(x, 0);
                qs.add(q.add(k, n), BooleanClause.Occur.SHOULD);
            }
            qs.add(qz, BooleanClause.Occur.MUST);
        }
        if (m.containsKey("-ei")) {
            Set a = Dict.deem4Def(m.get("-oi"), new HashSet());
            for(Object x : a) {
                int n = Dict.deem4Def(x, 0);
                qs.add(q.add(k, n), BooleanClause.Occur.MUST);
            }
        } else
        if (m.containsKey("-ni")) {
            Set a = Dict.deem4Def(m.get("-ni"), new HashSet());
            for(Object x : a) {
                int n = Dict.deem4Def(x, 0);
                qs.add(q.add(k, n), BooleanClause.Occur.MUST_NOT);
            }
        } else
        if (m.containsKey("-oi")) {
            Set a = Dict.deem4Def(m.get("-oi"), new HashSet());
            for(Object x : a) {
                int n = Dict.deem4Def(x, 0);
                qs.add(q.add(k, n), BooleanClause.Occur.SHOULD);
            }
        }
        {
            Integer n, x;
            boolean l, r;

            if (m.containsKey("-gt")) {
                n = Dict.deem4Def(m.get("-gt"), 0); l = false;
            } else
            if (m.containsKey("-ge")) {
                n = Dict.deem4Def(m.get("-ge"), 0); l = true;
            } else
            {
                n = null; l = true;
            }

            if (m.containsKey("-lt")) {
                x = Dict.deem4Def(m.get("-lt"), 0); r = false;
            } else
            if (m.containsKey("-le")) {
                x = Dict.deem4Def(m.get("-le"), 0); r = true;
            } else
            {
                x = null; r = true;
            }

            qs.add(q.add(k, n, x, l, r), BooleanClause.Occur.MUST);
        }
    }

    private static interface AddQuery {
        public Query add(String k, Object v);
        public Query add(String k, Object n, Object x, boolean l, boolean r);
    }

    private static class AddIntQuery implements AddQuery {
        public Query add(String k, Object v) {
            int n2 = Dict.deem4Cls(v, Integer.class);
            return NumericRangeQuery.newIntRange(k, n2, n2, true, true);
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            int n2 = Dict.deem4Cls(n, Integer.class);
            int x2 = Dict.deem4Cls(x, Integer.class);
            return NumericRangeQuery.newIntRange(k, n2, x2, l, r);
        }
    }

    private static class AddLongQuery implements AddQuery {
        public Query add(String k, Object v) {
            long n2 = Dict.deem4Cls(v, Long.class);
            return NumericRangeQuery.newLongRange(k, n2, n2, true, true);
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            long n2 = Dict.deem4Cls(n, Long.class);
            long x2 = Dict.deem4Cls(x, Long.class);
            return NumericRangeQuery.newLongRange(k, n2, x2, l, r);
        }
    }

    private static class AddFloatQuery implements AddQuery {
        public Query add(String k, Object v) {
            float n2 = Dict.deem4Cls(v, Float.class);
            return NumericRangeQuery.newFloatRange(k, n2, n2, true, true);
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            float n2 = Dict.deem4Cls(n, Float.class);
            float x2 = Dict.deem4Cls(x, Float.class);
            return NumericRangeQuery.newFloatRange(k, n2, x2, l, r);
        }
    }

    private static class AddDoubleQuery implements AddQuery {
        public Query add(String k, Object v) {
            double n2 = Dict.deem4Cls(v, Double.class);
            return NumericRangeQuery.newDoubleRange(k, n2, n2, true, true);
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            double n2 = Dict.deem4Cls(n, Double.class);
            double x2 = Dict.deem4Cls(x, Double.class);
            return NumericRangeQuery.newDoubleRange(k, n2, x2, l, r);
        }
    }

    private static class AddStringQuery implements AddQuery {
        public Query add(String k, Object v) {
            String n2 = v.toString();
            return new TermQuery(new Term(k, n2));
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            String n2 = n.toString();
            String x2 = x.toString();
            return TermRangeQuery.newStringRange(k, n2, x2, l, r);
        }
    }

    private static class AddTextQuery implements AddQuery {
        public Query add(String k, Object v) {
            String n2 = v.toString();
            return new TermQuery(new Term(k, n2));
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            String n2 = n.toString();
            String x2 = x.toString();
            return TermRangeQuery.newStringRange(k, n2, x2, l, r);
        }
    }

    public void readerBegin() throws HongsException {
//        try {
//            reader.close();
//        } catch (IOException ex) {
//            throw new HongsException(HongsException.COMMON, ex);
//        }
    }

    public void readerClose() throws HongsException {
//        try {
//            reader.close();
//        } catch (IOException ex) {
//            throw new HongsException(HongsException.COMMON, ex);
//        }
    }

}
