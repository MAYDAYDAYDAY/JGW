package com.example.controller;

import com.example.common.Result;
import com.example.common.config.JgwRuntimeConfig;
import com.example.entity.CharTable;
import com.example.service.CharTableFormerService;
import com.example.service.CharTableService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@RestController
public class PredictController {
    @Resource
    private CharTableFormerService charTableFormerService;

    @Resource
    private CharTableService charTableService;

    @Resource
    private JgwRuntimeConfig jgw;

    @PostMapping("/predict")
    public Result predict(String base64Image) {
        try {
            String base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            long timeStamp = System.currentTimeMillis();
            Path destinationFile = jgw.getPredictUploadDir().resolve(timeStamp + ".png");
            Files.write(destinationFile, decodedBytes);

            String[] cmd = {
                    jgw.getPythonExe(),
                    jgw.getShibieScript().toString(),
                    destinationFile.toString(),
                    jgw.getShibieModel().toString()
            };
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(jgw.getShibieScript().getParent().toFile());
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            int ID = -1;
            try {
                Process process = pb.start();
                int f = process.waitFor();

                if (f == 0) {
                    try (BufferedReader in = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                        String str = in.readLine();
                        if (str != null) {
                            ID = Integer.parseInt(str.trim());
                            System.out.println("ID:" + ID);
                        }
                    }
                } else {
                    System.out.println("predict subprocess exit=" + f);
                    return Result.error();
                }
            } catch (Exception e) {
                System.out.println("predict error");
                e.printStackTrace();
                return Result.error();
            }
            System.out.println("ID:" + ID);
            CharTable c1 = charTableFormerService.selectByID(ID);
            CharTable c2 = charTableService.selectByChar(c1.getsWord());
            return Result.success(c2);
        } catch (Exception e) {
            System.out.println("predict outer error");
            e.printStackTrace();
            return Result.error();
        }
    }

    @PostMapping("/generate")
    public Result generate(String base64Image) throws IOException {
        String base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        long timeStamp = System.currentTimeMillis();
        Path uploadImagePath = jgw.getGenerateUploadDir().resolve(timeStamp + ".jpg");
        Files.write(uploadImagePath, decodedBytes);

        Path script = jgw.getDiffusionMain();
        Path reference = jgw.getDiffusionReference();
        String[] cmd = {
                jgw.getPythonExe(),
                script.toString(),
                uploadImagePath.toString(),
                reference.toString()
        };
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(script.getParent().toFile());
        pb.environment().put("PYTHONIOENCODING", "utf-8");

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            Files.deleteIfExists(uploadImagePath);
            if (exitCode != 0) {
                return Result.error();
            }

            StringBuilder output = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            String[] lines = output.toString().split(System.lineSeparator());
            if (lines.length == 0) {
                return Result.error();
            }
            String lastLine = lines[lines.length - 1].trim();

            File file = new File(lastLine);
            if (!file.exists()) {
                return Result.error();
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return Result.success(base64);
        } catch (Exception e) {
            System.out.println("generate error");
            e.printStackTrace();
            return Result.error();
        }
    }
}
