package com.dair.cais.alert.checklist;

import com.dair.cais.common.config.CaisAlertConstants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = CaisAlertConstants.ALERT_STEP_CHECKLIST)
public class AlertStepChecklist {
    @Id
    private String id;
    private String alertId;
    private String stepId;
    private String userId;
    private LocalDateTime createdDate;
    private String userName;
    private List<String> checkedItems;
}