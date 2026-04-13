package com.example.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 解析甲骨文项目根目录：与 Oracle_lxr、scripts 同级的文件夹。
 */
public final class JgwPaths {

    private JgwPaths() {
    }

    /**
     * @param configured application 中配置的 jgw.home；为空则取 user.dir 的上一级（在 Oracle_lxr 下启动时一般为 …/jgw/jgw）
     */
    public static Path resolveHome(String configured) {
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured).toAbsolutePath().normalize();
        }
        Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path parent = cwd.getParent();
        return parent != null ? parent : cwd;
    }
}
