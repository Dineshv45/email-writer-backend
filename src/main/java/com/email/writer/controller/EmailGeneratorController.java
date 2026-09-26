package com.email.writer.controller;

import com.email.writer.emaildto.EmailRequestDto;
import com.email.writer.service.EmailGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*" )
public class EmailGeneratorController {

    private final EmailGeneratorService emailGeneratorService;

    public EmailGeneratorController(EmailGeneratorService emailGeneratorService) {
        this.emailGeneratorService = emailGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequestDto emailRequestDto){
        LocalDateTime requestTime = LocalDateTime.now();
        String response = emailGeneratorService.generateEmailReply(emailRequestDto);

        LocalDateTime responseTime = LocalDateTime.now();

        Duration duration = Duration.between(requestTime, responseTime);

        System.out.println(duration);
        return ResponseEntity.ok(response);
    }
}
