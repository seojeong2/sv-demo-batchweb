package com.example.svbatchweb.controller;

import com.example.svbatchweb.dto.ClassificateReqDto;
import com.example.svbatchweb.service.FileService;
import com.example.svbatchweb.service.VoiceClassificate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@Slf4j
@CrossOrigin
public class ApiController {

    @Value("${upload.dir}")
    private String uploadDir;

    private FileService fileService;
    private VoiceClassificate voiceClassificate;

    public ApiController(FileService fileService, VoiceClassificate voiceClassificate) {
        this.fileService = fileService;
        this.voiceClassificate = voiceClassificate;
    }

    // 배치테스트
    @PostMapping("/file-upload")
    public ResponseEntity<ClassificateReqDto> wavFileUpload(@RequestParam("file") MultipartFile uploadedFile) {
        log.info("file upload api call");

        try {
//            if (uploadedFile.isEmpty()) {
//                return new ResponseEntity<>("Please select a file to upload.", HttpStatus.BAD_REQUEST);
//            }

            // 디렉토리가 없다면 생성
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            ClassificateReqDto classificateReqDto = new ClassificateReqDto();


            // 업로드된 파일이 ZIP인지 WAV인지 확인
            if (fileService.isZipFile(uploadedFile)) {
                // ZIP 파일 처리
                List<String> extractedFiles = fileService.extractFilesFromZip(uploadedFile);

                // 개별 파일에 대한 TCP 요청
                for (String fileName : extractedFiles) {
                   // sendTcpRequest(new File(uploadDirectory, fileName));
                    log.info("fileName in list: " + fileName);
                    classificateReqDto.getWavPathList().add(uploadDir + File.separator+ fileName);
                }

            } else {
                // WAV 파일 처리
                File savedFile = new File(uploadDirectory, uploadedFile.getOriginalFilename());
                uploadedFile.transferTo(savedFile);
                //sendTcpRequest(savedFile);

                if (savedFile.exists()) {
                    log.info("File Saved successfully: {}", savedFile.getAbsolutePath());
                    classificateReqDto.getWavPathList().add(savedFile.getAbsolutePath());
                } else {
                    log.error("File Saving Failed");
                }
            }

            return new ResponseEntity<>(classificateReqDto, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }




//
//        try{
//            if (fileService.isZipFile(file)) {
//                log.info("this is zip");
//                List<String> path = fileService.extractFiles(file);
//
//                return ResponseEntity.status(HttpStatus.OK).body("ZIP file uploaded and extracted successfully!");
//            } else {
//                log.info("this is wav");
//                fileService.saveFile(file);
//                return ResponseEntity.status(HttpStatus.OK).body("WAV File uploaded successfully!");
//            }
//
//            // 파일 저장
////            String fileName = uploadDirectory.getAbsolutePath() + File.separator + file.getOriginalFilename();
////            log.info("fileName: " + fileName);
////
////            file.transferTo(new File(fileName));
//
//        } catch(IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File Upload Failed");
//        }
    }


    //@PostMapping("/real-time/upload")
    public ResponseEntity<String> handleAudioUpload2(@RequestParam("file") MultipartFile audioFile) {

        log.info("call real-time audio controller");
        log.info("audioFile.name: " + audioFile.getOriginalFilename());
        log.info("audioFile.size: " + audioFile.getSize());

        try {
            if (audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select an audio file to upload.");
            }

            // 디렉토리가 없다면 생성
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            // 파일 저장 경로
            Path filePath = Path.of(uploadDir, audioFile.getOriginalFilename());

            // 파일 복사
            Files.copy(audioFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok("Audio file uploaded successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload audio file: " + e.getMessage());
        }
    }


    @PostMapping("/upload")
    public ResponseEntity<String> handleAudioUpload(@RequestParam("file") MultipartFile audioFile) {

        log.info("call wav upload controller");
        log.info("filename: " + audioFile.getOriginalFilename());
        log.info("filesize: " + audioFile.getSize());
        try {
            if (audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select an audio file to upload.");
            }

            // 디렉토리가 없다면 생성
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            // 파일 저장 경로
            Path filePath = Path.of(uploadDir, audioFile.getOriginalFilename());

            // 파일 복사
            Files.copy(audioFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok("Audio file uploaded successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload audio file: " + e.getMessage());
        }
    }

}
