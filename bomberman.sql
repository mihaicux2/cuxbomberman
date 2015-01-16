CREATE DATABASE `bomberman`;

GRANT ALL PRIVILEGES ON `bomberman`.* TO 'bomberman_admin'@'%' IDENTIFIED BY 'bomberman_password';

USE `bomberman`;

CREATE TABLE `chat_message`
( `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `room_nr` int(11) NOT NULL,
  `message_time` DATETIME NOT NULL,
  `message` TEXT NOT NULL,
   PRIMARY KEY (`id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `characters`
( `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `name` varchar(128) NOT NULL,
  `speed` tinyint(2) NOT NULL,
  `bomb_range` tinyint(2) NOT NULL,
  `max_bombs` tinyint(3) NOT NULL,
  `triggered` tinyint(1) NOT NULL DEFAULT 0,
  `kills` int(11) NOT NULL DEFAULT 0,
  `deaths` int(11) NOT NULL DEFAULT 0,
  `creation_time` DATETIME NULL,
  `modification_time` DATETIME NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user`
( `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(256) NOT NULL,
  `username` varchar(256) NOT NULL,
  `password` varchar(256) NOT NULL,
  `registered_at` DATETIME NULL,
  `last_login` DATETIME NULL,
  `admin` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `login_history` (
 `id` int(11) NOT NULL AUTO_INCREMENT,
 `user_id` int(11) NOT NULL,
 `ip` varchar(255) NOT NULL,
 `login_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `user` (`id`, `email`, `username`, `password`, `admin`, `registered_at`) VALUES
(NULL, 'admin1@cuxbomberman.localhost', 'bombermanadmin', MD5('bomberman'), 1, NOW());

CREATE TABLE `banlist`
( `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_ip` varchar(128) NOT NULL DEFAULT '',
  `admin_id` int(11) NOT NULL, 
  `ban_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT  CHARSET=utf8;
