CREATE TABLE conversations (
    id                    BIGSERIAL PRIMARY KEY,
    buyer_id              BIGINT NOT NULL,
    seller_id             BIGINT NOT NULL,
    listing_id            VARCHAR(255) NOT NULL,
    last_seller_reply_at  TIMESTAMP WITH TIME ZONE,
    last_activity_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_conversation_triplet UNIQUE (buyer_id, seller_id, listing_id)
);

CREATE INDEX idx_conversations_buyer_created ON conversations(buyer_id, created_at);


CREATE TABLE messages (
    id               BIGSERIAL PRIMARY KEY,
    conversation_id  BIGINT NOT NULL,
    sender_id        BIGINT NOT NULL,
    body             VARCHAR(1000) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id)
);

CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at);
CREATE INDEX idx_messages_sender_created ON messages(sender_id, created_at);


CREATE TABLE block_list (
    id          BIGSERIAL PRIMARY KEY,
    blocker_id  BIGINT NOT NULL,
    blocked_id  BIGINT NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_block_pair UNIQUE (blocker_id, blocked_id)
);


CREATE TABLE user_reports (
    id               BIGSERIAL PRIMARY KEY,
    reporter_id      BIGINT NOT NULL,
    reported_id      BIGINT NOT NULL,
    conversation_id  BIGINT,
    reason           VARCHAR(500) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_reports_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id)
);

CREATE INDEX idx_user_reports_reported_created ON user_reports(reported_id, created_at);


CREATE TABLE user_stats (
    user_id                  BIGINT PRIMARY KEY,
    registered_at            TIMESTAMP WITH TIME ZONE,
    email_verified           BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified           BOOLEAN NOT NULL DEFAULT FALSE,
    completed_deals_buyer    INT NOT NULL DEFAULT 0,
    completed_deals_seller   INT NOT NULL DEFAULT 0,
    successful_interactions  INT NOT NULL DEFAULT 0,
    reports_received         INT NOT NULL DEFAULT 0,
    total_interactions       INT NOT NULL DEFAULT 0,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
