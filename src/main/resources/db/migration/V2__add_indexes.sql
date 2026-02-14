CREATE INDEX idx_orders_patient ON orders(patient_id);
CREATE INDEX idx_orders_type ON orders(type);
CREATE INDEX idx_study_status ON study(status);
CREATE INDEX idx_result_order ON order_result(order_id);
CREATE INDEX idx_result_current ON order_result(is_current);
