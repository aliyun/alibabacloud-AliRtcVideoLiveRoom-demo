CREATE TABLE `channel` (
  `id` varchar(50) NOT NULL COMMENT 'id',
  `owner_id` varchar(300) DEFAULT '' COMMENT 'token',
  `cover_url` varchar(300) DEFAULT NULL,
  `title` varchar(300) DEFAULT NULL,
  `create_datetime` datetime DEFAULT NULL COMMENT '用户ID',
  `end_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
