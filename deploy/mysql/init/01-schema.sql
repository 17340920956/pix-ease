CREATE DATABASE IF NOT EXISTS pix_ease DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pix_ease_qa DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pix_ease;

CREATE TABLE IF NOT EXISTS pe_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_del TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    user_name VARCHAR(64) NOT NULL COMMENT '用户名称',
    account VARCHAR(8) NOT NULL COMMENT '账号（系统生成，pe+6位数字）',
    password VARCHAR(60) NOT NULL COMMENT '密码（BCrypt(SHA256(原文))加密）',
    password_version TINYINT NOT NULL DEFAULT 0 COMMENT '密码版本 0-旧版BCrypt(原文) 1-新版BCrypt(SHA256(原文))',
    email VARCHAR(128) NOT NULL COMMENT '邮箱号',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '角色标识 0-普通用户 1-管理员',
    PRIMARY KEY (id),
    UNIQUE KEY uk_account (account),
    KEY idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

USE pix_ease_qa;

CREATE TABLE IF NOT EXISTS pe_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_del TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    user_name VARCHAR(64) NOT NULL COMMENT '用户名称',
    account VARCHAR(8) NOT NULL COMMENT '账号（系统生成，pe+6位数字）',
    password VARCHAR(60) NOT NULL COMMENT '密码（BCrypt(SHA256(原文))加密）',
    password_version TINYINT NOT NULL DEFAULT 0 COMMENT '密码版本 0-旧版BCrypt(原文) 1-新版BCrypt(SHA256(原文))',
    email VARCHAR(128) NOT NULL COMMENT '邮箱号',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '角色标识 0-普通用户 1-管理员',
    PRIMARY KEY (id),
    UNIQUE KEY uk_account (account),
    KEY idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';