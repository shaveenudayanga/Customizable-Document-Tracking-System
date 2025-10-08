-- Align database constraints with updated entity requirements

-- Department: ensure active flag defaults and is non-null
UPDATE department SET active = TRUE WHERE active IS NULL;
ALTER TABLE department
    ALTER COLUMN active SET DEFAULT TRUE,
    ALTER COLUMN active SET NOT NULL;

-- Pipeline template: guarantee required metadata and JSON structure
UPDATE pipeline_template
SET steps_json = COALESCE(steps_json::jsonb, '[]'::jsonb),
    created_by = COALESCE(created_by, 'system'),
    is_permanent = COALESCE(is_permanent, FALSE),
    version = COALESCE(version, 1);

ALTER TABLE pipeline_template
    ALTER COLUMN steps_json TYPE jsonb USING COALESCE(steps_json::jsonb, '[]'::jsonb),
    ALTER COLUMN steps_json SET DEFAULT '[]'::jsonb,
    ALTER COLUMN steps_json SET NOT NULL,
    ALTER COLUMN created_by SET NOT NULL,
    ALTER COLUMN is_permanent SET DEFAULT FALSE,
    ALTER COLUMN is_permanent SET NOT NULL,
    ALTER COLUMN version SET DEFAULT 1,
    ALTER COLUMN version SET NOT NULL;

-- Pipeline instance: backfill missing data and enforce constraints
UPDATE pipeline_instance
SET process_definition_key = COALESCE(process_definition_key, CONCAT('migrated_', id)),
    process_instance_id = COALESCE(process_instance_id, CONCAT('migrated_', id)),
    pipeline_json = COALESCE(pipeline_json::jsonb, '[]'::jsonb),
    status = CASE WHEN status IS NULL OR UPPER(status) = 'RUNNING' THEN 'IN_PROGRESS' ELSE status END,
    created_by = COALESCE(created_by, 'system');

ALTER TABLE pipeline_instance
    ALTER COLUMN pipeline_json TYPE jsonb USING COALESCE(pipeline_json::jsonb, '[]'::jsonb),
    ALTER COLUMN pipeline_json SET DEFAULT '[]'::jsonb,
    ALTER COLUMN pipeline_json SET NOT NULL,
    ALTER COLUMN process_definition_key SET NOT NULL,
    ALTER COLUMN process_instance_id SET NOT NULL,
    ALTER COLUMN status SET DEFAULT 'IN_PROGRESS',
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN created_by SET NOT NULL;
