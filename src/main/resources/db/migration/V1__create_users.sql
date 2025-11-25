CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255) NOT NULL,

                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,
                       deleted_at TIMESTAMP,

                       CONSTRAINT users_email_unique UNIQUE(email)
);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role VARCHAR(50) NOT NULL,
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
