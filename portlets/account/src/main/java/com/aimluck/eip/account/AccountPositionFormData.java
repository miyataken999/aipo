/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2008 Aimluck,Inc.
 * http://aipostyle.com/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.aimluck.eip.account;

import java.util.Date;
import java.util.List;

import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectQuery;
import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import com.aimluck.commons.field.ALStringField;
import com.aimluck.eip.account.util.AccountUtils;
import com.aimluck.eip.cayenne.om.account.EipMPosition;
import com.aimluck.eip.cayenne.om.security.TurbineUser;
import com.aimluck.eip.common.ALAbstractFormData;
import com.aimluck.eip.common.ALDBErrorException;
import com.aimluck.eip.common.ALEipConstants;
import com.aimluck.eip.common.ALEipManager;
import com.aimluck.eip.common.ALPageNotFoundException;
import com.aimluck.eip.modules.actions.common.ALAction;
import com.aimluck.eip.orm.DatabaseOrmService;
import com.aimluck.eip.util.ALEipUtils;

/**
 *　役職を管理するフォームデータを管理するクラスです。 <BR>
 *
 */
public class AccountPositionFormData extends ALAbstractFormData {

  /** logger */
  private static final JetspeedLogger logger = JetspeedLogFactoryService
      .getLogger(AccountPositionFormData.class.getName());

  /** 役職名 */
  private ALStringField position_name;

  /** 役職ID */
  private int position_id;

  private DataContext dataContext;

  /**
   * 初期化します。
   *
   * @param action
   * @param rundata
   * @param context
   * @see com.aimluck.eip.common.ALAbstractFormData#init(com.aimluck.eip.modules.actions.common.ALAction,
   *      org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
   */
  public void init(ALAction action, RunData rundata, Context context)
      throws ALPageNotFoundException, ALDBErrorException {
    super.init(action, rundata, context);
  }

  /**
   * 各フィールドを初期化します。 <BR>
   *
   * @see com.aimluck.eip.common.ALData#initField()
   */
  public void initField() {
    dataContext = DatabaseOrmService.getInstance().getDataContext();

    // 役職名
    position_name = new ALStringField();
    position_name.setFieldName("役職名");
    position_name.setTrim(true);
  }

  /**
   * 各フィールドに対する制約条件を設定します。 <BR>
   *
   * @see com.aimluck.eip.common.ALAbstractFormData#setValidator()
   */
  protected void setValidator() {
    position_name.setNotNull(true);
    position_name.limitMaxLength(50);
  }

  /**
   *
   * @param rundata
   * @param context
   * @param msgList
   * @return
   * @see com.aimluck.eip.common.ALAbstractFormData#setFormData(org.apache.turbine.util.RunData,
   *      org.apache.velocity.context.Context, java.util.ArrayList)
   */
  protected boolean setFormData(RunData rundata, Context context,
      List<String> msgList) throws ALPageNotFoundException, ALDBErrorException {
    boolean res = super.setFormData(rundata, context, msgList);
    if (res) {
      try {
        if (ALEipConstants.MODE_UPDATE.equals(getMode())) {
          position_id = Integer.parseInt(ALEipUtils.getTemp(rundata, context,
              ALEipConstants.ENTITY_ID));
        }
      } catch (Exception ex) {
        logger.error("Exception", ex);
      }
    }
    return res;
  }

  /**
   * フォームに入力されたデータの妥当性検証を行います。 <BR>
   *
   * @param msgList
   * @return
   * @see com.aimluck.eip.common.ALAbstractFormData#validate(java.util.ArrayList)
   */
  protected boolean validate(List<String> msgList) {
    position_name.validate(msgList);

    try {
      SelectQuery query = new SelectQuery(EipMPosition.class);

      if (ALEipConstants.MODE_INSERT.equals(getMode())) {
        Expression exp = ExpressionFactory.matchExp(
            EipMPosition.POSITION_NAME_PROPERTY, position_name.getValue());
        query.setQualifier(exp);
      } else if (ALEipConstants.MODE_UPDATE.equals(getMode())) {
        Expression exp1 = ExpressionFactory.matchExp(
            EipMPosition.POSITION_NAME_PROPERTY, position_name.getValue());
        query.setQualifier(exp1);
        Expression exp2 = ExpressionFactory.noMatchDbExp(
            EipMPosition.POSITION_ID_PK_COLUMN, Integer.valueOf(position_id));
        query.andQualifier(exp2);
      }

      if (dataContext.performQuery(query).size() != 0) {
        msgList.add("役職名『 <span class='em'>" + position_name
            + "</span> 』は既に登録されています。");
      }
    } catch (Exception ex) {
      logger.error("Exception", ex);
      return false;
    }

    return (msgList.size() == 0);
  }

