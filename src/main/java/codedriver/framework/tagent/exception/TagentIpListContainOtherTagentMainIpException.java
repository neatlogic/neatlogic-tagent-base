/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */
package codedriver.framework.tagent.exception;

import codedriver.framework.tagent.dto.TagentVo;

/**
 * @author longrf
 * @date 2022/11/25 17:36
 */

public class TagentIpListContainOtherTagentMainIpException extends RuntimeException {

    public TagentIpListContainOtherTagentMainIpException(TagentVo registeringTagent, TagentVo registeredTagent) {
        super("正在注册的tagent（ip：“" + registeringTagent.getIp() + "”，port：“" + registeringTagent.getPort() + "”）的包含ip列表（ipString：“" + registeringTagent.getIpString() + "”）包含了另一个已经注册了的tagent（ip：“" + registeredTagent.getIp() + "”,port：“" + registeredTagent.getPort() + "”）的主ip，请修改包含ip列表（ipString）后再重新注册");
    }
}
