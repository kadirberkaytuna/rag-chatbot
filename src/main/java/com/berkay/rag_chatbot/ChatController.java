package com.berkay.rag_chatbot;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final Assistant assistant;
    private final DocumentIngestionService ingestionService;

    public ChatController(Assistant assistant,
                          DocumentIngestionService ingestionService) {
        this.assistant = assistant;
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        ingestionService.ingest(file.getInputStream());
        return "Döküman yüklendi ve vektörlendi: " + file.getOriginalFilename();
    }

    @PostMapping("/chat")
    public String chat(@RequestParam("question") String question) {
        return assistant.chat(question);
    }
}