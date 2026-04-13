SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
use jgw;
-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `messageID` int(10) NOT NULL AUTO_INCREMENT COMMENT 'messageID',
  `sessionID` int(10) NOT NULL   COMMENT '对话id',
  `content` varchar(6000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
  `who` int(10) NOT NULL   COMMENT '发送者',
   `timePoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息产生的时间',
  PRIMARY KEY (`messageID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息' ROW_FORMAT = DYNAMIC;

insert into message values(1,1,'你好',1,'2024-01-19 07:46:20');
insert into message values(2,1,'你好,你好',0,'2024-01-19 07:46:21');
