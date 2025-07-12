
INSERT INTO users (id, name, email, password, created_on, updated_on)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Alice', 'alice@example.com', '{bcrypt}$2a$10$ken5PA2PuuKWIpGpwATPXuoOD9JOGxVUPEGXU6af3mAqOOvlX.EoO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'Bob', 'bob@example.com', '{bcrypt}$2a$10$ken5PA2PuuKWIpGpwATPXuoOD9JOGxVUPEGXU6af3mAqOOvlX.EoO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO notes (user_id, title, content, created_on, updated_on,expires_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Sample Note 1', 'Content of the first note', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,null),
    ('11111111-1111-1111-1111-111111111111', 'Sample Note 2', 'Content of the second note', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,null);
