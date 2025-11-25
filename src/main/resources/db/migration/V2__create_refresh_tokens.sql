CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                user_id UUID NOT NULL,
                                token_hash VARCHAR(255) NOT NULL UNIQUE,
                                expiry TIMESTAMP NOT NULL,
                                revoked BOOLEAN NOT NULL,

                                created_at TIMESTAMP NOT NULL,
                                updated_at TIMESTAMP NOT NULL,
                                deleted_at TIMESTAMP,

                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
