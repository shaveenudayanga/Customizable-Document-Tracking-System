-- Initialize databases for all services
CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS document_db;
CREATE DATABASE IF NOT EXISTS workflow_db;
CREATE DATABASE IF NOT EXISTS tracking_db;
CREATE DATABASE IF NOT EXISTS notification_db;

-- Create user with permissions
CREATE USER IF NOT EXISTS docutrace WITH PASSWORD 'docutrace123';
GRANT ALL PRIVILEGES ON DATABASE user_db TO docutrace;
GRANT ALL PRIVILEGES ON DATABASE document_db TO docutrace;
GRANT ALL PRIVILEGES ON DATABASE workflow_db TO docutrace;
GRANT ALL PRIVILEGES ON DATABASE tracking_db TO docutrace;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO docutrace;