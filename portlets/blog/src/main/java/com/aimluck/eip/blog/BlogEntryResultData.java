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
package com.aimluck.eip.blog;

import java.util.ArrayList;
import java.util.Date;

import com.aimluck.commons.field.ALDateTimeField;
import com.aimluck.commons.field.ALNumberField;
import com.aimluck.commons.field.ALStringField;
import com.aimluck.eip.common.ALData;
import com.aimluck.eip.util.ALCommonUtils;
import com.aimluck.eip.util.ALEipUtils;

/**
 * ブログエントリーのResultDataです。 <BR>
 * 
 */
public class BlogEntryResultData implements ALData {

  /** Entry ID */
  private ALNumberField entry_id;

  /** Owner ID */
  private ALNumberField owner_id;

  /** Owner 名 */
  private ALStringField owner_name;

  /** Title */
  private ALStringField title;

  /** メモ */
  private ALStringField note;

  /** ブログ ID */
  private ALNumberField blog_id;

  /** カテゴリID */
  private ALNumberField thema_id;

  /** カテゴリ名 */
  private ALStringField thema_name;

  /** コメント付加フラグ */
  private boolean allow_comments;

  /** タイトル月日 */
  private ALStringField title_date;

  /** 登録日 */
  private ALStringField create_date;
  
  /** 登録日(DATA型) */
  private ALDateTimeField create_date_alternative;

  /** 更新日 */
  private ALDateTimeField update_date;

  /** 添付ファイルリスト */
  private ArrayList attachmentFileList = null;

  /** コメント数 */
  private int comments_num;
  
  /** 日付（アンカーリンク用） */
  private int day;

  /**
   * 
   * @see com.aimluck.eip.common.ALData#initField()
   */
  public void initField() {
    entry_id = new ALNumberField();
    owner_id = new ALNumberField();
    owner_name = new ALStringField();
    title = new ALStringField();
    note = new ALStringField();
    blog_id = new ALNumberField();
    thema_id = new ALNumberField();
    thema_name = new ALStringField();
    allow_comments = true;
    title_date = new ALStringField();
    create_date = new ALStringField();
    create_date_alternative = new ALDateTimeField();
    update_date = new ALDateTimeField();
    attachmentFileList = new ArrayList();

    day = 0;
    comments_num = 0;
  }

  /**
   * @return
   */
  public ALNumberField getThemaId() {
    return thema_id;
  }

  /**
   * @return
   */
  public String getThemaName() {
    return ALCommonUtils.replaceToAutoCR(thema_name.toString());
  }

  /**
   * @return
   */
  public ALNumberField getEntryId() {
    return entry_id;
  }

  /**
   * @return
   */
  public String getTitle() {
    return ALCommonUtils.replaceToAutoCR(title.toString());
  }

  /**
   * @param i
   */
  public void setThemaId(long i) {
    thema_id.setValue(i);
  }

  /**
   * @param string
   */
  public void setThemaName(String string) {
    thema_name.setValue(string);
  }

  /**
   * @param i
   */
  public void setEntryId(long i) {
    entry_id.setValue(i);
  }

  /**
   * @param string
   */
  public void setTitle(String string) {
    title.setValue(string);
  }

  /**
   * @return
   */
  public String getNote() {
    return ALEipUtils.getMessageList(note.getValue());
  }

  /**
   * コメント付加フラグ．
   * 
   * @return
   */
  public boolean allowComments() {
    return allow_comments;
  }

  /**
   * @param string
   */
  public void setNote(String string) {
    note.setValue(string);
  }

  /**
   * @return
   */
  public void setAllowComments(boolean bool) {
    allow_comments = bool;
  }

  /**
   * @return
   */
  public ALStringField getTitleDate() {
    return title_date;
  }

  /**
   * @param string
   */
  public void setTitleDate(String str) {
    title_date.setValue(str);
  }

  /**
   * @return
   */
  public ALStringField getCreateDate() {
    return create_date;
  }
  
  /**
   * @return
   */
  public ALDateTimeField getCreateDateAlternative() {
    return create_date_alternative;
  }

  /**
   * @return
   */
  public ALDateTimeField getUpdateDate() {
    return update_date;
  }

  /**
   * @param string
   */
  public void setCreateDate(String string) {
    create_date.setValue(string);
  }
  
  /**
   * @param string
   */
  public void setCreateDateAlternative(Date date) {
    create_date_alternative.setValue(date);
  }

  /**
   * @param string
   */
  public void setUpdateDate(Date date) {
    update_date.setValue(date);
  }

  /**
   * @return
   */
  public ALNumberField getOwnerId() {
    return owner_id;
  }

  /**
   * @param i
   */
  public void setOwnerId(long i) {
    owner_id.setValue(i);
  }

  /**
   * @return
   */
  public ALStringField getOwnerName() {
    return owner_name;
  }

  /**
   * @param i
   */
  public void setOwnerName(String string) {
    owner_name.setValue(string);
  }

  /**
   * @return
   */
  public ALNumberField getBlogId() {
    return blog_id;
  }

  /**
   * @param i
   */
  public void setBlogId(long i) {
    blog_id.setValue(i);
  }

  public ArrayList getAttachmentFileList() {
    return attachmentFileList;
  }

  public void setAttachmentFiles(ArrayList list) {
    attachmentFileList = list;
  }

  public int getCommentsNum() {
    return comments_num;
  }

  public void setCommentsNum(int i) {
    if (i < 0) {
      comments_num = 0;
    } else {
      comments_num = i;
    }
  }
  
  public int getDay() {
    return day;
  }
  
  public void setDay(int i) {
    day = i;
  }


}
