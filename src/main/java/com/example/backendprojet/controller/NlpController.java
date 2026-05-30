package com.example.backendprojet.controller;

import com.example.backendprojet.dto.NlpRequest;
import com.example.backendprojet.dto.NlpResponse;
import com.example.backendprojet.services.NlpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nlp")
@CrossOrigin(origins = "http://localhost:4200")
public class NlpController {

    private final NlpService nlpService;

    public NlpController(NlpService nlpService) {
        this.nlpService = nlpService;
    }

    @PostMapping("/convertir")
    public ResponseEntity<NlpResponse> convertir(@RequestBody NlpRequest request) {
        NlpResponse response = nlpService.convertir(request.getPhrase());
        return ResponseEntity.ok(response);
    }
}
