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
package com.aimluck.eip.modules.screens;

import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import com.aimluck.eip.account.AccountEditFormData;
import com.aimluck.eip.account.AccountPasswdFormData;
import com.aimluck.eip.common.ALEipConstants;
import com.aimluck.eip.util.ALEipUtils;

/**
 * 個人設定・ユーザー情報を処理するクラスです。 <br />
 * 
 */
public class AccountPersonFormScreen extends ALVelocityScreen {
  /** logger */
  private static final JetspeedLogger logger = JetspeedLogFactoryService
      .getLogger(AccountPersonFormScreen.class.getName());

  /**
   * @see org.apache.turbine.modules.screens.RawScreen#doOutput(org.apache.turbine.util.RunData)
   */
  @Override
  protected void doOutput(RunData rundata, Context context) throws Exception {
    // MODEを取得
    String mode = rundata.getParameters().getString(ALEipConstants.MODE);
    try {

      if ("passwdform".equals(mode)) {
        doAccount_passwd_form(rundata, context);
      } else {
        doAccountperson_form(rundata, context);
      }
    } catch (Exception ex) {
      logger.error("[AccountUserFormScreen] Exception.", ex);
      ALEipUtils.redirectDBError(rundata);
    }
  }

  /**
   * 
   * @param rundata
   * @param context
   * @throws Exception
   */
  public void doAccountperson_form(RunData rundata, Context context)
      throws Exception {
    AccountEditFormData formData = new AccountEditFormData();
    formData.initField();
    formData.doViewForm(this, rundata, context);
    setTemplate(rundata, context,
        "portlets/html/ja/ajax-account-person-form.vm");
  }

  /**
   * 
   * @param rundata
   * @param context
   * @throws Exception
   */
  protected void doAccount_passwd_form(RunData rundata, Context context)
      throws Exception {
    AccountPasswdFormData formData = new AccountPasswdFormData();
    formData.initField();
    formData.doViewForm(this, rundata, context);

    String layout_template = "portlets/html/ja/ajax-account-person-passwd-form.vm";
    setTemplate(rundata, context, layout_template);
  }
}
