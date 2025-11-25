CREATE TABLE device_sessions (
                                 id UUID PRIMARY KEY,
                                 user_id UUID NOT NULL,

                                 user_agent TEXT,
                                 ip_address TEXT,
                                 device_name TEXT,
                                 os TEXT,
                                 last_used_at TIMESTAMP,
                                 revoked BOOLEAN NOT NULL,

                                 created_at TIMESTAMP NOT NULL,
                                 updated_at TIMESTAMP NOT NULL,
                                 deleted_at TIMESTAMP,

                                 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

