package app.model;

import java.util.Map;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;
import app.hongs.db.FetchBean;

/**
 * 演示模型
 * @author Hongs
 */
public class Demo extends AbstractBaseModel
{

  public Demo()
    throws HongsException
  {
    super("hs_demo");
  }

  @Override
  public void getFilter(Map req, FetchBean fs)
    throws HongsException
  {
    super.getFilter(req, fs);

    /**
     * 如果存在mctime参数, 则仅取某天的数据
     */
    if (req.containsKey("mctime")) {
      String mctime = (String)req.get("mctime");
      DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
      Long dt1, dt2;
      try {
        dt1 = df.parse(mctime).getTime() / 1000;
        dt2 = dt1 + 86400;
      } catch (ParseException ex) {
        throw new HongsException(0x1000, ex);
      }
      fs.where("((.__mtime__ >= ? AND .__mtime__ <= ?) OR (.__ctime__ >= ? AND .__ctime__ <= ?))",
               dt1, dt2, dt1, dt2);
    }
  }

}
