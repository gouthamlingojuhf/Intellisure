CREATE TABLE IF NOT EXISTS workflow (
    workflow_id BINARY(16) NOT NULL,
    workflow_type VARCHAR(100) NOT NULL,
    reference_id BINARY(16) NOT NULL,
    reference_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_step VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (workflow_id),
    INDEX idx_workflow_reference (reference_id)
);

CREATE TABLE IF NOT EXISTS workflow_task (
    task_id BINARY(16) NOT NULL,
    workflow_id BINARY(16) NOT NULL,
    task_name VARCHAR(100) NOT NULL,
    assignee_id BINARY(16),
    status VARCHAR(50) NOT NULL,
    due_date DATE,
    completed_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (task_id),
    CONSTRAINT fk_workflow_task_workflow FOREIGN KEY (workflow_id) REFERENCES workflow(workflow_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notification (
    notification_id BINARY(16) NOT NULL,
    recipient_id BINARY(16) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    read_at DATETIME,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (notification_id),
    INDEX idx_notification_recipient (recipient_id)
);
