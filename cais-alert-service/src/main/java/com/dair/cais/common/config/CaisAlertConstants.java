package com.dair.cais.common.config;

import com.dair.util.CaisBaseConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CaisAlertConstants extends CaisBaseConstants {

    // mongo db collection names
    public static final String MONGO_COLLECTION_ALERT_NOTES = "alert-notes";
    public static final String MONGO_COLLECTION_ALERT_ATTACHMENTS = "alert-attachments";
    public static final String ALERT_TYPE_COLLECTION_NAME = "alert-types";

    public static final String CAIS_ALERT_TYPE_COLLECTION_NAME = "alertTypes";

    public static final String USER_PERMISSION_DATA = "UserBasedPermissions";

    public static final String ROLE_PERMISSION_COLLECTION = "RoleBasedPermission";

    public static final String ALERT_STEP_CHECKLIST = "AlertStepChecklist";

    public static final String ALERT_TRANSITION_REASON = "AlertTransitionReason";



    public static final String AML = "AML";
    public static final String TRADE_REVIEW = "Trade_Review";
    public static final String FRAUD = "Fraud";
    public static final String OTHER = "OTHER";

    public static final String ALERTS = "alerts";
    public static final List<String> validAlertTypes = Arrays.asList(AML.toLowerCase(), TRADE_REVIEW.toLowerCase(),
            FRAUD.toLowerCase(), ALERTS.toLowerCase());
    public static final List<String> allAlertTypes;

    static {
        allAlertTypes = new ArrayList<>(validAlertTypes);
        allAlertTypes.add(OTHER.toLowerCase());
    }
}
