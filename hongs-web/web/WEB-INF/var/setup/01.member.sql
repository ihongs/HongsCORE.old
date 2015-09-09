-- DB: member

--
-- 部门
--

DROP TABLE IF EXISTS `a_member_dept`;
CREATE TABLE `a_member_dept` (
  `id` char(20) NOT NULL,
  `pid` char(20) DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `note` text,
  `ctime` int(11) DEFAULT NULL,
  `mtime` int(11) DEFAULT NULL,
  `rtime` int(11) DEFAULT NULL,
  `state` tinyint(4) DEFAULT '1',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`pid`) REFERENCES `a_member_dept` (`id`) ON DELETE CASCADE
);

CREATE INDEX `IK_a_member_dept_dept` ON `a_member_dept` (`pid`);
CREATE INDEX `IK_a_member_dept_state` ON `a_member_dept` (`state`);
CREATE INDEX `IK_a_member_dept_ctime` ON `a_member_dept` (`ctime`);
CREATE INDEX `IK_a_member_dept_mtime` ON `a_member_dept` (`mtime`);
CREATE UNIQUE INDEX `UK_a_member_dept_name` ON `a_member_dept` (`name`,`pid`);

INSERT INTO `a_member_dept` VALUES ('0',NULL,'ROOT','ROOT','1424075622230','1424075622230',1);
INSERT INTO `a_member_dept` VALUES ('HXSDROLE001REB0Q01','0','技术部','这是技术部','1424075622230','1424075622230',1);
INSERT INTO `a_member_dept` VALUES ('HYPRZ8Q5006II04J01','0','市场部','这是市场部','1424075622230','1424075622230',1);
INSERT INTO `a_member_dept` VALUES ('HY9XXIS5000T3DD501','HXSDROLE001REB0Q01','研发部','','1424075622230','1424075622230',1);
INSERT INTO `a_member_dept` VALUES ('HYPR7S3N00BWKOZ001','HXSDROLE001REB0Q01','运维部','','1424075622230','1424075622230',1);
INSERT INTO `a_member_dept` VALUES ('HYPS1ROT007T1AG601','HYPRZ8Q5006II04J01','产品部','','1424075622230','1424075622230',1);

--
-- 部门所属角色
--

DROP TABLE IF EXISTS `a_member_dept_role`;
CREATE TABLE `a_member_dept_role` (
  `dept_id` char(20) NOT NULL,
  `role` char(100) NOT NULL,
  PRIMARY KEY (`dept_id`,`role`),
  FOREIGN KEY (`dept_id`) REFERENCES `a_member_dept` (`id`)
);

CREATE INDEX `IK_a_member_dept_role_dept` ON `a_member_dept_role` (`dept_id`);
CREATE INDEX `IK_a_member_dept_role_role` ON `a_member_dept_role` (`role`);

--
-- 用户
--

DROP TABLE IF EXISTS `a_member_user`;
CREATE TABLE `a_member_user` (
  `id` char(20) NOT NULL,
  `name` varchar(200) NOT NULL,
  `note` text,
  `username` varchar(200) DEFAULT NULL,
  `password` varchar(200) DEFAULT NULL,
  `ctime` int(11) DEFAULT NULL,
  `mtime` int(11) DEFAULT NULL,
  `rtime` int(11) DEFAULT NULL,
  `state` tinyint(4) DEFAULT '1',
  PRIMARY KEY (`id`)
);

CREATE INDEX `IK_a_member_user_ctime` ON `a_member_user` (`ctime`);
CREATE INDEX `IK_a_member_user_mtime` ON `a_member_user` (`mtime`);
CREATE INDEX `IK_a_member_user_state` ON `a_member_user` (`state`);
CREATE INDEX `IK_a_member_user_username` ON `a_member_user` (`username`);
CREATE UNIQUE INDEX `UK_a_member_user_username` ON `a_member_user` (`username`);

