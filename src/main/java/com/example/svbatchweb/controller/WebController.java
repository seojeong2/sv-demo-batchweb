package com.example.svbatchweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class WebController {

    @GetMapping("/")
    public String goMain() {
        return "index";
    }

    @GetMapping("/hello")
    public String goHello() {

        return "hello";
    }

    @GetMapping("/demo")
    public String goTest() {
        return "demo";
    }
}
