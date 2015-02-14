-- DB: hcum

--
-- 部门
--

DROP TABLE IF EXISTS `a_hcum_dept`;
CREATE TABLE `a_hcum_dept` (
  `id` char(20) NOT NULL,
  `pid` char(20) DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `note` text,
  `ctime` datetime DEFAULT NULL,
  `mtime` datetime DEFAULT NULL,
  `state` tinyint(4) DEFAULT '1',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`pid`) REFERENCES `a_hcum_dept` (`id`) ON DELETE CASCADE
);

CREATE INDEX `IK_a_hcum_dept_state` ON `a_hcum_dept` (`state`);
CREATE INDEX `IK_a_hcum_dept_ctime` ON `a_hcum_dept` (`ctime`);
CREATE INDEX `IK_a_hcum_dept_mtime` ON `a_hcum_dept` (`mtime`);
CREATE INDEX `IK_a_hcum_dept_dept` ON `a_hcum_dept` (`pid`);
CREATE UNIQUE INDEX `UK_a_hcum_dept_name` ON `a_hcum_dept` (`name`,`pid`);

INSERT INTO `a_hcum_dept` VALUES ('0',NULL,'ROOT','ROOT',1,'0000-00-00 00:00:00','0000-00-00 00:00:00');
INSERT INTO `a_hcum_dept` VALUES ('HXSDROLE001REB0Q01','0','技术部','这是技术部',1,'2014-07-19 11:33:06','2014-07-19 11:33:06');
INSERT INTO `a_hcum_dept` VALUES ('HYPRZ8Q5006II04J01','0','市场部','这是市场部',1,'2014-08-11 20:27:17','2014-08-11 20:27:17');
INSERT INTO `a_hcum_dept` VALUES ('HY9XXIS5000T3DD501','HXSDROLE001REB0Q01','研发部','',1,'2014-07-31 18:29:35','2014-08-11 22:17:02');
INSERT INTO `a_hcum_dept` VALUES ('HYPR7S3N00BWKOZ001','HXSDROLE001REB0Q01','运维部','',1,'2014-08-11 20:05:56','2014-08-11 20:05:56');
INSERT INTO `a_hcum_dept` VALUES ('HYPS1ROT007T1AG601','HYPRZ8Q5006II04J01','产品部','',1,'2014-08-11 20:29:15','2014-08-11 20:29:15');

--
-- 部门所属角色
--

DROP TABLE IF EXISTS `a_hcum_dept_role`;
CREATE TABLE `a_hcum_dept_role` (
  `dept_id` char(20) NOT NULL,
  `role` char(100) NOT NULL,
  PRIMARY KEY (`dept_id`,`role`),
  FOREIGN KEY (`dept_id`) REFERENCES `a_hcum_dept` (`id`)
);

CREATE INDEX `IK_a_hcum_dept_role_dept` ON `a_hcum_dept_role` (`dept_id`);
CREATE INDEX `IK_a_hcum_dept_role_role` ON `a_hcum_dept_role` (`role`);

--
-- 用户
--

DROP TABLE IF EXISTS `a_hcum_user`;
CREATE TABLE `a_hcum_user` (
  `id` char(20) NOT NULL,
  `name` varchar(200) NOT NULL,
  `note` text,
  `username` varchar(200) DEFAULT NULL,
  `password` varchar(200) DEFAULT NULL,
  `ctime` datetime DEFAULT NULL,
  `mtime` datetime DEFAULT NULL,
  `state` tinyint(4) DEFAULT '1',
  PRIMARY KEY (`id`)
);

CREATE INDEX `IK_a_hcum_user_ctime` ON `a_hcum_user` (`ctime`);
CREATE INDEX `IK_a_hcum_user_mtime` ON `a_hcum_user` (`mtime`);
CREATE INDEX `IK_a_hcum_user_state` ON `a_hcum_user` (`state`);
CREATE INDEX `IK_a_hcum_user_username` ON `a_hcum_user` (`username`);
CREATE UNIQUE INDEX `UK_a_hcum_user_username` ON `a_hcum_user` (`username`);

INSERT INTO `a_hcum_user` VALUES ('01I2ODRZHR00KLJOEM','张三 (总经理)',NULL,'a@abc.com',NULL,1,'2014-11-19 15:40:47','2014-11-25 02:14:38');
INSERT INTO `a_hcum_user` VALUES ('01I2ODSOGC00KGZCQK','李四 (技术总监)',NULL,'b@abc.com',NULL,1,'2014-11-19 15:41:19','2014-12-31 01:39:10');
INSERT INTO `a_hcum_user` VALUES ('HXNZ0OLR00297H9H01','王五 (市场总监)',NULL,'c@abc.com',NULL,1,'2014-07-16 09:29:07','2014-07-16 09:29:07');
INSERT INTO `a_hcum_user` VALUES ('HXZGVRBH000XHB0601','赵六 (研发主管)',NULL,'d@abc.com',NULL,1,'2014-07-24 10:34:38','2014-11-25 02:15:33');
INSERT INTO `a_hcum_user` VALUES ('HXZGWPZV002I1J1601','钱七 (运维主管)',NULL,'e@abc.com',NULL,1,'2014-07-24 10:35:23','2014-11-25 02:25:00');
INSERT INTO `a_hcum_user` VALUES ('HY9XQN2L000WGH9Q01','孙八 (产品总监)',NULL,'f@abc.com',NULL,1,'2014-07-31 18:24:14','2014-11-25 02:23:31');

--
-- 用户所属角色
--

DROP TABLE IF EXISTS `a_hcum_user_role`;
CREATE TABLE `a_hcum_user_role` (
  `user_id` char(20) NOT NULL,
  `role` char(100) NOT NULL,
  PRIMARY KEY (`user_id`,`role`),
  FOREIGN KEY (`user_id`) REFERENCES `a_hcum_user` (`id`) ON DELETE CASCADE
);

CREATE INDEX `IK_a_hcum_user_role_user` ON `a_hcum_user_role` (`user_id`);
CREATE INDEX `IK_a_hcum_user_role_role` ON `a_hcum_user_role` (`role`);

--
-- 用户所属部门
--

DROP TABLE IF EXISTS `a_hcum_user_dept`;
CREATE TABLE `a_hcum_user_dept` (
  `user_id` char(20) NOT NULL,
  `dept_id` char(20) NOT NULL,
  PRIMARY KEY (`user_id`,`dept_id`),
  FOREIGN KEY (`user_id`) REFERENCES `a_hcum_user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`dept_id`) REFERENCES `a_hcum_dept` (`id`) ON DELETE CASCADE
);

CREATE INDEX `IK_a_hcum_user_dept_user` ON `a_hcum_user_dept` (`user_id`);
CREATE INDEX `IK_a_hcum_user_dept_dept` ON `a_hcum_user_dept` (`dept_id`);

INSERT INTO `a_hcum_user_dept` VALUES ('01I2ODRZHR00KLJOEM','0');
INSERT INTO `a_hcum_user_dept` VALUES ('01I2ODSOGC00KGZCQK','HXSDROLE001REB0Q01');
INSERT INTO `a_hcum_user_dept` VALUES ('HXNZ0OLR00297H9H01','HYPRZ8Q5006II04J01');
INSERT INTO `a_hcum_user_dept` VALUES ('HXZGVRBH000XHB0601','HY9XXIS5000T3DD501');
INSERT INTO `a_hcum_user_dept` VALUES ('HXZGWPZV002I1J1601','HYPR7S3N00BWKOZ001');
INSERT INTO `a_hcum_user_dept` VALUES ('HY9XQN2L000WGH9Q01','HYPS1ROT007T1AG601');
