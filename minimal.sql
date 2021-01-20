-- Root user, not meant to log in
INSERT INTO verification_challenge VALUES(
  'root_challenge',  -- verification_challenge_key_hash
  1, -- creation_time
  'root', -- name
  'root@example.com', -- email
  '$2a$10$kteCMggOjaT1lJWybiFwMewtFvec7QB35lo6Rjk7IjNJFVJBoyDQ.' -- password_hash
);

INSERT INTO user VALUES(
  0, -- user_id
  1, -- creation_time
  'root',  -- name
  'root@example.com',  -- email
  'root_challenge' -- verification_challenge_key_hash
);

INSERT INTO subscription VALUES(
  0, -- subscription_id
  1, -- creation_time
  0, -- creator_user_id
  100000000000000, -- duration
  10 -- max_uses
);

-- The school everybody attends
INSERT INTO school VALUES(
  0, -- school_id
  1, -- creation_time
  0, -- creator_id
  'Squidward Community College', --name 
  0 -- subscription_id
);

-- Dummy location LMAO to prevent errors
INSERT INTO location VALUES(
  0, -- location_id
  1, -- creation_time
  0, -- creator_user_id
  0, -- school_id
  'Dummy Location',  -- name
  'Virtually, at Squidward Community College', -- description
  1
);

