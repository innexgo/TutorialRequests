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


-- The school everybody attends
INSERT INTO school VALUES(0, 1, 0, 'Squidward Community College', 'sqcc');

INSERT INTO user VALUES(1,1,1,'BOB JOHNSON',      'bob@example.com',     1, '$2a$10$kteCMggOjaT1lJWybiFwMewtFvec7QB35lo6Rjk7IjNJFVJBoyDQ.');
INSERT INTO user VALUES(2,1,1,'SARAH DOE',        'sarah@example.com',   1, '$2a$10$kteCMggOjaT1lJWybiFwMewtFvec7QB35lo6Rjk7IjNJFVJBoyDQ.');
INSERT INTO user VALUES(3,1,1,'JOE SMITH',        'joe@example.com',     1, '$2a$10$kteCMggOjaT1lJWybiFwMewtFvec7QB35lo6Rjk7IjNJFVJBoyDQ.');
INSERT INTO user VALUES(4,1,0,'ALICE BROWN',      'alice@example.com',   1, '$2a$10$kteCMggOjaT1lJWybiFwMewtFvec7QB35lo6Rjk7IjNJFVJBoyDQ.');
INSERT INTO user VALUES(5,1,0,'BILLY FLETCHER',   'billy@example.com',   1, '$2a$10$kteCMggOjaT1lJWybiFwMewtFvec7QB35lo6Rjk7IjNJFVJBoyDQ.');
INSERT INTO user VALUES(6,1,0,'CARSON WILSON',    'carson@example.com',  1, '$2a$10$kteCMggOjaT1lJWybiFwMewtFvec7QB35lo6Rjk7IjNJFVJBoyDQ.');
INSERT INTO user VALUES(7,1,0,'GEORGE OHARE',     'george@example.com',  1, '$2a$10$kteCMggOjaT1lJWybiFwMewtFvec7QB35lo6Rjk7IjNJFVJBoyDQ.');
INSERT INTO user VALUES(8,1,0,'WILLIAM DOE',      'william@example.com', 1, '$2a$10$kteCMggOjaT1lJWybiFwMewtFvec7QB35lo6Rjk7IjNJFVJBoyDQ.');
INSERT INTO user VALUES(9,1,0,'ROBERT MCPHILLIP', 'robert@example.com',  1, '$2a$10$kteCMggOjaT1lJWybiFwMewtFvec7QB35lo6Rjk7IjNJFVJBoyDQ.');



-- Dummy location LMAO to prevent errors
INSERT INTO location VALUES(
  0, -- location_id
  1, -- creation_time
  0, -- creator_user_id
  0, -- school_id
  'Dummy Location',  -- name
  'Virtually, at Squidward Community College', -- description
  true
);


-- A different school that we don't want to accidentally see showing up on any query
INSERT INTO school VALUES(
  1, -- school_id
  1, -- creation_time
  0, -- creator_user_id
  'Other School (Shouldnt show up)', -- name
  'os' -- abbreviation
);


-- A student in this different school we don't want to show up at all


