package app.model.cums;

import app.hongs.Core;
import app.hongs.db.AbstractTreeModel;

/**
 * 部门基础信息树模型
 * @author Hongs
 */
public class DeptBaseTree
extends AbstractTreeModel {

  public DeptBaseTree() {
      super((DeptBaseInfo)Core.getInstance("app.model.cums.DeptBaseInfo"));
      this.cnumKey = "cnum";
  }

}
