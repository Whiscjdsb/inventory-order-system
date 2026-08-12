CREATE INDEX idx_order_status_create_time
    ON sys_order (status, create_time);