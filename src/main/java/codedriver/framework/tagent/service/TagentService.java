/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.framework.tagent.service;

import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import com.alibaba.fastjson.JSONObject;

/**
 * @author lvzk
 * @since 2021/8/23 17:40
 **/
public interface TagentService {

    int updateTagentById(TagentVo tagent);

    Long saveTagent(TagentVo tagentVo);

    /**
     * 执行tagent action
     * @param message 入参
     * @param action 执行动作
     */
    JSONObject execTagentCmd(TagentMessageVo message, String action) throws Exception;
}
