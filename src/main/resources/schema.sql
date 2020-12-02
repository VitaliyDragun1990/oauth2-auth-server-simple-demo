CREATE TABLE IF NOT EXISTS oauth_access_token (
    token_id VARCHAR(255) NOT NULL,
    token blob,
    authentication_id VARCHAR(255) DEFAULT NULL,
    user_name VARCHAR(255) DEFAULT NULL,
    client_id VARCHAR(255) DEFAULT NULL,
    authentication blob,
    refresh_token VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (token_id)
);

CREATE TABLE IF NOT EXISTS oauth_refresh_token (
    token_id VARCHAR(255) NOT NULL,
    token blob,
    authentication blob,
    PRIMARY KEY (token_id)
);
