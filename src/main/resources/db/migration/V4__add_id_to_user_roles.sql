ALTER TABLE user_roles ADD COLUMN IF NOT EXISTS id UUID;
UPDATE user_roles SET id = gen_random_uuid() WHERE id IS NULL;
ALTER TABLE user_roles ALTER COLUMN id SET NOT NULL;
ALTER TABLE user_roles ADD PRIMARY KEY (id);

