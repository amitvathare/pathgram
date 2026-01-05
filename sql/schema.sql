-- SQL DDL for LIS Event Listener Backend

CREATE TABLE IF NOT EXISTS patient (
    patient_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    dob DATE,
    gender VARCHAR(10),
    hospital_id TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sample (
    sample_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID REFERENCES patient(patient_id),
    received_at TIMESTAMP WITH TIME ZONE,
    source_ip TEXT,
    status VARCHAR(50),
    mode TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS result (
    result_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sample_id UUID REFERENCES sample(sample_id),
    obx_code TEXT,
    value TEXT,
    units TEXT,
    reference_range TEXT,
    flag TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TYPE direction_enum AS ENUM ('inbound','outbound');

CREATE TABLE IF NOT EXISTS event_log (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sample_id UUID,
    raw_message TEXT,
    direction direction_enum DEFAULT 'inbound',
    ack_status VARCHAR(20),
    processed_at TIMESTAMP WITH TIME ZONE,
    created_by TEXT
);

CREATE TABLE IF NOT EXISTS binary_artifact (
    artifact_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sample_id UUID REFERENCES sample(sample_id),
    type TEXT,
    storage_path TEXT,
    content_type TEXT,
    size BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS mode_mapping (
    mode TEXT PRIMARY KEY,
    lis_test_code TEXT,
    analyzer_mode TEXT
);

CREATE TABLE IF NOT EXISTS app_user (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    roles TEXT
);

CREATE TABLE IF NOT EXISTS audit_log (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity TEXT,
    entity_id UUID,
    action TEXT,
    user_id UUID,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT now(),
    details TEXT
);

-- Sample seed for mode_mapping
INSERT INTO mode_mapping(mode, lis_test_code, analyzer_mode) VALUES
('CBC','CBC01','MODE_A')
ON CONFLICT (mode) DO NOTHING;
