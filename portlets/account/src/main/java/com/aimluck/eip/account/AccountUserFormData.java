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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.cayenne.ObjectId;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectQuery;
import org.apache.jetspeed.om.security.JetspeedUser;
import org.apache.jetspeed.om.security.UserNamePrincipal;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.jetspeed.services.PsmlManager;
import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.jetspeed.services.resources.JetspeedResources;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import com.aimluck.commons.field.ALNumberField;
import com.aimluck.commons.field.ALStringField;
import com.aimluck.commons.utils.ALStringUtil;
import com.aimluck.eip.account.util.AccountUtils;
import com.aimluck.eip.cayenne.om.account.EipMUserPosition;
import com.aimluck.eip.cayenne.om.portlet.EipTBlog;
import com.aimluck.eip.cayenne.om.portlet.EipTBlogFootmarkMap;
import com.aimluck.eip.cayenne.om.portlet.EipTTodo;
import com.aimluck.eip.cayenne.om.portlet.EipTTodoCategory;
import com.aimluck.eip.cayenne.om.security.TurbineUser;
import com.aimluck.eip.cayenne.om.security.TurbineUserGroupRole;
import com.aimluck.eip.common.ALAbstractFormData;
import com.aimluck.eip.common.ALBaseUser;
import com.aimluck.eip.common.ALDBErrorException;
import com.aimluck.eip.common.ALEipConstants;
import com.aimluck.eip.common.ALEipManager;
import com.aimluck.eip.common.ALEipPosition;
import com.aimluck.eip.common.ALEipPost;
import com.aimluck.eip.common.ALPageNotFoundException;
import com.aimluck.eip.fileupload.beans.FileuploadLiteBean;
import com.aimluck.eip.fileupload.util.FileuploadUtils;
import com.aimluck.eip.modules.actions.common.ALAction;
import com.aimluck.eip.orm.DatabaseOrmService;
import com.aimluck.eip.services.accessctl.ALAccessControlFactoryService;
import com.aimluck.eip.services.accessctl.ALAccessControlHandler;
import com.aimluck.eip.services.datasync.ALDataSyncFactoryService;
import com.aimluck.eip.user.beans.UserGroupLiteBean;
import com.aimluck.eip.user.util.UserUtils;
import com.aimluck.eip.util.ALEipUtils;

/**
 * ユーザーアカウントのフォームデータを管理するクラスです。 <BR>
 *
 */
public class AccountUserFormData extends ALAbstractFormData {

  /** logger */
  private static final JetspeedLogger logger = JetspeedLogFactoryService
      .getLogger(AccountUserFormData.class.getName());

  /** ブラウザに表示するデフォルトのパスワード（ダミーパスワード） */
  private static final String DEFAULT_VIEW_PASSWORD = "******";

  /** ユーザー名 */
  private ALStringField username;

  /** パスワード */
  private ALStringField password;

  /** パスワード */
  private ALStringField password2;

  /** 名前（名） */
  private ALStringField firstname;

  /** 名前（姓） */
  private ALStringField lastname;

  /** メールアドレス */
  private ALStringField email;

  /** アカウント有効/無効 */
  private ALStringField disabled;

  /** 電話番号（内線） */
  private ALStringField in_telephone;

  /** 電話番号 */
  private ALStringField out_telephone1;

  /** 電話番号 */
  private ALStringField out_telephone2;

  /** 電話番号 */
  private ALStringField out_telephone3;

  /** 電話番号（携帯） */
  private ALStringField cellular_phone1;

  /** 電話番号（携帯） */
  private ALStringField cellular_phone2;

  /** 電話番号（携帯） */
  private ALStringField cellular_phone3;

  /** 携帯メールアドレス */
  private ALStringField cellular_mail;

  /** 会社 ID */
  private ALNumberField company_id;

  /** 役職 ID */
  private ALNumberField position_id;

  /** 部署 ID */
  private ALNumberField post_id;

  /** フリガナ（名） */
  private ALStringField first_name_kana;

  /** フリガナ（姓） */
  private ALStringField last_name_kana;

  /** 顔写真 */
  private ALStringField photo = null;

  /** 添付ファイル */
  private FileuploadLiteBean filebean = null;

  /** 添付フォルダ名 */
  private String folderName = null;

  /** 部署リスト */
  private List<UserGroupLiteBean> postList;

  /** 役職リスト */
  private List<ALEipPosition> positionList;

  /** 部署 */
  private AccountPostFormData post;

  /** 役職 */
  private AccountPositionFormData position;

  /** */
  private boolean is_new_post;

  /** */
  private boolean is_new_position;

  /** パスワード変更の可否．変更する場合は，false． */
  private boolean dontUpdatePasswd = false;

  private String org_id;

  private DataContext dataContext;

  /** 顔写真データ */
  private byte[] facePhoto;

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

    folderName = rundata.getParameters().getString("folderName");

    is_new_post = rundata.getParameters().getBoolean("is_new_post");
    is_new_position = rundata.getParameters().getBoolean("is_new_position");

    org_id = DatabaseOrmService.getInstance().getOrgId(rundata);

