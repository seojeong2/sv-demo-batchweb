package com.example.svbatchweb.controller;

import com.example.svbatchweb.dto.ClassificateReqDto;
import com.example.svbatchweb.dto.ClassifyResDto;
import com.example.svbatchweb.dto.TcpResDto;
import com.example.svbatchweb.service.FileService;
import com.example.svbatchweb.service.TcpClient;
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

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@CrossOrigin
public class ApiController {

    @Value("${upload.dir}")
    private String uploadDir;

    private FileService fileService;
    private VoiceClassificate voiceClassificate;

    private TcpClient tcpClient;

    public ApiController(FileService fileService, VoiceClassificate voiceClassificate, TcpClient tcpClient) {
        this.fileService = fileService;
        this.voiceClassificate = voiceClassificate;
        this.tcpClient = tcpClient;
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


    //@PostMapping("/batch/classify")
    public ResponseEntity<List<ClassifyResDto>> voiceClassify(@RequestBody String[] wavPaths) {
        log.info("call batch classify controller");

        try{
            List<ClassifyResDto> resultList = new ArrayList<>();

            for (String wavPath : wavPaths) {
                log.info("req wav path: " + wavPath);
                ClassifyResDto classifyResDto = new ClassifyResDto();

                String res = tcpClient.sendClassificationReq(wavPath); // 분류결과
                //String res = "old male";

                classifyResDto.setWavPath(wavPath);
                classifyResDto.setClassifyResult(res);

                resultList.add(classifyResDto);

            }
            log.info("final response list: " + resultList);

            return ResponseEntity.ok(resultList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }


    @PostMapping("/batch/classify")
    public ResponseEntity<String> voiceBatchClassify(@RequestParam String filePath) {
        log.info("call batch classify controller");

        try{
            ClassifyResDto classifyResDto = new ClassifyResDto();

            String res = tcpClient.sendClassificationReq(filePath);
            //String res = "OM"; // 로컬 테스트

            classifyResDto.setWavPath(filePath);
            classifyResDto.setClassifyResult(res);

            log.info("classify result: " + classifyResDto);

            return ResponseEntity.status(HttpStatus.OK).body(classifyResDto.getClassifyResult());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @PostMapping("/result/upload")
    public ResponseEntity<List<String>> uploadResultFile(@RequestParam("txtFile") MultipartFile file) {

        List<String> lines = new ArrayList<>();
        try ( InputStream inputStream = file.getInputStream();
              InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
              BufferedReader br = new BufferedReader(inputStreamReader);
        ) {

            String line;
            while ((line = br.readLine()) != null) {
//                log.info("Read line: " + line);
                lines.add(line);
            }

            return ResponseEntity.status(HttpStatus.OK).body(lines);

        } catch (IOException e ){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }


    @GetMapping("/server/status")
    public String getServerStatus() {

        String ip = "43.202.10.88";
        int port = 9101;

        boolean isServerAvailable = isServerReachable(ip,port);

        if (isServerAvailable) {
            return "ON";
        } else {
            return "OFF";
        }

    }

    private boolean isServerReachable(String ip, int port) {
        try(
                Socket socket = new Socket();
        ) {
            socket.connect(new InetSocketAddress(ip,port),1000);
            return true;

        } catch (IOException e) {
            return false;
        }
    }


}
