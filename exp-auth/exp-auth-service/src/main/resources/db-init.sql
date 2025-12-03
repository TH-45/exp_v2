-- 创建 expdb 数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `expdb` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `expdb`;

-- 注意：表结构会由 JPA 的 ddl-auto: update 自动创建
-- 这里只需要创建数据库即可