INSERT INTO `a_member_user` VALUES ('1','老大 (管理员)',NULL,'abc@def.cn','9BA587D4E465F45669F19AF20CA033D9','1424075622230','1424075622230',1);
INSERT INTO `a_member_user` VALUES ('01I2ODRZHR00KLJOEM','张三 (总经理)',NULL,'a@abc.com','9BA587D4E465F45669F19AF20CA033D9','1424075622230','1424075622230',1);
INSERT INTO `a_member_user` VALUES ('01I2ODSOGC00KGZCQK','李四 (技术总监)',NULL,'b@abc.com','9BA587D4E465F45669F19AF20CA033D9','1424075622230','1424075622230',1);
INSERT INTO `a_member_user` VALUES ('HXNZ0OLR00297H9H01','王五 (市场总监)',NULL,'c@abc.com','9BA587D4E465F45669F19AF20CA033D9','1424075622230','1424075622230',1);
INSERT INTO `a_member_user` VALUES ('HXZGVRBH000XHB0601','赵六 (研发主管)',NULL,'d@abc.com','9BA587D4E465F45669F19AF20CA033D9','1424075622230','1424075622230',1);
INSERT INTO `a_member_user` VALUES ('HXZGWPZV002I1J1601','钱七 (运维主管)',NULL,'e@abc.com','9BA587D4E465F45669F19AF20CA033D9','1424075622230','1424075622230',1);
INSERT INTO `a_member_user` VALUES ('HY9XQN2L000WGH9Q01','孙八 (产品总监)',NULL,'f@abc.com','9BA587D4E465F45669F19AF20CA033D9','1424075622230','1424075622230',1);

--
-- 用户所属部门
--

DROP TABLE IF EXISTS `a_member_user_dept`;
CREATE TABLE `a_member_user_dept` (
  `user_id` char(20) NOT NULL,
  `dept_id` char(20) NOT NULL,
  PRIMARY KEY (`user_id`,`dept_id`),
  FOREIGN KEY (`user_id`) REFERENCES `a_member_user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`dept_id`) REFERENCES `a_member_dept` (`id`) ON DELETE CASCADE
);

CREATE INDEX `IK_a_member_user_dept_user` ON `a_member_user_dept` (`user_id`);
CREATE INDEX `IK_a_member_user_dept_dept` ON `a_member_user_dept` (`dept_id`);

INSERT INTO `a_member_user_dept` VALUES ('1','0');
INSERT INTO `a_member_user_dept` VALUES ('01I2ODRZHR00KLJOEM','0');
INSERT INTO `a_member_user_dept` VALUES ('01I2ODSOGC00KGZCQK','HXSDROLE001REB0Q01');
INSERT INTO `a_member_user_dept` VALUES ('HXNZ0OLR00297H9H01','HYPRZ8Q5006II04J01');
INSERT INTO `a_member_user_dept` VALUES ('HXZGVRBH000XHB0601','HY9XXIS5000T3DD501');
INSERT INTO `a_member_user_dept` VALUES ('HXZGWPZV002I1J1601','HYPR7S3N00BWKOZ001');
INSERT INTO `a_member_user_dept` VALUES ('HY9XQN2L000WGH9Q01','HYPS1ROT007T1AG601');

--
-- 用户所属角色
--

DROP TABLE IF EXISTS `a_member_user_role`;
CREATE TABLE `a_member_user_role` (
  `user_id` char(20) NOT NULL,
  `role` char(100) NOT NULL,
  PRIMARY KEY (`user_id`,`role`),
  FOREIGN KEY (`user_id`) REFERENCES `a_member_user` (`id`) ON DELETE CASCADE
);

CREATE INDEX `IK_a_member_user_role_user` ON `a_member_user_role` (`user_id`);
CREATE INDEX `IK_a_member_user_role_role` ON `a_member_user_role` (`role`);

INSERT INTO `a_member_user_role` VALUES ('1','hongs/member/user/retrieve');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/member/user/create');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/member/user/update');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/member/user/delete');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/member/dept/retrieve');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/member/dept/create');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/member/dept/update');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/member/dept/delete');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/module/form/retrieve');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/module/form/create');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/module/form/update');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/module/form/delete');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/module/unit/retrieve');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/module/unit/create');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/module/unit/update');
INSERT INTO `a_member_user_role` VALUES ('1','hongs/module/unit/delete');

--
-- 用户登录凭证
--

DROP TABLE IF EXISTS `a_member_user_sign`;
CREATE TABLE `a_member_user_sign` (
  `user_id` char(20) NOT NULL,
  `type` char(20) NOT NULL,
  `code` char(20) NOT NULL,
  `ctime` int(11) DEFAULT NULL,
  PRIMARY KEY (`user_id`,`type`),
  FOREIGN KEY (`user_id`) REFERENCES `a_member_user` (`id`) ON DELETE CASCADE
);

CREATE INDEX `IK_a_member_user_sign_user` ON `a_member_user_dept` (`user_id`);
CREATE INDEX `IK_a_member_user_sign_type` ON `a_member_user_dept` (`type`);
CREATE INDEX `IK_a_member_user_sign_code` ON `a_member_user_dept` (`code`);
