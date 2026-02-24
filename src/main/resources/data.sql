-- Seed data for permissions
-- Run this manually or use as data.sql
-- Insert permissions (topic.*)
INSERT INTO auth.permissions (name, description)
VALUES ('topic.create', 'Criar tópicos'),
    ('topic.edit', 'Editar tópicos'),
    ('topic.delete', 'Deletar tópicos'),
    ('topic.pin', 'Fixar tópicos'),
    ('topic.lock', 'Trancar tópicos') ON CONFLICT DO NOTHING;
-- Insert permissions (post.*)
INSERT INTO auth.permissions (name, description)
VALUES ('post.create', 'Criar posts'),
    ('post.edit', 'Editar posts'),
    ('post.delete', 'Deletar posts') ON CONFLICT DO NOTHING;
-- Insert permissions (user.*)
INSERT INTO auth.permissions (name, description)
VALUES ('user.view', 'Ver detalhes de usuários'),
    ('user.edit', 'Editar usuários'),
    ('user.ban', 'Banir usuários'),
    (
        'user.manage_roles',
        'Gerenciar roles de usuários'
    ) ON CONFLICT DO NOTHING;
-- Assign permissions to ADMIN role (all permissions)
INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id,
    p.id
FROM auth.roles r,
    auth.permissions p
WHERE r.name = 'ADMIN' ON CONFLICT DO NOTHING;
-- Assign topic + post permissions to MOD role
INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id,
    p.id
FROM auth.roles r,
    auth.permissions p
WHERE r.name = 'MOD'
    AND (
        p.name LIKE 'topic.%'
        OR p.name LIKE 'post.%'
        OR p.name = 'user.view'
    ) ON CONFLICT DO NOTHING;
-- Assign basic permissions to USER role
INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id,
    p.id
FROM auth.roles r,
    auth.permissions p
WHERE r.name = 'USER'
    AND p.name IN ('topic.create', 'post.create') ON CONFLICT DO NOTHING;
-- Migrate existing users: assign USER role to all users that don't have any role yet
INSERT INTO auth.user_roles (user_id, role_id)
SELECT u.id,
    r.id
FROM auth.users u,
    auth.roles r
WHERE r.name = 'USER'
    AND NOT EXISTS (
        SELECT 1
        FROM auth.user_roles ur
        WHERE ur.user_id = u.id
    ) ON CONFLICT DO NOTHING;