SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
use jgw;
-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `pichistory`;
CREATE TABLE `pichistory`  (
  `id` int(10) NOT NULL  COMMENT 'ID',
  `picID` int(10) NOT NULL   AUTO_INCREMENT COMMENT '图片id',
  `picUrl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片链接',
   `stars` int(10) NOT NULL   COMMENT '评分',
    `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`picID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '简体甲骨对照表' ROW_FORMAT = DYNAMIC;

