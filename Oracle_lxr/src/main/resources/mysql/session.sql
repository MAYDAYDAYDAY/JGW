SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
use jgw;
-- ----------------------------
-- Table structure for session
-- ----------------------------
DROP TABLE IF EXISTS `session`;
CREATE TABLE `session`  (
  `sessionID` int(10) NOT NULL AUTO_INCREMENT COMMENT 'sessionID',
  `userID` int(10) NOT NULL   COMMENT '�û�id',
  `startTime` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '��ʼʱ��',
  PRIMARY KEY (`sessionID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '�Ի�' ROW_FORMAT = DYNAMIC;

insert into session values(1,1,'2024-01-19 07:46:20');