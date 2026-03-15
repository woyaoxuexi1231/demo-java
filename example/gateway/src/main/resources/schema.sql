CREATE TABLE IF NOT EXISTS gateway_route
(
    id          VARCHAR(64) PRIMARY KEY,
    uri         VARCHAR(255) NOT NULL,
    predicates  TEXT,
    filters     TEXT,
    order_num   INT       DEFAULT 0,
    status      TINYINT   DEFAULT 1,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


INSERT INTO gateway_route (id, uri, predicates, filters, order_num, status)
VALUES ('route-user', 'http://httpbin.org:80', '[{"name":"Path","args":{"_genkey_0":"/get"}}]', '[]', 0, 1)
ON DUPLICATE KEY UPDATE update_time = CURRENT_TIMESTAMP;