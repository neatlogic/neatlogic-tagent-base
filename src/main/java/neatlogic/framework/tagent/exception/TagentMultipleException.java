/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */
package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.tagent.dto.TagentVo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author longrf
 * @date 2023/3/7 16:07
 */

public class TagentMultipleException extends ApiRuntimeException {
    public TagentMultipleException(String insertTagentIp, Integer insertTagentPort, List<TagentVo> tagentVoList) {
        super("注册失败，通过输入ip({0})和输入port({1})找到多个tagent：{2}", insertTagentIp, insertTagentPort, tagentVoList.stream().map(a -> a.getIp() + ":" + a.getPort() + "(" + a.getName() + ")").collect(Collectors.joining(",")));
    }
}
