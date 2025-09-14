CREATE TABLE branch (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(128) UNIQUE NOT NULL
);


CREATE TABLE employee_profile (
                                  id SERIAL PRIMARY KEY,
                                  telegram_user_id BIGINT NOT NULL UNIQUE,
                                  role VARCHAR(32) NOT NULL CHECK (role IN ('MECHANIC','ELECTRICIAN','MANAGER')),
                                  branch_id INT NOT NULL REFERENCES branch(id),
                                  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);


CREATE TABLE feedback (
                          id SERIAL PRIMARY KEY,
                          profile_id INT NOT NULL REFERENCES employee_profile(id),
                          text TEXT NOT NULL,
                          sentiment VARCHAR(16) NOT NULL,
                          criticality SMALLINT NOT NULL,
                          resolution TEXT,
                          trello_card_id VARCHAR(64),
                          created_at TIMESTAMP NOT NULL DEFAULT NOW()
);