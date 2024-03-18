/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
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
        super("正在注册的tagent（ip：“{0}”，port：“{1}”）的包含ip列表（ipString：“{2}”）包含了下列已经注册了的tagent的主ip：{3}。请修改包含ip列表（ipString）后再重新注册", registeringTagent.getIp(), registeringTagent.getPort(), registeringTagent.getIpString(), registeredTagentList.stream().map(a -> a.getIp() + ":" + a.getPort() + "(" + a.getName() + ")").collect(Collectors.joining(",")));
    }
}
