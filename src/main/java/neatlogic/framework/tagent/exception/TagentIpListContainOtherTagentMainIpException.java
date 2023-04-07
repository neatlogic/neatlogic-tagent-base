/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package neatlogic.framework.tagent.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.tagent.dto.TagentVo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author longrf
 * @date 2022/11/25 17:36
 */

public class TagentIpListContainOtherTagentMainIpException extends ApiRuntimeException {

    public TagentIpListContainOtherTagentMainIpException(TagentVo registeringTagent, List<TagentVo> registeredTagentList) {
        super("exception.tagent.tagentiplistcontainothertagentmainipexception", registeringTagent.getIp(), registeringTagent.getPort(), registeringTagent.getIpString(), registeredTagentList.stream().map(a -> a.getIp() + ":" + a.getPort() + "(" + a.getName() + ")").collect(Collectors.joining(",")));
    }
}
