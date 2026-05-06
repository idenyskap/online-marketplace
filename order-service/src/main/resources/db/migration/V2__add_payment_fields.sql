ALTER TABLE orders
    ADD COLUMN payment_session_id VARCHAR(255),
    ADD COLUMN payment_intent_id VARCHAR(255);