package app.hongs.tag;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import app.hongs.Core;

/**
 * 唯一ID生成器
 * @author Hongs
 */
public class SuidTag extends SimpleTagSupport {

  private String sid = null;

  @Override
  public void doTag() throws JspException {
    JspWriter out = getJspContext().getOut();

    String uid;
    if (this.sid != null) {
      uid = Core.getUniqueId(this.sid);
    } else {
      uid = Core.getUniqueId();
    }

    try {
      out.print(uid);

      JspFragment f = getJspBody();
      if (f != null) f.invoke(out);
    } catch (java.io.IOException ex) {
      throw new JspException("Error in UidTag", ex);
    }
  }

  public void setSid(String sid) {
    this.sid = sid;
  }
}
