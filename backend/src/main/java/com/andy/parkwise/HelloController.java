package com.andy.parkwise;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/morning")
    public String goodMorning(String name) {
        return "Good morning, " + name;
    }
}
