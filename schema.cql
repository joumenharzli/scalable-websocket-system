CREATE KEYSPACE "app" WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1};

CREATE TABLE app.notifications (
   id uuid,
   content text,
   userId uuid,
   seen boolean,
   created_at timestamp,
   PRIMARY KEY (id));

CREATE INDEX notifications_user_index ON app.notifications (userId);
