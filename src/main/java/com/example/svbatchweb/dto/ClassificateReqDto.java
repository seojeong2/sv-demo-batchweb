package com.example.svbatchweb.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClassificateReqDto {

    public List<String> wavPathList = new ArrayList<>();
}
