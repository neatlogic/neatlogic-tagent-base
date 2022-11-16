package codedriver.framework.tagent.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BaseEditorVo;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.framework.util.SnowflakeUtil;

public class TagentOsBitVo extends BaseEditorVo {
    private static final long serialVersionUID = -6989056143057900024L;
    @EntityField(name = "id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "name", type = ApiParamType.STRING)
    private String name;

    public TagentOsBitVo() {
    }

    public TagentOsBitVo(String name) {
        this.name = name;
    }

    public Long getId() {
        if (id == null) {
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

}
