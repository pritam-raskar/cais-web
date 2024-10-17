package com.dair.cais.alert.reason;

import com.dair.cais.common.config.CaisAlertConstants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = CaisAlertConstants.ALERT_TRANSITION_REASON)
public class AlertTransitionReason {
    @Id
    private String id;
    private String alertId;
    private String fromStepId;
    private String toStepId;
    private String userId;
    private String userName;
    private List<String> checkedReasons;
    private String note;
    private LocalDateTime createdDate;
}