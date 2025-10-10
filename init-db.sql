SELECT 'CREATE DATABASE user_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'user_db')\gexec
SELECT 'CREATE DATABASE document_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'document_db')\gexec
SELECT 'CREATE DATABASE workflow_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'workflow_db')\gexec
SELECT 'CREATE DATABASE tracking_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'tracking_db')\gexec
SELECT 'CREATE DATABASE notification_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_db')\gexec

GRANT ALL PRIVILEGES ON DATABASE user_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE document_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE workflow_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE tracking_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO postgres;