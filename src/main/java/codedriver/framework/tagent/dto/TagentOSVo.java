package codedriver.framework.tagent.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BaseEditorVo;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.framework.util.SnowflakeUtil;

public class TagentOSVo extends BaseEditorVo {
    private static final long serialVersionUID = -6989056143057900024L;
    @EntityField(name = "OS id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "OS name", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "描述", type = ApiParamType.STRING)
    private String description;

    public TagentOSVo(String name) {
        this.name = name;
    }

    public TagentOSVo() {
    }

    public Long getId() {
        if(id == null){
            id = SnowflakeUtil.uniqueLong();
        }
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
