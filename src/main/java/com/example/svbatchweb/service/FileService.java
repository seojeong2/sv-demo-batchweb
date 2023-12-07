package com.example.svbatchweb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class FileService {

    @Value("${upload.dir}")
    private String uploadDir;

    public List<String> extractFilesFromZip(MultipartFile zipFile) throws IOException {
        List<String> extractedFiles = new ArrayList<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                String fileName = entry.getName();
                //String filePath = uploadDir + File.separator + fileName;

                // 디렉토리가 없다면 생성
                File file = new File(uploadDir,fileName);
                log.info("final file: " + file.getAbsolutePath());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {

                    // __MACOSX 폴더 제외
                    if (!fileName.startsWith("__MACOSX/")) {
                        extractedFiles.add(fileName);
                        file.getParentFile().mkdirs();
                        // 파일 복사
                        Files.copy(zipInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    }
                }
            }
        }

        return extractedFiles;


//        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {
//            ZipEntry entry;
//
//            while ((entry = zipInputStream.getNextEntry()) != null) {
//                String fileName = entry.getName();
//                String filePath = uploadDir + File.separator + fileName;
//                log.info("filePath: " + filePath);
//
//                // 디렉토리가 없다면 생성
//                File file = new File(filePath);
//                if (entry.isDirectory()) {
//                    file.mkdirs();
//                } else {
//                    extractedFiles.add(fileName);
//                    file.getParentFile().mkdirs();
//
//                    // 파일 복사
//                    Files.copy(zipInputStream, file.toPath());
//                }
//            }
//        }
//        return extractedFiles;
    }


    public boolean isZipFile(MultipartFile file) {
        return file.getOriginalFilename().toLowerCase().endsWith(".zip");
    }

    // 파일 저장 로직
    public String saveFile(MultipartFile file) throws IOException {


        String fileName = file.getOriginalFilename();
        //String filePath = uploadDir + File.separator + fileName;
        String filePath = Paths.get(uploadDir, fileName).toString();

        log.info("filePath: " + filePath);

        // 파일을 업로드 디렉토리에 저장
        Path destinationPath = new File(filePath).toPath();
        Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        return filePath;
    }
}
