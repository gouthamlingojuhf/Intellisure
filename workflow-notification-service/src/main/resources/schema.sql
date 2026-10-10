CREATE TABLE IF NOT EXISTS workflow (
    workflow_id BINARY(16) NOT NULL,
    workflow_type VARCHAR(100) NOT NULL,
    reference_id BINARY(16),
    reference_type VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    current_step VARCHAR(100),
    initiated_by VARCHAR(255),
    started_at DATETIME,
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (workflow_id),
    INDEX idx_workflow_reference (reference_id, reference_type),
    INDEX idx_workflow_status (status)
);

CREATE TABLE IF NOT EXISTS workflow_task (
    task_id BINARY(16) NOT NULL,
    workflow_id BINARY(16) NOT NULL,
    task_type VARCHAR(100) NOT NULL,
    assignee_user_id BINARY(16),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    due_at DATETIME,
    outcome VARCHAR(255),
    completion_note VARCHAR(500),
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (task_id),
    CONSTRAINT fk_workflow_task_workflow FOREIGN KEY (workflow_id) REFERENCES workflow(workflow_id) ON DELETE CASCADE,
    INDEX idx_workflow_task_workflow (workflow_id),
    INDEX idx_workflow_task_assignee (assignee_user_id),
    INDEX idx_workflow_task_status (status),
    INDEX idx_workflow_task_due (due_at)
);

CREATE TABLE IF NOT EXISTS notification (
    notification_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    reference_type VARCHAR(100),
    reference_id BINARY(16),
    `read` BOOLEAN NOT NULL DEFAULT FALSE,
    channel VARCHAR(50) NOT NULL DEFAULT 'IN_APP',
    read_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id),
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_user_read (user_id, `read`),
    INDEX idx_notification_reference (reference_type, reference_id),
    INDEX idx_notification_created (created_at)
);

CREATE TABLE IF NOT EXISTS chat_channel (
    channel_id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    channel_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BINARY(16),
    created_by BINARY(16) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (channel_id),
    INDEX idx_chat_channel_entity (entity_type, entity_id),
    INDEX idx_chat_channel_type (channel_type)
);

CREATE TABLE IF NOT EXISTS chat_participant (
    participant_id BINARY(16) NOT NULL,
    channel_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    role VARCHAR(50) NOT NULL,
    display_name VARCHAR(255),
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (participant_id),
    INDEX idx_chat_participant_channel (channel_id),
    INDEX idx_chat_participant_user (user_id)
);

CREATE TABLE IF NOT EXISTS chat_message (
    message_id BINARY(16) NOT NULL,
    channel_id BINARY(16) NOT NULL,
    sender_id BINARY(16) NOT NULL,
    sender_name VARCHAR(255) NOT NULL,
    sender_role VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id),
    INDEX idx_chat_message_channel (channel_id),
    INDEX idx_chat_message_channel_created (channel_id, created_at)
);