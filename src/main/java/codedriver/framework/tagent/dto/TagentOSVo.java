package codedriver.framework.tagent.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.restful.annotation.EntityField;

public class TagentOSVo extends BasePageVo {
    private static final long serialVersionUID = -6989056143057900024L;
    @EntityField(name = "OS id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "OS name", type = ApiParamType.STRING)
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
