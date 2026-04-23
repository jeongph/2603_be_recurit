CREATE TABLE subscription (
    phone_number VARCHAR(16) NOT NULL,
    state        VARCHAR(16) NOT NULL,
    version      BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE subscription_event (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    phone_number VARCHAR(16)  NOT NULL,
    state_from   VARCHAR(16)  NOT NULL,
    state_to     VARCHAR(16)  NOT NULL,
    channel_id   BIGINT       NOT NULL,
    operation    VARCHAR(16)  NOT NULL,
    outcome      VARCHAR(16)  NOT NULL,
    -- DATETIME 은 세션 타임존에 영향받지 않고 있는 그대로 저장. 저장 규약은 UTC.
    occurred_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_event_phone_occurred (phone_number, occurred_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