    dataContext = DatabaseOrmService.getInstance().getDataContext();
  }

  /**
   * 各フィールドを初期化します。 <BR>
   *
   * @see com.aimluck.eip.common.ALData#initField()
   */
  public void initField() {
    // ログイン名
    username = new ALStringField();
    username.setFieldName("ログイン名");
    username.setTrim(true);
    // パスワード
    password = new ALStringField();
    password.setFieldName("パスワード");
    password.setTrim(true);
    // パスワード2
    password2 = new ALStringField();
    password2.setFieldName("パスワード（確認用）");
    password2.setTrim(true);
    // 名
    firstname = new ALStringField();
    firstname.setFieldName("名前（名）");
    firstname.setTrim(true);
    // 姓
    lastname = new ALStringField();
    lastname.setFieldName("名前（姓）");
    lastname.setTrim(true);
    // メールアドレス
    email = new ALStringField();
    email.setFieldName("メールアドレス");
    email.setTrim(true);
    // アカウント有効/無効
    disabled = new ALStringField();
    disabled.setFieldName("アカウント有効/無効");
    disabled.setTrim(true);
    // 内線番号
    in_telephone = new ALStringField();
    in_telephone.setFieldName("電話番号（内線）");
    in_telephone.setTrim(true);

    // 外線番号
    out_telephone1 = new ALStringField();
    out_telephone1.setFieldName("電話番号（外線）");
    out_telephone1.setTrim(true);
    out_telephone2 = new ALStringField();
    out_telephone2.setFieldName("電話番号（外線）");
    out_telephone2.setTrim(true);
    out_telephone3 = new ALStringField();
    out_telephone3.setFieldName("電話番号（外線）");
    out_telephone3.setTrim(true);

    // 携帯番号
    cellular_phone1 = new ALStringField();
    cellular_phone1.setFieldName("電話番号（携帯）");
    cellular_phone1.setTrim(true);
    cellular_phone2 = new ALStringField();
    cellular_phone2.setFieldName("電話番号（携帯）");
    cellular_phone2.setTrim(true);
    cellular_phone3 = new ALStringField();
    cellular_phone3.setFieldName("電話番号（携帯）");
    cellular_phone3.setTrim(true);

    // 携帯アドレス
    cellular_mail = new ALStringField();
    cellular_mail.setFieldName("携帯メールアドレス");
    cellular_mail.setTrim(true);
    // 名（フリガナ）
    first_name_kana = new ALStringField();
    first_name_kana.setFieldName("名前（フリガナ）");
    first_name_kana.setTrim(true);
    // 姓（フリガナ）
    last_name_kana = new ALStringField();
    last_name_kana.setFieldName("名前（フリガナ）");
    last_name_kana.setTrim(true);
    // 顔写真
    photo = new ALStringField();
    photo.setFieldName("顔写真");
    photo.setTrim(true);

    // 部署ID
    post_id = new ALNumberField();
    post_id.setFieldName("部署");

    // 役職ID
    position_id = new ALNumberField();
    position_id.setFieldName("役職");

    post = new AccountPostFormData();
    post.setJoinMember(false);
    post.initField();

    position = new AccountPositionFormData();
    position.initField();
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
    try {
      if (res) {
        post.setFormData(rundata, context, msgList);
        position.setFormData(rundata, context, msgList);

        List<FileuploadLiteBean> fileBeanList = FileuploadUtils.getFileuploadList(rundata);
        if (fileBeanList != null && fileBeanList.size() > 0) {
          filebean = fileBeanList.get(0);
          if (filebean.getFileId() > 0) {
            // 顔写真をセットする．
            String[] acceptExts = ImageIO.getWriterFormatNames();
            facePhoto = FileuploadUtils.getBytesShrinkFilebean(org_id,
                folderName, ALEipUtils.getUserId(rundata), filebean,
                acceptExts, FileuploadUtils.DEF_THUMBNAIL_WIDTH,
                FileuploadUtils.DEF_THUMBNAIL_HEIGTH, msgList);
          } else {
            facePhoto = null;
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Exception", ex);
      res = false;
    }
    return res;
  }

  /**
   * 各フィールドに対する制約条件を設定します。 <BR>
   *
   * @see com.aimluck.eip.common.ALAbstractFormData#setValidator()
   */
  protected void setValidator() {
    // ユーザー名
    username.setNotNull(true);
    username.setCharacterType(ALStringField.TYPE_ASCII);
    username.limitMaxLength(16);
    // パスワード
    password.setNotNull(true);
    password.setCharacterType(ALStringField.TYPE_ALPHABET_NUMBER);
    password.limitMaxLength(16);
    // パスワード2
    password2.setNotNull(true);
    password2.setCharacterType(ALStringField.TYPE_ALPHABET_NUMBER);
    password2.limitMaxLength(16);
    // 名
    firstname.setNotNull(true);
    firstname.limitMaxLength(20);
    // 姓
    lastname.setNotNull(true);
    lastname.limitMaxLength(20);

    // 名（フリガナ）
    first_name_kana.setNotNull(true);
    first_name_kana.limitMaxLength(20);
    // 姓（フリガナ）
    last_name_kana.setNotNull(true);
    last_name_kana.limitMaxLength(20);

    // 内線
    // in_telephone.setCharacterType(ALStringField.TYPE_ALPHABET_NUMBER);
    in_telephone.setCharacterType(ALStringField.TYPE_ASCII);
    in_telephone.limitMaxLength(13);
    // メールアドレス
    email.setCharacterType(ALStringField.TYPE_ASCII);
    email.limitMaxLength(50);

    // 外線
    out_telephone1.setCharacterType(ALStringField.TYPE_NUMBER);
    out_telephone1.limitMaxLength(5);
    out_telephone2.setCharacterType(ALStringField.TYPE_NUMBER);
    out_telephone2.limitMaxLength(4);
    out_telephone3.setCharacterType(ALStringField.TYPE_NUMBER);
    out_telephone3.limitMaxLength(4);

    // 携帯
    cellular_phone1.setCharacterType(ALStringField.TYPE_NUMBER);
    cellular_phone1.limitMaxLength(5);
    cellular_phone2.setCharacterType(ALStringField.TYPE_NUMBER);
    cellular_phone2.limitMaxLength(4);
    cellular_phone3.setCharacterType(ALStringField.TYPE_NUMBER);
    cellular_phone3.limitMaxLength(4);
    // 携帯メール
    cellular_mail.setCharacterType(ALStringField.TYPE_ASCII);

    post.setValidator();
    position.setValidator();
  }

  /**
   * フォームに入力されたデータの妥当性検証を行います。 <BR>
   *
   * @param msgList
   * @return
   * @see com.aimluck.eip.common.ALAbstractFormData#validate(java.util.ArrayList)
   */
  protected boolean validate(List<String> msgList) {
    ArrayList<String> dummy = new ArrayList<String>();
    username.validate(msgList);
    if (ALEipConstants.MODE_INSERT.equals(getMode())) {
      try {
        Expression exp = ExpressionFactory.matchExp(
            TurbineUser.LOGIN_NAME_PROPERTY, username.getValue());
        SelectQuery query = new SelectQuery(TurbineUser.class, exp);
        List<?> ulist = dataContext.performQuery(query);
        if (ulist.size() > 0) {
          //TurbineUser user = (TurbineUser) ulist.get(0);
          // if ("F".equals(user.getDisabled())) {
          msgList.add("ログイン名『 <span class='em'>" + username
              + "</span> 』はすでに登録されています。別のログイン名で登録してください。");
          // } else {
          // msgList.add("ログイン名『 <span class='em'>" + username
          // + "</span> 』はすでに削除されています。一旦削除したログイン名では登録できません。");
          // }
        }
      } catch (Exception ex) {
        logger.error("Exception", ex);
        return false;
      }
    }

    String unameValue = username.getValue();
    int length = unameValue.length();
    for (int i1 = 0; i1 < length; i1++) {
      if (isSymbol(unameValue.charAt(i1))) {
        // 使用されているのが妥当な記号であるかの確認
        if (!(unameValue.charAt(i1) == "_".charAt(0)
            || unameValue.charAt(i1) == "-".charAt(0) || unameValue.charAt(i1) == "."
            .charAt(0))) {
          msgList
              .add("『 <span class='em'>ログイン名</span> 』に使用できる記号は「-」「.」「_」のみです。");
          break;
        }
      }
    }

    // ユーザー名の先頭にdummy_が含まれるかの確認
    if (ALEipConstants.MODE_INSERT.equals(getMode())) {
      if (username.getValue().length() > 5) {
        if (ALEipUtils.dummy_user_head.equals((username.getValue()).substring(
            0, 6))) {
          msgList.add("ログイン名の先頭に『 <span class='em'>"
              + ALEipUtils.dummy_user_head
              + "</span> 』は使用出来ません。別のログイン名で登録してください。");
        }
      }
    }

    // パスワードの確認
    if (ALEipConstants.MODE_INSERT.equals(getMode())) {
      if (!password.getValue().equals(password2.getValue())) {
        msgList
            .add("『 <span class='em'>パスワード</span> 』と『 <span class='em'>パスワード（確認用）</span> 』を正しく入力してください。");
      } else {
        password.validate(msgList);
        password2.validate(msgList);
      }
    } else if (ALEipConstants.MODE_UPDATE.equals(getMode())) {
      if (password.getValue().equals(DEFAULT_VIEW_PASSWORD)
          && password2.getValue().equals(DEFAULT_VIEW_PASSWORD)) {
        dontUpdatePasswd = true;
      } else {
        if (!password.getValue().equals(password2.getValue())) {
          msgList
              .add("『 <span class='em'>パスワード</span> 』と『 <span class='em'>パスワード（確認用）</span> 』を正しく入力してください。");
        } else {
          password.validate(msgList);
          password2.validate(msgList);
        }
      }
    }

    firstname.validate(msgList);
    lastname.validate(msgList);

    // フリガナのカタカナへの変換
    first_name_kana.setValue(ALStringUtil.convertHiragana2Katakana(ALStringUtil
        .convertH2ZKana(first_name_kana.toString())));
    last_name_kana.setValue(ALStringUtil.convertHiragana2Katakana(ALStringUtil
        .convertH2ZKana(last_name_kana.toString())));
    first_name_kana.validate(msgList);
    last_name_kana.validate(msgList);

    // メールアドレス
    email.validate(msgList);
    if (email.getValue() != null && email.getValue().trim().length() > 0
        && !ALStringUtil.isCellPhoneMailAddress(email.getValue())) {
      msgList.add("『 <span class='em'> メールアドレス </span>』を正しく入力してください。");
    }

    if (!out_telephone1.getValue().equals("")
        || !out_telephone2.getValue().equals("")
        || !out_telephone3.getValue().equals("")) {

      if (!out_telephone1.validate(dummy) || !out_telephone2.validate(dummy)
          || !out_telephone3.validate(dummy)) {
        msgList.add("『 <span class='em'>電話番号（外線）</span> 』を正しく入力してください。");
      } else {
        // 電話番号の長さチェック
        int req_size = out_telephone1.getValue().length()
            + out_telephone2.getValue().length()
            + out_telephone3.getValue().length();
        int limit_size = out_telephone1.getMaxLength()
            + out_telephone2.getMaxLength() + out_telephone3.getMaxLength();
        if (req_size > limit_size) {
          msgList.add("『 <span class='em'>電話番号（外線）</span> 』を" + limit_size
              + "桁以内で正しく入力してください。");
        }
      }
    }

    in_telephone.validate(msgList);
    // ハイフン以外の記号とアルファベットの入力をはじきます
    Pattern pattern = Pattern.compile(".*[^-0-9]+.*");
    Matcher matcher = pattern.matcher(in_telephone.getValue());
    Boolean ext_validater = matcher.matches();
    if (ext_validater) {
      msgList.add("電話番号（内線）は 15 文字以下でハイフン（-）または半角数字で入力してください。");
    }

    if (!cellular_phone1.getValue().equals("")
        || !cellular_phone2.getValue().equals("")
        || !cellular_phone3.getValue().equals("")) {
      if (!cellular_phone1.validate(dummy) || !cellular_phone2.validate(dummy)
          || !cellular_phone3.validate(dummy)) {
        msgList.add("『 <span class='em'>電話番号（携帯）</span> 』を正しく入力してください。");
      }
    }

    // 携帯メールアドレス
    cellular_mail.validate(msgList);
    if (cellular_mail.getValue().trim().length() > 0
        && !ALStringUtil.isCellPhoneMailAddress(cellular_mail.getValue())) {
      msgList.add("『 <span class='em'> 携帯メールアドレス </span>』を正しく入力してください。");
    }

    // 顔写真
    if (filebean != null && filebean.getFileId() > 0 && facePhoto == null) {
      msgList.add("『 <span class='em'>顔写真</span> 』にはJpeg画像を指定してください。");
    }

    if (is_new_post) {
      post.setMode(ALEipConstants.MODE_INSERT);
      post.validate(msgList);
    }
    if (is_new_position) {
      position.setMode(ALEipConstants.MODE_INSERT);
      position.validate(msgList);
    }

    return (msgList.size() == 0);
  }

  /**
   * 『ユーザー』を読み込みます。 <BR>
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
      ALBaseUser user = AccountUtils.getBaseUser(rundata, context);
      if (user == null)
        return false;
      // ユーザー名
      username.setValue(user.getUserName());
      // パスワード
      password.setValue(DEFAULT_VIEW_PASSWORD);
      // パスワード2
      password2.setValue(DEFAULT_VIEW_PASSWORD);
      // 名前（名）
      firstname.setValue(user.getFirstName());
      // 名前（姓）
      lastname.setValue(user.getLastName());
      // メールアドレス
      email.setValue(user.getEmail());
      // 電話番号（内線）
      in_telephone.setValue(user.getInTelephone());
      // 電話番号（外線）
      StringTokenizer token;
      if (user.getOutTelephone() != null) {
        token = new StringTokenizer(user.getOutTelephone(), "-");
        if (token.countTokens() == 3) {
          out_telephone1.setValue(token.nextToken());
          out_telephone2.setValue(token.nextToken());
          out_telephone3.setValue(token.nextToken());
        }
      } // 電話番号（携帯）
      if (user.getCellularPhone() != null) {
        token = new StringTokenizer(user.getCellularPhone(), "-");
        if (token.countTokens() == 3) {
          cellular_phone1.setValue(token.nextToken());
          cellular_phone2.setValue(token.nextToken());
          cellular_phone3.setValue(token.nextToken());
        }
      } // 携帯メールアドレス
      cellular_mail.setValue(user.getCellularMail());
      // 会社ID
      // company_id.setValue(user.getCompanyId());
      // 役職ID
      position_id.setValue(user.getPositionId());
      // フリガナ（名）
      first_name_kana.setValue(user.getFirstNameKana());
      // フリガナ（姓）
      last_name_kana.setValue(user.getLastNameKana());

      if (user.getPhoto() != null) {
        filebean = new FileuploadLiteBean();
        filebean.initField();
        filebean.setFolderName("");
        filebean.setFileId(0);
        filebean.setFileName("以前の写真ファイル");
      }

      postList = AccountUtils
          .getPostBeanList(Integer.parseInt(user.getUserId()));

      return true;
    } catch (Exception e) {
      logger.error("Exception", e);
      return false;
    }
  }

  /**
   * 『ユーザー』を追加します。 <BR>
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

    boolean res = true;
    try {

      int user_num = ALEipUtils.getCurrentUserNum(rundata);
      int max_user = ALEipUtils.getLimitUsers();
      if ((max_user > 0) && (user_num + 1 > max_user)) {
        msgList.add("ユーザー数が利用制限を超えています。");
        return false;
      }

      // WebAPIのDBへ接続できるか確認
      if (!ALDataSyncFactoryService.getInstance().getDataSyncHandler()
          .checkConnect()) {
        msgList.add("コントロールパネルWebAPIのデータベースの接続に失敗したため、処理は実行されませんでした。");
        return false;
      }

      // if (is_new_post) {
      // // 部署登録も同時に行う場合
      // res = post.insertFormData(rundata, context, msgList);
      // if (res)
      // post_id.setValue(post.getPostId());
      // }
      if (is_new_position && res) {
        // 役職登録も同時に行う場合
        res = position.insertFormData(rundata, context, msgList);
        if (res)
          position_id.setValue(position.getPositionId());
      }
      if (res) { // オブジェクトモデルを生成
        ALBaseUser user = (ALBaseUser) JetspeedSecurity.getUserInstance();
        rundata.getParameters().setProperties(user);
        // ユーザー名
        user.setUserName(JetspeedSecurity.convertUserName(username.getValue()));
        Date now = new Date();
        // 作成日
        // 以下のメソッドは動作しないため、ALBaseUserにてオーバーライド
        // user.setCreateDate(now);
        user.setCreated(now);
        user.setModified(now);
        user.setLastLogin(now);
        user.setCreatedUserId(ALEipUtils.getUserId(rundata));
        user.setUpdatedUserId(ALEipUtils.getUserId(rundata));
        user.setConfirmed(JetspeedResources.CONFIRM_VALUE);
        // user.setDisabled(disabled.getValue());
        user.setDisabled("F");
        user.setPassword(password.getValue());
        user.setPasswordChanged(new Date());
        user.setInTelephone(in_telephone.getValue());
        if (!out_telephone1.getValue().equals("")
            && !out_telephone2.getValue().equals("")
            && !out_telephone3.getValue().equals("")) {
          user.setOutTelephone(new StringBuffer()
              .append(out_telephone1.getValue()).append("-")
              .append(out_telephone2.getValue()).append("-")
              .append(out_telephone3.getValue()).toString());
        } else {
          user.setOutTelephone("");
        }

        if (!cellular_phone1.getValue().equals("")
            && !cellular_phone2.getValue().equals("")
            && !cellular_phone3.getValue().equals("")) {
          user.setCellularPhone(new StringBuffer()
              .append(cellular_phone1.getValue()).append("-")
              .append(cellular_phone2.getValue()).append("-")
              .append(cellular_phone3.getValue()).toString());
        } else {
          user.setCellularPhone("");
        }
        user.setCellularMail(cellular_mail.getValue());
        user.setCompanyId(1);
        user.setPositionId((int) position_id.getValue());
        user.setPostId((int) post_id.getValue());
        user.setFirstNameKana(first_name_kana.getValue());
        user.setLastNameKana(last_name_kana.getValue());
        if (filebean != null && filebean.getFileId() > 0) {
          // 顔写真を登録する．
          user.setPhoto(facePhoto);
        }

        // ユーザーを追加
        JetspeedSecurity.addUser(user);
        logger.debug("JOIN GROUP:" + "LoginUser");

        // ユーザーをグループに追加。
        String[] groups = rundata.getParameters().getStrings("group_to");
        if (groups != null && groups.length != 0) {
          for (int i = 0; i < groups.length; i++) {
            JetspeedSecurity.joinGroup(user.getUserName(), groups[i]);
          }
        }

        // 初期メールアカウントの作成
        // if (email.getValue() != null && (!email.getValue().equals(""))) {
        // ALMailUtils.insertMailAccountData(rundata, msgList, Integer
        // .parseInt(user.getUserId()), "初期アカウント",
        // ALMailUtils.ACCOUNT_TYPE_INIT, email.getValue(), "未設定", "未設定",
        // 25, "未設定", 110, "未設定", "未設定", ALSmtpMailSender.AUTH_SEND_NONE,
        // null, null, ALPop3MailReceiver.AUTH_RECEIVE_NORMAL, 0, 0, 1, "0");
        // }

        // アクセス権限
        ALAccessControlFactoryService aclservice = (ALAccessControlFactoryService) ((TurbineServices) TurbineServices
            .getInstance())
            .getService(ALAccessControlFactoryService.SERVICE_NAME);
        ALAccessControlHandler aclhandler = aclservice
            .getAccessControlHandler();
        aclhandler.insertDefaultRole(Integer.parseInt(user.getUserId()));

        dataContext.commitChanges();

        /** ユーザリストのキャッシュをクリアする */
        UserUtils.clearCache();

        // WebAPIとのDB同期
        if (!ALDataSyncFactoryService.getInstance().getDataSyncHandler()
            .addUser(user)) {
          return false;
        }

      }

      // 一時的な添付ファイルの削除
      File folder = FileuploadUtils.getFolder(org_id,
          ALEipUtils.getUserId(rundata), folderName);
      FileuploadUtils.deleteFolder(folder);
    } catch (Exception e) {
      logger.error("Exception", e);
      res = false;
    }
    return res;
  }

  /**
   * 『ユーザー』を更新します。 <BR>
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
    boolean res = true;
    try {
      // WebAPIのDBへ接続できるか確認
      if (!ALDataSyncFactoryService.getInstance().getDataSyncHandler()
          .checkConnect()) {
        msgList.add("コントロールパネルWebAPIのデータベースの接続に失敗したため、処理は実行されませんでした。");
        return false;
      }
      if (is_new_post) {
        // 部署登録も同時に行う場合
        res = post.insertFormData(rundata, context, msgList);
        if (res)
          post_id.setValue(post.getPostId());
      }
      if (is_new_position && res) {
        // 役職登録も同時に行う場合
        res = position.insertFormData(rundata, context, msgList);
        if (res)
          position_id.setValue(position.getPositionId());
      }
      if (res) {
        ALBaseUser user = AccountUtils.getBaseUser(rundata, context);
        if (user == null) {
          return false;
        }
        String oldDisabled = user.getDisabled();
        rundata.getParameters().setProperties(user);
        user.setLastAccessDate();

        if (!dontUpdatePasswd) {
          JetspeedSecurity.forcePassword(user, password.getValue());
        } else {
          Expression exp = ExpressionFactory.matchDbExp(
              TurbineUser.USER_ID_PK_COLUMN, user.getUserId());
          SelectQuery query = new SelectQuery(TurbineUser.class, exp);
          List<?> list = dataContext.performQuery(query);
          if (list == null || list.size() == 0)
            return false;
          TurbineUser tuser = (TurbineUser) list.get(0);
          user.setPassword(tuser.getPasswordValue());
        }
        String strDisabled = user.getDisabled();
        // String strDisabled = disabled.getValue();
        user.setDisabled(strDisabled);
        if (!"T".equals(strDisabled) && "T".equals(oldDisabled)
            && JetspeedSecurity.isDisableAccountCheckEnabled()) {
          JetspeedSecurity.resetDisableAccountCheck(user.getUserName());
        }

        user.setInTelephone(in_telephone.getValue());
        if (!out_telephone1.getValue().equals("")
            && !out_telephone2.getValue().equals("")
            && !out_telephone3.getValue().equals("")) {
          user.setOutTelephone(new StringBuffer()
              .append(out_telephone1.getValue()).append("-")
              .append(out_telephone2.getValue()).append("-")
              .append(out_telephone3.getValue()).toString());
        } else {
          user.setOutTelephone("");
        }

        if (!cellular_phone1.getValue().equals("")
            && !cellular_phone2.getValue().equals("")
            && !cellular_phone3.getValue().equals("")) {
          user.setCellularPhone(new StringBuffer()
              .append(cellular_phone1.getValue()).append("-")
              .append(cellular_phone2.getValue()).append("-")
              .append(cellular_phone3.getValue()).toString());
        } else {
          user.setCellularPhone("");
        }
        user.setCellularMail(cellular_mail.getValue());
        // user.setCompanyId((int)company_id.getValue());
        user.setPositionId((int) position_id.getValue());
        user.setFirstNameKana(first_name_kana.getValue());
        user.setLastNameKana(last_name_kana.getValue());
        if (filebean != null) {
          if (filebean.getFileId() > 0) {
            // 顔写真を登録する．
            user.setPhoto(facePhoto);
          }
        } else {
          user.setPhoto(null);
        }

        user.setEmail(email.getValue());
        // ユーザーを更新
        JetspeedSecurity.saveUser(user);

        // 部署を移動
        List<UserGroupLiteBean> postList_old = AccountUtils.getPostBeanList(Integer.parseInt(user
            .getUserId()));
        if (postList_old != null && postList_old.size() > 0) {
          UserGroupLiteBean uglb = null;
          int old_size = postList_old.size();
          // グループからユーザーを削除
          for (int i = 0; i < old_size; i++) {
            uglb = (UserGroupLiteBean) postList_old.get(i);
            JetspeedSecurity.unjoinGroup(user.getUserName(), uglb.getGroupId());
          }
        }

        String[] groupNameList = rundata.getParameters().getStrings("group_to");
        if (groupNameList != null && groupNameList.length > 0) {
          int size = groupNameList.length;
          for (int i = 0; i < size; i++) {
            // グループへユーザーを追加
            JetspeedSecurity.joinGroup(user.getUserName(), groupNameList[i]);
          }
        }

        ALBaseUser currentUser = (ALBaseUser) rundata.getUser();

        // もし編集者自身が自分の情報を修正していた場合には
        // セッション情報も書き換える。現状は以下のため、ifは常にfalse
        // CurrentUser ： 管理者のみ
        // User : 一般ユーザーのみ
        if (currentUser.getUserName().equals(user.getUserName())) {
          currentUser.setPassword(user.getPassword());
          currentUser.setFirstName(user.getFirstName());
          currentUser.setLastName(user.getLastName());
          currentUser.setEmail(user.getEmail());
          currentUser.setInTelephone(user.getInTelephone());
          currentUser.setOutTelephone(user.getOutTelephone());
          currentUser.setCellularPhone(user.getCellularPhone());
          currentUser.setCellularMail(user.getCellularMail());
          // user.setCompanyId((int)company_id.getValue());
          currentUser.setPositionId(user.getPositionId());
          currentUser.setPostId(user.getPostId());
          currentUser.setFirstNameKana(user.getFirstNameKana());
          currentUser.setLastNameKana(user.getLastNameKana());
        }
        // WebAPIとのDB同期
        if (!ALDataSyncFactoryService.getInstance().getDataSyncHandler()
            .updateUser(user)) {
          return false;
        }
      }

      /** ユーザリストのキャッシュをクリアする */
      UserUtils.clearCache();

      // 一時的な添付ファイルの削除
      File folder = FileuploadUtils.getFolder(org_id,
          ALEipUtils.getUserId(rundata), folderName);
      FileuploadUtils.deleteFolder(folder);
    } catch (Exception e) {
      logger.error("Exception", e);
      res = false;
    }
    return res;
  }

  /**
   * 『ユーザー』を無効化します。 <BR>
   *
   * @param rundata
   * @param context
   * @param msgList
   * @return
   *
   */
  public boolean disableFormData(RunData rundata, Context context,
      List<String> msgList) {
    try {
      if (!doCheckSecurity(rundata, context)) {
        return false;
      }
      // WebAPIのDBへ接続できるか確認
      if (!ALDataSyncFactoryService.getInstance().getDataSyncHandler()
          .checkConnect()) {
        msgList.add("コントロールパネルWebAPIのデータベースの接続に失敗したため、処理は実行されませんでした。");
        return false;
      }
      String user_name = ALEipUtils.getTemp(rundata, context,
          ALEipConstants.ENTITY_ID);

      if (user_name == null || "".equals(user_name)) {
        return false;
      }

      DataContext dataContext = DatabaseOrmService.getInstance()
          .getDataContext();
      SelectQuery query = new SelectQuery(TurbineUser.class);
      Expression exp = ExpressionFactory.matchExp(
          TurbineUser.LOGIN_NAME_PROPERTY, user_name);
      query.setQualifier(exp);
      List<?> list = dataContext.performQuery(query);

      if (list == null || list.size() == 0) {
        return false;
      }

      TurbineUser target_user = (TurbineUser) list.get(0);
      target_user.setDisabled("N");

      // ワークフロー自動承認
      AccountUtils.acceptWorkflow(target_user.getUserId());

      dataContext.commitChanges();

      // WebAPIとのDB同期
      String[] user_name_list = { user_name };
      if (!ALDataSyncFactoryService.getInstance().getDataSyncHandler()
          .multiDisableUser(user_name_list, user_name_list.length)) {
        return false;
      }

    } catch (Exception e) {
      logger.error("Exception", e);
      return false;
    }
    return true;
  }

  /**
   * 『ユーザー』を有効化します。 <BR>
   *
   * @param rundata
   * @param context
   * @param msgList
   * @return
   *
   */
  public boolean enableFormData(RunData rundata, Context context,
      List<String> msgList) {
    try {
      if (!doCheckSecurity(rundata, context)) {
        return false;
      }
      // WebAPIのDBへ接続できるか確認
      if (!ALDataSyncFactoryService.getInstance().getDataSyncHandler()
          .checkConnect()) {
        msgList.add("コントロールパネルWebAPIのデータベースの接続に失敗したため、処理は実行されませんでした。");
        return false;
      }
      String user_name = ALEipUtils.getTemp(rundata, context,
          ALEipConstants.ENTITY_ID);

      if (user_name == null || "".equals(user_name)) {
        return false;
      }

      DataContext dataContext = DatabaseOrmService.getInstance()
          .getDataContext();
      SelectQuery query = new SelectQuery(TurbineUser.class);
      Expression exp = ExpressionFactory.matchExp(
          TurbineUser.LOGIN_NAME_PROPERTY, user_name);
      query.setQualifier(exp);
      List<?> list = dataContext.performQuery(query);

      if (list == null || list.size() == 0) {
        return false;
      }

      ((TurbineUser) list.get(0)).setDisabled("F");
      dataContext.commitChanges();

      // WebAPIとのDB同期
      String[] user_name_list = { user_name };
      if (!ALDataSyncFactoryService.getInstance().getDataSyncHandler()
          .multiEnableUser(user_name_list, user_name_list.length)) {
        return false;
      }

    } catch (Exception e) {
      logger.error("Exception", e);
      return false;
    }
    return true;
  }

  /**
   * 『ユーザー』を削除します。 <BR>
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
    /*
     * // PSMLも含め、ユーザを削除 try { ALBaseUser user =
     * AccountUtils.getBaseUser(rundata, context); if (user == null) return
     * false; // ユーザーを削除 JetspeedSecurity.removeUser(user.getUserName()); //
     * TODO: ユーザーグループロールを削除する処理 return true; } catch (Exception e) {
     * logger.error("Exception", e); return false; }
     */

    try {
      // WebAPIのDBへ接続できるか確認
      logger.debug("deleteFormData");
      if (!ALDataSyncFactoryService.getInstance().getDataSyncHandler()
          .checkConnect()) {
        msgList.add("コントロールパネルWebAPIのデータベースの接続に失敗したため、処理は実行されませんでした。");
        return false;
      }
      logger.debug("enddeleteFormData");
      String user_name = ALEipUtils.getTemp(rundata, context,
          ALEipConstants.ENTITY_ID);

      if (user_name == null)
        return false;

      // ユーザーを論理削除
      DataContext dataContext = DatabaseOrmService.getInstance()
          .getDataContext();
      ObjectId oid_user = new ObjectId("TurbineUser",
          TurbineUser.LOGIN_NAME_COLUMN, user_name);
      TurbineUser user = (TurbineUser) dataContext.refetchObject(oid_user);

      user.setPositionId(Integer.valueOf(0));
      user.setDisabled("T");
      // dataContext.commitChanges();

      /*
       * String query1 = "UPDATE TURBINE_USER SET DISABLED = 'T', POST_ID = 0,
       * POSITION_ID = 0 WHERE LOGIN_NAME = '" + user_name + "'"; SQLTemplate
       * rawSelect1 = new SQLTemplate(TurbineUser.class, query1, false);
       * dataContext.performQuery(rawSelect1);
       */

      // ユーザーIDを取得する
      SelectQuery query = new SelectQuery(TurbineUser.class);
      Expression exp1 = ExpressionFactory.matchExp(
          TurbineUser.LOGIN_NAME_PROPERTY, user_name);
      query.setQualifier(exp1);
      List<?> list3 = dataContext.performQuery(query);

      int userNum = list3.size();
      if (userNum != 1)
        return false;
      TurbineUser deleteuser = (TurbineUser) list3.get(0);
      String userId;
      userId = deleteuser.getUserId().toString();

      // 対象ユーザのユーザーグループロールをすべて削除する
      SelectQuery query2 = new SelectQuery(TurbineUserGroupRole.class);
      Expression exp2 = ExpressionFactory.matchExp(
          TurbineUserGroupRole.TURBINE_USER_PROPERTY, userId);
      query2.setQualifier(exp2);
      List<?> list4 = dataContext.performQuery(query2);

      TurbineUserGroupRole ugr = null;
      for (int i = 0; i < list4.size(); i++) {
        ugr = (TurbineUserGroupRole) list4.get(i);
        dataContext.deleteObject(ugr);
      }

      /*
       * ObjectId oid_ugr = new ObjectId("TurbineUserGroupRole",
       * TurbineUserGroupRole.TURBINE_USER_PROPERTY, userId);
       *
       * //TurbineUserGroupRole ugr = (TurbineUserGroupRole)
       * dataContext.refetchObject(oid_ugr); dataContext.deleteObject(ugr);
       */

      // dataContext.commitChanges();
      /*
       * String query2 = "DELETE FROM TURBINE_USER_GROUP_ROLE " + "WHERE USER_ID
       * IN " + "(SELECT USER_ID FROM TURBINE_USER WHERE login_name= '" +
       * user_name + "')"; SQLTemplate rawSelect2 = new
       * SQLTemplate(TurbineUser.class, query2, false);
       * dataContext.performQuery(rawSelect2);
       */

      /*
       * String query3 = "SELECT USER_ID FROM TURBINE_USER WHERE login_name= '"
       * + user_name + "'"; SQLTemplate rawSelect3 = new
       * SQLTemplate(TurbineUser.class, query3, true);
       * rawSelect3.setFetchingDataRows(true); List list3 =
       * dataContext.performQuery(rawSelect3);
       */

      // ToDoを削除する
      String sql4 = "DELETE FROM EIP_T_TODO WHERE USER_ID = '" + userId + "'";
      @SuppressWarnings("deprecation")
      SQLTemplate rawSelect4 = new SQLTemplate(EipTTodo.class, sql4, false);
      dataContext.performQuery(rawSelect4);
      String sql5 = "DELETE FROM EIP_T_TODO_CATEGORY WHERE USER_ID = '"
          + userId + "'";
      @SuppressWarnings("deprecation")
      SQLTemplate rawSelect5 = new SQLTemplate(EipTTodoCategory.class, sql5,
          false);
      dataContext.performQuery(rawSelect5);

      // ブログを削除する
      String sql6 = "DELETE FROM EIP_T_BLOG WHERE OWNER_ID = '" + userId + "'";
      @SuppressWarnings("deprecation")
      SQLTemplate rawSelect6 = new SQLTemplate(EipTBlog.class, sql6, false);
      dataContext.performQuery(rawSelect6);

      // ブログの足跡を削除する
      String sql7 = "DELETE FROM EIP_T_BLOG_FOOTMARK_MAP WHERE USER_ID = '"
          + userId + "'";
      @SuppressWarnings("deprecation")
      SQLTemplate rawSelect7 = new SQLTemplate(EipTBlogFootmarkMap.class, sql7,
          false);
      dataContext.performQuery(rawSelect7);

      // ワークフロー自動承認
      AccountUtils.acceptWorkflow(deleteuser.getUserId());
      /*
       * SelectQuery workflow_request_map_query = new
       * SelectQuery(EipTWorkflowRequestMap.class); Expression workflow_exp =
       * ExpressionFactory.matchExp( EipTWorkflowRequestMap.USER_ID_PROPERTY,
       * userId); Expression workflow_exp2 = ExpressionFactory.matchExp(
       * EipTWorkflowRequestMap.STATUS_PROPERTY, "C");
       * workflow_request_map_query
       * .setQualifier(workflow_exp.andExp(workflow_exp2)); List
       * workflow_request_map_list =
       * dataContext.performQuery(workflow_request_map_query);
       * EipTWorkflowRequestMap workflow_request_map = null; for (int j = 0; j <
       * list4.size(); j++) { workflow_request_map = (EipTWorkflowRequestMap)
       * workflow_request_map_list.get(j); // 次の人がいるかどうか int request_number =
       * workflow_request_map.getOrderIndex(); SelectQuery
       * workflow_request_map_query2 = new
       * SelectQuery(EipTWorkflowRequestMap.class); Expression workflow_exp3 =
       * ExpressionFactory.matchExp(
       * EipTWorkflowRequestMap.EIP_TWORKFLOW_REQUEST_PROPERTY,
       * workflow_request_map.getEipTWorkflowRequest()); Expression
       * workflow_exp4 = ExpressionFactory.matchExp(
       * EipTWorkflowRequestMap.ORDER_INDEX_PROPERTY,
       * Integer.valueOf(request_number + 1));
       * workflow_request_map_query2.setQualifier
       * (workflow_exp3.andExp(workflow_exp4)); List workflow_request_map_list2
       * = dataContext.performQuery(workflow_request_map_query2); if
       * (workflow_request_map_list2.size() == 1) { // 自動的に承認して次の人に回す
       * workflow_request_map.setStatus("A"); EipTWorkflowRequestMap
       * workflow_request_map2 = (EipTWorkflowRequestMap)
       * workflow_request_map_list2.get(0);
       * workflow_request_map2.setStatus("C"); } }
       */

      dataContext.commitChanges();

      // 他のユーザの順番を変更する．
      SelectQuery p_query = new SelectQuery(EipMUserPosition.class);
      p_query.addOrdering(EipMUserPosition.POSITION_PROPERTY, true);
      List<?> userPositions = dataContext.performQuery(p_query);
      if (userPositions != null && userPositions.size() > 0) {
        EipMUserPosition userPosition = null;
        int index = -1;
        int size = userPositions.size();
        for (int i = 0; i < size; i++) {
          userPosition = (EipMUserPosition) userPositions.get(i);
          if (userId.equals(userPosition.getTurbineUser().toString())) {
            // 指定したユーザを削除する．
            dataContext.deleteObject(userPosition);
            index = i;
            break;
          }
        }
        if (index >= 0) {
          for (int i = index + 1; i < size; i++) {
            userPosition = (EipMUserPosition) userPositions.get(i);
            userPosition.setPosition(Integer.valueOf(i));
          }
        }
      }

      // PSMLを削除
      JetspeedUser juser = JetspeedSecurity.getUser(new UserNamePrincipal(
          user_name));
      PsmlManager.removeUserDocuments(juser);

      // ユーザー名の先頭に"dummy_userid_"を追加
      String dummy_user_name = ALEipUtils.dummy_user_head + userId + "_"
          + user_name;
      user.setLoginName(dummy_user_name);

      dataContext.commitChanges();

      // WebAPIとのDB同期
      if (!ALDataSyncFactoryService.getInstance().getDataSyncHandler()
          .deleteUser(user_name)) {
        return false;
      }

      return true;
    } catch (Exception e) {
      logger.error("Exception", e);
      return false;
    }
  }

  /**
   * 添付ファイルを削除する．
   *
   * @param action
   * @param rundata
   * @param context
   * @return TRUE 成功 FALSE 失敗
   */
  public boolean doDeleteAttachments(ALAction action, RunData rundata,
      Context context, String mode) {
    try {
      init(action, rundata, context);
      action.setMode(mode);
      setMode(mode);
      ArrayList<String> msgList = new ArrayList<String>();
      setValidator();
      boolean res = (setFormData(rundata, context, msgList) && deleteAttachments(
          rundata, context, msgList));
      action.setResultData(this);
      action.addErrorMessages(msgList);
      action.putData(rundata, context);
      return res;
    } catch (ALPageNotFoundException e) {
      ALEipUtils.redirectPageNotFound(rundata);
      return false;
    } catch (ALDBErrorException e) {
      ALEipUtils.redirectDBError(rundata);
      return false;
    }
  }

  /**
   * @see com.aimluck.eip.common.ALAbstractFormData#updateFormData(org.apache.turbine.util.RunData,
   *      org.apache.velocity.context.Context, java.util.ArrayList)
   */
  protected boolean deleteAttachments(RunData rundata, Context context,
      List<String> msgList) {
    if (rundata == null || context == null) {
      msgList.add("システム上の問題のため、削除できませんでした。");
      return false;
    }
    int userId = ALEipUtils.getUserId(rundata);

    ArrayList<FileuploadLiteBean> fileBeanList = new ArrayList<FileuploadLiteBean>();
    fileBeanList.add(filebean);
    return FileuploadUtils.deleteAttachments(org_id, userId, folderName,
        fileBeanList);
  }

  /**
   * 指定したchar型文字が記号であるかを判断します。
   *
   * @param ch
   * @return
   */
  protected boolean isSymbol(char ch) {
    byte[] chars;

    try {
      chars = (Character.valueOf(ch).toString()).getBytes("shift_jis");
    } catch (UnsupportedEncodingException ex) {
      return false;
    }

    if (chars == null || chars.length == 2 || Character.isDigit(ch)
        || Character.isLetter(ch)) {
      return false;
    } else {
      return true;
    }

  }

  /**
   * 携帯メールアドレスを取得します。 <BR>
   *
   * @return
   */
  public ALStringField getCellularMail() {
    return cellular_mail;
  }

  /**
   * 会社IDを取得します。 <BR>
   *
   * @return
   */
  public ALNumberField getCompanyId() {
    return company_id;
  }

  /**
   * アカウント有効/無効フラグを取得します。 <BR>
   *
   * @return
   */
  public ALStringField getDisabled() {
    return disabled;
  }

  /**
   * メールアドレスを取得します。 <BR>
   *
   * @return
   */
  public ALStringField getEmail() {
    return email;
  }

  /**
   * フリガナ（名）を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getFirstNameKana() {
    return first_name_kana;
  }

  /**
   * 名前（名）を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getFirstName() {
    return firstname;
  }

  /**
   * 電話番号（内線）を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getInTelephone() {
    return in_telephone;
  }

  /**
   * フリガナ（姓）を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getLastNameKana() {
    return last_name_kana;
  }

  /**
   * 名前（姓）を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getLastName() {
    return lastname;
  }

  /**
   * 携帯電話番号を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getCellularPhone1() {
    return cellular_phone1;
  }

  /**
   * 携帯電話番号を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getCellularPhone2() {
    return cellular_phone2;
  }

  /**
   * 携帯電話番号を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getCellularPhone3() {
    return cellular_phone3;
  }

  /**
   * 電話番号を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getOutTelephone1() {
    return out_telephone1;
  }

  /**
   * 電話番号を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getOutTelephone2() {
    return out_telephone2;
  }

  /**
   * 電話番号を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getOutTelephone3() {
    return out_telephone3;
  }

  /**
   * パスワードを取得します。 <BR>
   *
   * @return
   */
  public ALStringField getPassword() {
    return password;
  }

  /**
   * パスワード2を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getPassword2() {
    return password2;
  }

  /**
   * 役職IDを取得します。 <BR>
   *
   * @return
   */
  public ALNumberField getPositionId() {
    return position_id;
  }

  /**
   * 部署IDを取得します。 <BR>
   *
   * @return
   */
  public ALNumberField getPostId() {
    return post_id;
  }

  /**
   * ユーザー名を取得します。 <BR>
   *
   * @return
   */
  public ALStringField getUserName() {
    return username;
  }

  /**
   * 役職リストを取得します。 <BR>
   *
   * @return
   */
  public List<ALEipPosition> getPositionList() {
    return positionList;
  }

  /**
   *
   * @return
   */
  public List<UserGroupLiteBean> getPostList() {
    return postList;
  }

  /**
   *
   * @return
   */
  public Map<Integer, ALEipPost> getPostMap() {
    return ALEipManager.getInstance().getPostMap();
  }

  /**
   *
   * @return
   */
  public Map<Integer, ALEipPosition> getPositionMap() {
    return ALEipManager.getInstance().getPositionMap();
  }

  /**
   *
   * @return
   */
  public AccountPostFormData getPost() {
    return post;
  }

  /**
   *
   * @return
   */
  public AccountPositionFormData getPosition() {
    return position;
  }

  /**
   *
   * @return
   */
  public boolean isNewPost() {
    return is_new_post;
  }

  /**
   *
   * @return
   */
  public boolean isNewPosition() {
    return is_new_position;
  }

  public FileuploadLiteBean getFileBean() {
    return filebean;
  }

  public List<FileuploadLiteBean> getAttachmentFileNameList() {
    if (filebean == null)
      return null;
    ArrayList<FileuploadLiteBean> list = new ArrayList<FileuploadLiteBean>();
    list.add(filebean);
    return list;
  }

  public String getFolderName() {
    return folderName;
  }

}
