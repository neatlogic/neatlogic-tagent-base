/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.framework.tagent.service;

import codedriver.framework.tagent.dto.TagentVo;

/**
 * @author lvzk
 * @since 2021/8/23 17:40
 **/
public interface TagentService {

    int updateTagentById(TagentVo tagent);

    Long saveTagent(TagentVo tagentVo);
}
