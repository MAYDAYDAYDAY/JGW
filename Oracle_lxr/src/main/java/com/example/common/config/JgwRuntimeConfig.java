package com.example.common.config;

import com.example.utils.JgwPaths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Python 解释器路径与脚本、模型、上传目录；可通过 application.yml 或环境变量覆盖。
 * 环境变量示例：JGW_HOME、JGW_PYTHON（需在启动前导出，或在 yml 中用 ${JGW_HOME}）
 */
@Component
public class JgwRuntimeConfig {

    @Value("${jgw.home:}")
    private String homeConfig;

    @Value("${jgw.python-exe:python}")
    private String pythonExe;

    @Value("${jgw.shibie-script:}")
    private String shibieScriptConfig;

    @Value("${jgw.shibie-model:}")
    private String shibieModelConfig;

    @Value("${jgw.diffusion-main:}")
    private String diffusionMainConfig;

    @Value("${jgw.diffusion-reference:}")
    private String diffusionReferenceConfig;

    @Value("${jgw.mail-sender-script:}")
    private String mailSenderScriptConfig;

    private Path jgwHome;
    private Path predictUploadDir;
    private Path generateUploadDir;
    private Path shibieScript;
    private Path shibieModel;
    private Path diffusionMain;
    private Path diffusionReference;
    private Path mailSenderScript;

    @PostConstruct
    public void init() throws IOException {
        jgwHome = JgwPaths.resolveHome(homeConfig);
        predictUploadDir = jgwHome.resolve("upload_imgs");
        generateUploadDir = jgwHome.resolve("scripts/diffusion/test_data_dir");
        shibieScript = resolveOptional(shibieScriptConfig, jgwHome.resolve("former/shibie.py"));
        shibieModel = resolveOptional(shibieModelConfig, jgwHome.resolve("former/mbnet.pkl"));
        diffusionMain = resolveOptional(diffusionMainConfig, jgwHome.resolve("scripts/diffusion/main.py"));
        diffusionReference = resolveOptional(diffusionReferenceConfig, jgwHome.resolve("scripts/diffusion/example_kaishu.png"));
        mailSenderScript = resolveOptional(mailSenderScriptConfig, jgwHome.resolve("scripts/mailSender.py"));
        Files.createDirectories(predictUploadDir);
        Files.createDirectories(generateUploadDir);
        System.out.println("[jgw] home=" + jgwHome.toAbsolutePath());
        System.out.println("[jgw] python=" + pythonExe);
    }

    private static Path resolveOptional(String configured, Path defaultPath) {
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured).toAbsolutePath().normalize();
        }
        return defaultPath;
    }

    public String getPythonExe() {
        return pythonExe;
    }

    public Path getJgwHome() {
        return jgwHome;
    }

    public Path getPredictUploadDir() {
        return predictUploadDir;
    }

    public Path getGenerateUploadDir() {
        return generateUploadDir;
    }

    public Path getShibieScript() {
        return shibieScript;
    }

    public Path getShibieModel() {
        return shibieModel;
    }

    public Path getDiffusionMain() {
        return diffusionMain;
    }

    public Path getDiffusionReference() {
        return diffusionReference;
    }

    public Path getMailSenderScript() {
        return mailSenderScript;
    }
}
