package com.dair.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;




@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaisBaseModel implements Serializable {

    private String id;
    @NotNull
    private String name;
    private String description;
    private String type;
    private String status;

    private List<String> tags;
    private Map<String, String> metadata;

    private Date createdDate;
    private Date updatedDate;

}
