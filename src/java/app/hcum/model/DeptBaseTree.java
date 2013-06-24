package app.hcum.model;

import app.hongs.Core;
import app.hongs.db.AbstractTreeModelOld;

/**
 * 部门基础信息树模型
 * @author Hongs
 */
public class DeptBaseTree
extends AbstractTreeModelOld {

  public DeptBaseTree() {
      super((DeptBaseInfo)Core.getInstance("app.hcum.model.DeptBaseInfo"));
      this.cnumKey = "cnum";
  }

}
