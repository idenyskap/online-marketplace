CREATE TABLE orders (
    id            BIGSERIAL PRIMARY KEY,
    buyer_id      BIGINT NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    total_amount  DECIMAL(12, 2) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id            BIGSERIAL PRIMARY KEY,
    order_id      BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id    VARCHAR(255) NOT NULL,
    product_name  VARCHAR(255) NOT NULL,
    quantity      INTEGER NOT NULL,
    price         DECIMAL(12, 2) NOT NULL
);

CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
