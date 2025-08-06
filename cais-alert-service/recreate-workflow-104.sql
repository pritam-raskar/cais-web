-- Recreate Workflow 104 based on test data structure
-- Only run this if workflow 104 is completely missing from all tables

-- Insert the main workflow record
INSERT INTO info_alert.cm_workflow (
    workflow_id, 
    workflow_name, 
    description, 
    created_by, 
    updated_by, 
    created_date, 
    updated_date,
    ui_config,
    is_active,
    is_deleted
) VALUES (
    104,
    'Test Account Review Workflow',
    'Test workflow for account review process',
    'TEST_WORKFLOW_SYSTEM',
    'TEST_WORKFLOW_USER',
    NOW(),
    NOW(),
    '{"steps":[],"transitions":[]}',
    true,
    false
);

-- Verify the insertion worked
SELECT * FROM info_alert.cm_workflow WHERE workflow_id = 104;

-- Check if there are any workflow steps that need to be reconnected
SELECT * FROM info_alert.cm_workflow_step WHERE workflow_id = 104;
SELECT * FROM info_alert.cm_workflow_transition WHERE workflow_id = 104;