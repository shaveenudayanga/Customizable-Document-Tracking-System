-- Department catalog
CREATE TABLE department (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Pipeline templates (permanent/temporary)
CREATE TABLE pipeline_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    document_type VARCHAR(50),
    steps_json JSONB NOT NULL,
    is_permanent BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 1,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Pipeline instances (track process instances)
CREATE TABLE pipeline_instance (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    template_id BIGINT REFERENCES pipeline_template(id),
    process_definition_key VARCHAR(100),
    process_instance_id VARCHAR(100) UNIQUE,
    pipeline_json JSONB,
    status VARCHAR(50) DEFAULT 'RUNNING',
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- Seed departments
INSERT INTO department (key, name) VALUES 
    ('hr', 'Human Resources'),
    ('finance', 'Finance'),
    ('it', 'Information Technology'),
    ('admin', 'Administration');
