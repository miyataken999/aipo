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

import com.aimluck.eip.addressbook.AddressBookFormData;
import com.aimluck.eip.util.ALEipUtils;

/**
 * アドレス帳を処理するクラスです。 <br />
 */
public class AddressBookFormScreen extends ALVelocityScreen {
  /** logger */
  private static final JetspeedLogger logger = JetspeedLogFactoryService
      .getLogger(AddressBookFormScreen.class.getName());

  /**
   * @see org.apache.turbine.modules.screens.RawScreen#doOutput(org.apache.turbine.util.RunData)
   */
  protected void doOutput(RunData rundata, Context context) throws Exception {

    try {
      doAddressBook_form(rundata, context);      
    } catch (Exception ex) {
      // ここに到達する場合はバグまたは不具合の可能性アリ
      logger.error("[AddressBookFormScreen] Exception.", ex);
      ALEipUtils.redirectDBError(rundata);
    }
  }

  protected void doAddressBook_form(RunData rundata, Context context) {
    AddressBookFormData formData = new AddressBookFormData();
    formData.initField();
    formData.doViewForm(this, rundata, context);

    formData.loadGroupList(rundata, context);
    formData.loadCompanyList(rundata, context);
    formData.loadGroups(rundata, context);
    
    String layout_template = "portlets/html/ja/ajax-addressbook-form.vm";
    setTemplate(rundata, context, layout_template);
  }
}
