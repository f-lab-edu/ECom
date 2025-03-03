package com.example.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DemoController {
    private final DemoService demoService;

    @GetMapping("/save")
    public void save() {
        demoService.save();
    }

    @GetMapping("/find")
    public void find() {
        demoService.find();
    }
}
