package com.example.svbatchweb.service;

import com.example.svbatchweb.dto.TcpResDto;
import com.example.svbatchweb.exception.ResException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;

@Service
@Slf4j
public class TcpClient {

    public String sendClassificationReq(String reqWavPath) {
        String serverHost = "43.202.10.41";
        int serverPort = 9101;

        log.info("ip: " + serverHost +" port: " + serverPort);

        try(
                Socket socket = new Socket(serverHost, serverPort);
                OutputStream out = socket.getOutputStream();
        ) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("wavPath",reqWavPath);

            String jsonString = jsonObj.toString();
            log.info("req json string: " + jsonString);

            String apiCode = "2000"; // 분류 고정
            String resultCode = "0000"; // 고정
            String bodySize = String.format("%08d", jsonString.length());

            String header = apiCode + resultCode + bodySize;

            // 전체 요청 메시지 생성
            String requestMsg = header + jsonString;
            System.out.println("req string: " + requestMsg);

            // 요청 전송
            out.write(requestMsg.getBytes());

            InputStream in = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String response = br.readLine();
            System.out.println("response: " + response);

            TcpResDto tcpResDto = new TcpResDto();

            String responseApiCode = response.substring(0, 4);
            String responseResultCode = response.substring(4, 8);
            String responseBodySize = response.substring(8, 16);
            String responseBody = response.substring(16);

            tcpResDto.setApiCode(responseApiCode);
            tcpResDto.setResultCode(responseResultCode);
            tcpResDto.setResponseBodySize(responseBodySize);
            tcpResDto.setResult(responseBody);

            log.info("resApiCode: " + tcpResDto.getApiCode());
            log.info("resResultCode: " + tcpResDto.getResultCode());
            log.info("resBodySize: " + tcpResDto.getResponseBodySize());
            log.info("resBody: " + tcpResDto.getResult());

            socket.close();
            out.close();
            in.close();
            jsonObj.clear();

            return tcpResDto.getResult();


        } catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IO 예외 발생: " + e.getMessage());

        }

    }
}
