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
package com.aimluck.eip.cabinet.beans;

import java.util.ArrayList;
import java.util.Hashtable;

import com.aimluck.commons.field.ALNumberField;
import com.aimluck.commons.field.ALStringField;
import com.aimluck.eip.cabinet.FolderInfo;
import com.aimluck.eip.common.ALData;

/**
 * 共有フォルダのフォルダ階層データを管理するクラスです。
 *
 */
public class CabinetBean implements ALData, Cloneable {

  /** <code>cabinet_id</code> フォルダまたはファイルID */
  private ALNumberField cabinet_id;

  /** <code>cabinet_name</code> フォルダまたはファイル名 */
  private ALStringField cabinet_name;

  /** <code>type</code> タイプ */
  private ALNumberField type;

  /** <code>cabinet_numberofitems</code> フォルダまたはファイル名が持つ要素の数 */
  // private ALNumberField cabinet_numberofitems;
  /** <code>cabinet_child</code> フォルダの中の要素 */
  private ArrayList<Hashtable> cabinet_child;

  /** items フォルダに属するファイル */
  // private ArrayList<CabinetBean> items;
  /*
   * @see com.aimluck.eip.common.ALData#initField()
   */
  public void initField() {
    cabinet_id = new ALNumberField();
    cabinet_name = new ALStringField();
    // cabinet_numberofitems = new ALNumberField();
    cabinet_child = new ArrayList();
    // items = new ArrayList();
    type = new ALNumberField();
  }

  public void setResultData(FolderInfo info) {
  }

  public int getcabinetid() {
    return (int) cabinet_id.getValue();
  }

  public void setCabinetId(int number) {
    cabinet_id.setValue(number);
  }

  public String getCabinetName() {
    return cabinet_name.toString();
  }

  public void setCabinetName(String str) {
    cabinet_name.setValue(str);
  }

  /*
   * public int getCabinetNumberofitems() { return (int)
   * cabinet_numberofitems.getValue(); }
   *
   * public void setCabinetNumberofitems(int number) {
   * cabinet_numberofitems.setValue(number); }
   */
  public ArrayList<Hashtable> getchildren() {
    return cabinet_child;
  }

  public void setCabinetChild(ArrayList<Hashtable> list) {
    cabinet_child = list;
  }

  /*
   * public ArrayList<CabinetBean> getItems() { return items; }
   *
   * public void setItems(ArrayList<CabinetBean> list) { items = list; }
   */

  public String gettype() {
    return type.toString();
  }

  public void settype(int i) {
    type.setValue(i);
  }
  @Override
  protected Object clone() throws CloneNotSupportedException {
    // TODO 自動生成されたメソッド・スタブ
    return super.clone();
  }
}
