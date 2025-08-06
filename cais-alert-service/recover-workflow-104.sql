-- Workflow 104 Recovery Script
-- Run this against your PostgreSQL database

-- First, check if the workflow exists but is marked as deleted/inactive
SELECT * FROM info_alert.cm_workflow WHERE workflow_id = 104;

-- Check for soft delete (common patterns)
SELECT * FROM info_alert.cm_workflow WHERE workflow_id = 104 AND (is_active = false OR is_deleted = true);

-- Check workflow audit/history table if it exists
SELECT * FROM info_alert.cm_workflow_audit WHERE workflow_id = 104 ORDER BY created_date DESC LIMIT 10;
SELECT * FROM info_alert.cm_workflow_history WHERE workflow_id = 104 ORDER BY created_date DESC LIMIT 10;

-- Check for related workflow steps that might still exist
SELECT * FROM info_alert.cm_workflow_step WHERE workflow_id = 104;

-- Check for workflow transitions
SELECT * FROM info_alert.cm_workflow_transition WHERE workflow_id = 104;

-- If found in history/audit, you can restore with something like:
-- INSERT INTO info_alert.cm_workflow 
-- SELECT * FROM info_alert.cm_workflow_history 
-- WHERE workflow_id = 104 
-- ORDER BY created_date DESC LIMIT 1;

-- Or if it's soft deleted, restore with:
-- UPDATE info_alert.cm_workflow 
-- SET is_active = true, is_deleted = false, modified_date = NOW()
-- WHERE workflow_id = 104;