-- remove duplicate name
DELETE from email_domain where name = '%layobservers.co.uk';
INSERT INTO email_domain (email_domain_id, name, description)
VALUES  (newid(), '%layobservers.co.uk', 'LAYOBSERVERS');

ALTER TABLE email_domain ADD CONSTRAINT email_domain_name_unique UNIQUE (name);