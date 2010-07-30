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
package com.aimluck.eip.common;

import java.io.File;
import java.util.List;

import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.upload.FileItem;
import org.apache.velocity.context.Context;

import com.aimluck.commons.field.ALStringField;
import com.aimluck.eip.modules.actions.common.ALAction;

/**
 * CSVデータを管理するための抽象クラスです。 <br />
 *
 */
public abstract class ALCsvAbstractUploadFormData extends ALAbstractFormData {

  private static final JetspeedLogger logger = JetspeedLogFactoryService
      .getLogger(ALCsvAbstractUploadFormData.class.getName());

  /** 添付ファイル名の最大文字数 */
  private final int FIELD_ATTACHMENT_MAX_LEN = 128;

  /** 受信した添付ファイル */
  private FileItem attachmentItem = null;

  /** 添付ファイル名 */
  private ALStringField attachmentName = null;

  /** データを分割表示する際の分割数 */
  protected int page_count;

  /** CSVファイルの行数 */
  protected int line_count;

  public void init(ALAction action, RunData rundata, Context context)
      throws ALPageNotFoundException, ALDBErrorException {
    super.init(action, rundata, context);
    page_count = 0;
    line_count = 0;
  }

  /**
   * 一時フォルダを指定します。 <br />
   *
   * @return
   */
  public abstract String getTempFilePath();

  /**
   * データに値を設定します。 <br />
   *
   * @param rundata
   * @param context
   * @param msgList
   *          エラーメッセージのリスト
   * @return TRUE 成功 FALSE 失敗
   */
  protected boolean setFormData(RunData rundata, Context context,
      List<String> msgList) throws ALPageNotFoundException, ALDBErrorException {

    // Itemの取得
    ParameterParser parser = rundata.getParameters();
    attachmentItem = parser.getFileItem("attachment");
    if (attachmentItem != null) {
      if (attachmentItem.getSize() > 0) {
        File file = new File(attachmentItem.getName());
        attachmentName.setValue(file.getName());
        return true;
      } else {
        msgList.add("サイズが 0KB のファイルを追加することはできません。");
        return false;
      }
    } else {
      msgList.add("ファイルが見つかりません。");
      return false;
    }

  }

  /**
   * @see com.aimluck.eip.common.ALAbstractFormData#setValidator()
   */
  protected void setValidator() {
    attachmentName.setNotNull(true);
    attachmentName.limitMaxLength(FIELD_ATTACHMENT_MAX_LEN);
    attachmentName.setCharacterType(ALStringField.TYPE_ALL);
  }

  /**
   * @see com.aimluck.eip.common.ALAbstractFormData#validate(java.util.ArrayList)
   */
  protected boolean validate(List<String> msgList) {
    attachmentName.validate(msgList);

    return (msgList.size() == 0);
  }

  /**
   * @see com.aimluck.eip.common.ALAbstractFormData#loadFormData(org.apache.turbine.util.RunData,
   *      org.apache.velocity.context.Context, java.util.ArrayList)
   */
  protected boolean loadFormData(RunData rundata, Context context,
      List<String> msgList) {
    return true;
  }

  /**
   * @see com.aimluck.eip.common.ALAbstractFormData#insertFormData(org.apache.turbine.util.RunData,
   *      org.apache.velocity.context.Context, java.util.ArrayList)
   */
  protected boolean insertFormData(RunData rundata, Context context,
      List<String> msgList) {
    return false;
  }

  /**
   * @see com.aimluck.eip.common.ALAbstractFormData#updateFormData(org.apache.turbine.util.RunData,
   *      org.apache.velocity.context.Context, java.util.ArrayList)
   */
  protected boolean updateFormData(RunData rundata, Context context,
      List<String> msgList) {

    try {
      String filepath = getTempFilePath();
      File file = new File(filepath);
      if (file.exists()) {
        file.delete();
      }
      file.createNewFile();
      attachmentItem.write(file.getAbsolutePath());

      ALCsvTokenizer reader = new ALCsvTokenizer();
      if (!reader.init(filepath)) {
        return false;
      }
      /*
      int i;
      String token;
      try {
        page_count = 0;
        line_count = 0;
        while (reader.eof != -1) {
          // if(j++ > 100)break;
          for (i = 0; i < ALCsvTokenizer.CSV_SHOW_SIZE; i++) {
            while (reader.eof != -1) {
              token = reader.nextToken();
              if (reader.eof == -1)
                break;
              if (reader.line)
                break;
            }
            if (reader.eof == -1)
              break;
            line_count++;
          }
          page_count++;
        }
      } catch (Exception e) {

      }
      */

    } catch (Exception ex) {
      logger.error("Exception", ex);
      return false;
    }

    return true;
  }

  /**
   * @see com.aimluck.eip.common.ALAbstractFormData#deleteFormData(org.apache.turbine.util.RunData,
   *      org.apache.velocity.context.Context, java.util.ArrayList)
   */
  protected boolean deleteFormData(RunData rundata, Context context,
      List<String> msgList) {
    try {
      File file = new File(getTempFilePath());
      if (file.exists()) {
        file.delete();
      }
    } catch (Exception ex) {
      logger.error("Exception", ex);
      return false;
    }

    return true;
  }

  /**
   * @see com.aimluck.eip.common.ALData#initField()
   */
  public void initField() {
    attachmentName = new ALStringField();
    attachmentName.setFieldName("CSVファイル名");
    attachmentName.setTrim(true);
  }

  public int getPageCount() {
    return page_count;
  }

  public int getLineCount() {
    return line_count;
  }
}