  /**
   * 『役職』を読み込みます。 <BR>
   *
   * @param rundata
   * @param context
   * @param msgList
   * @return
   * @see com.aimluck.eip.common.ALAbstractFormData#loadFormData(org.apache.turbine.util.RunData,
   *      org.apache.velocity.context.Context, java.util.ArrayList)
   */
  protected boolean loadFormData(RunData rundata, Context context,
      List<String> msgList) {
    try {
      // オブジェクトモデルを取得
      EipMPosition record = AccountUtils.getEipMPosition(rundata, context);
      if (record == null)
        return false;
      position_name.setValue(record.getPositionName());
    } catch (Exception ex) {
      logger.error("Exception", ex);
      return false;
    }
    return true;
  }

  /**
   * 『役職』を追加します。 <BR>
   *
   * @param rundata
   * @param context
   * @param msgList
   * @return
   * @see com.aimluck.eip.common.ALAbstractFormData#insertFormData(org.apache.turbine.util.RunData,
   *      org.apache.velocity.context.Context, java.util.ArrayList)
   */
  protected boolean insertFormData(RunData rundata, Context context,
      List<String> msgList) {
    try {
      if (dataContext == null) {
        dataContext = DatabaseOrmService.getInstance().getDataContext();
      }

      EipMPosition position = (EipMPosition) dataContext
          .createAndRegisterNewObject(EipMPosition.class);
      position.setPositionName(position_name.getValue());
      Date now = new Date();
      position.setCreateDate(now);
      position.setUpdateDate(now);
      dataContext.commitChanges();

      position_id = position.getPositionId().intValue();

      ALEipManager.getInstance().reloadPosition();
    } catch (Exception ex) {
      dataContext.rollbackChanges();
      logger.error("Exception", ex);
      return false;
    }
    return true;
  }

  /**
   * 『役職』を更新します。 <BR>
   *
   * @param rundata
   * @param context
   * @param msgList
   * @return
   * @see com.aimluck.eip.common.ALAbstractFormData#updateFormData(org.apache.turbine.util.RunData,
   *      org.apache.velocity.context.Context, java.util.ArrayList)
   */
  protected boolean updateFormData(RunData rundata, Context context,
      List<String> msgList) {
    try {
      // オブジェクトモデルを取得
      EipMPosition record = AccountUtils.getEipMPosition(rundata, context);
      if (record == null)
        return false;
      record.setPositionName(position_name.getValue());
      record.setUpdateDate(new Date());
      dataContext.commitChanges();
      ALEipManager.getInstance().reloadPosition();
    } catch (Exception ex) {
      dataContext.rollbackChanges();
      logger.error("Exception", ex);
      return false;
    }
    return true;
  }

  /**
   * 『役職』を削除します。 <BR>
   *
   * @param rundata
   * @param context
   * @param msgList
   * @return
   * @see com.aimluck.eip.common.ALAbstractFormData#deleteFormData(org.apache.turbine.util.RunData,
   *      org.apache.velocity.context.Context, java.util.ArrayList)
   */
  protected boolean deleteFormData(RunData rundata, Context context,
      List<String> msgList) {
    try {
      // オブジェクトモデルを取得
      EipMPosition record = AccountUtils.getEipMPosition(rundata, context);
      if (record == null)
        return false;

      // 役職を削除
      dataContext.deleteObject(record);
      dataContext.commitChanges();

      // この役職に設定されているユーザーの役職IDを0とする
      String id = ALEipUtils
          .getTemp(rundata, context, ALEipConstants.ENTITY_ID);
      String sql = "UPDATE TURBINE_USER set POSITION_ID = 0 where POSITION_ID = "
          + id;
      @SuppressWarnings("deprecation")
      SQLTemplate rawSelect = new SQLTemplate(TurbineUser.class, sql, false);
      dataContext.performQuery(rawSelect);
      ALEipManager.getInstance().reloadPosition();
    } catch (Exception ex) {
      dataContext.rollbackChanges();
      logger.error("Exception", ex);
      return false;
    }
    return true;
  }

  /**
   * 『役職名』を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getPositionName() {
    return position_name;
  }

  /**
   *
   * @return
   */
  public int getPositionId() {
    return position_id;
  }
}
