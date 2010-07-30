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
package com.aimluck.eip.modules.parameters;

import java.util.List;

import org.apache.jetspeed.modules.parameters.ListBox;
import org.apache.turbine.util.RunData;

import com.aimluck.eip.common.ALEipUser;
import com.aimluck.eip.util.ALEipUtils;

/**
 * ログインユーザーの一覧をListBoxで返すクラスです。 <br />
 *
 */
public class LoginUserListBox extends ListBox {

  public static final String INITIAL_VALUE = "initialvalue";

  private String DEF_INITIAL_VALUE = "（ユーザー選択）";

  /**
   * 表示オプションを初期化します。
   *
   * @param data
   */
  protected void init(RunData data) {

    // ログインユーザの取得
    List<ALEipUser> list = ALEipUtils.getUsers("LoginUser");

    ALEipUser user = null;
    int length = list.size();
    String[] groupKeys = new String[length + 1];
    String[] groupValues = new String[length + 1];

    groupKeys[0] = "";
    groupValues[0] = (String) this.getParm(INITIAL_VALUE, DEF_INITIAL_VALUE);
    for (int i = 0; i < length; i++) {
      user = list.get(i);
      groupKeys[i + 1] = user.getUserId().getValueAsString();
      groupValues[i + 1] = user.getAliasName().getValue();
    }

    this.layout = (String) this.getParm(LAYOUT, LAYOUT_COMBO);
    this.items = groupKeys;
    this.values = groupValues;
    this.size = Integer.toString(length + 1);
    this.multiple = Boolean.valueOf(
        (String) this.getParm(MULTIPLE_CHOICE, "false")).booleanValue();
  }

}
