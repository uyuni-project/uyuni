ALTER TABLE rhnchannel ADD COLUMN IF NOT EXISTS
    auto_sync boolean default true not null;