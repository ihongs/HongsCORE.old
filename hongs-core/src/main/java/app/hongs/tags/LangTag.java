package app.hongs.tags;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.util.Text;

/**
 * 语言信息读取标签
 *
 * <h3>使用方法:</h3>
 * <pre>
 * &lt;hs:lang load="language.name"/&gt;
 * &lt;hs:lang key="language.key" [esc="yes|no|EscapeSymbol"] [_0="replacement0" _1="replacement1"]/&gt;
 * &lt;hs:lang key="language.key" [esc="yes|no|EscapeSymbol"] [xx="replacementX" yy="replacementY"]/&gt;
 * &lt;hs:lang key="language.key" [esc="yes|no|EscapeSymbol"] [rep=String[]|List&lt;String&gt;|Map&lt;String, String&gt;]/&gt;
 * </pre>
 *
 * @author Hongs
 */
public class LangTag extends TagSupport implements DynamicAttributes {

  private String key = null;
  private String esc = null;
  private String load = null;
  private String[] repArr = null;
  private List<String> repLst = null;
  private Map<String, String> repMap = null;

  @Override
  public int doStartTag() throws JspException {
    JspWriter out = this.pageContext.getOut();

    CoreLanguage lang = CoreLanguage.getInstance().clone();

    if (this.load != null) {
      lang.load(this.load);
    }

    if (this.key  != null) {
      String str;
      if (this.repMap != null) {
        str = lang.translate(this.key, this.repMap);
      }
      else if (this.repLst != null) {
        str = lang.translate(this.key, this.repLst);
      }
      else if (this.repArr != null) {
        str = lang.translate(this.key, this.repArr);
      }
      else {
        str = lang.translate(this.key);
      }

      if (this.esc != null
      && ! "".equals(this.esc)
      && ! "no".equals(this.esc)) {
        if ("yes".equals(this.esc)) {
          str = Text.escape(str);
        }
        else {
          str = Text.escape(str, this.esc);
        }
      }

      try {
        out.print(str);
      } catch (java.io.IOException ex) {
        throw new JspException("Error in LangTag", ex);
      }
    }

    return TagSupport.SKIP_BODY;
  }

  public void setLoad(String load) {
    this.load = load;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setEsc(String esc) {
    this.esc = esc;
  }

  @Override
  public void setDynamicAttribute(String uri, String name, Object value) throws JspException {
    if (value instanceof Map) {
      this.repMap = (Map<String, String>)value;
    }
    else if (value instanceof List) {
      this.repLst = (List<String>)value;
    }
    else if (value instanceof Object[]) {
      this.repArr = (String[])value;
    }
    else {
      if (name.matches("^_\\d+$")) {
        name = name.substring(1);
      }
      if (this.repMap == null) {
        this.repMap = new HashMap<String, String>();
      }
      this.repMap.put(name, (String)value);
    }
  }

}