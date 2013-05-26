package app.hongs.tag;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.util.Text;

/**
 * <h1>配置信息读取标签</h1>
 *
 * <h2>使用方法:</h2>
 * <pre>
 * &lt;hs:conf load"config.name"/&gt;
 * &lt;hs:conf key="config.key" [esc="yes|no|EscapeSymbol"] [def="default.value"]/&gt;
 * </pre>
 *
 * @author Hongs
 */
public class ConfTag extends TagSupport {

  private String load = null;
  private String key = null;
  private String esc = null;
  private String def = null;

  @Override
  public int doStartTag() throws JspException {
    JspWriter out = this.pageContext.getOut();

    CoreConfig conf = (CoreConfig)Core.getInstance("app.hongs.CoreConfig");

    if (this.load != null)
    {
      if (this.load.endsWith(".xml")) {
        conf.loadFromXML(this.load.substring(0 , this.load.length( ) - 4));
      }
      else {
        conf.load(this.load);
      }
    }

    if (this.key != null)
    {
      String str = conf.getProperty(this.key , this.def!=null?this.def:"");

      if (this.esc != null
      &&  ! "".equals(this.esc)
      &&  ! "no".equals(this.esc)) {
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
        throw new JspException("Error in ConfTag", ex);
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

  public void setDef(String def) {
    this.def = def;
  }

}
