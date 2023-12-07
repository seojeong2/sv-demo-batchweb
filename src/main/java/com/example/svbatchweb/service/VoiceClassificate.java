package com.example.svbatchweb.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;

@Service
public class VoiceClassificate {


    public String sendClassificateReq(String wavPath) {
        String serverHost = "43.202.10.41";
        int serverPort = 9101;

        try (Socket socket = new Socket(serverHost, serverPort);
             OutputStream out = socket.getOutputStream();
        ) {

            JSONObject jsonObj = new JSONObject();
            // jsonObj.put("wavPath","/home/ec2-user/wav_dir/recorded_audio_8k.wav");
            jsonObj.put("wavPath",wavPath); // 녹음한 음성을 보내는게 맞음 -> 인증용이냐 등록용이냐

            String jsonString = jsonObj.toString();

            System.out.println("json body string: " + jsonString);
            int jsonLen = jsonString.length(); // json의 길이

            // RequestHeader
            //String apiCode = "1000";

            String apiCode = "2000"; // 등록: 1000, 인증:2000
            String resultCode = "0000"; // 고정
            String bodySize = String.format("%08d", jsonString.length());


            String header = apiCode + resultCode + bodySize;

            // 전체 요청 메시지 생성
            String requestMessage = header + jsonString;
            System.out.println("최종요청 스트링: " + requestMessage);

            // 요청 메시지 전송
            out.write(requestMessage.getBytes());

            InputStream in = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String response = br.readLine();
            System.out.println("response: " + response);

            String responseApiCode = response.substring(0, 4);
            String responseResultCode = response.substring(4, 8);
            String responseBodySize = response.substring(8, 16);

            String responseBody = response.substring(16);

            System.out.println("responseApiCode: " + responseApiCode);
            System.out.println("responseResultCode: " + responseResultCode);
            System.out.println("responseBodySize: " + responseBodySize);

            System.out.println("responseBody: " + responseBody);


            socket.close();
            out.close();
            in.close();
            jsonObj.clear();
            jsonObj = null;

            return responseBody;


        } catch (IOException e) {
            e.printStackTrace();

            return "error";

        }
    }
}
