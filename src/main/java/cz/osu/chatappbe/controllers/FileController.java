package cz.osu.chatappbe.controllers;

import cz.osu.chatappbe.models.entity.Message;
import cz.osu.chatappbe.services.models.MessageService;
import cz.osu.chatappbe.services.utility.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/messages")
public class FileController {

    private final FileStorageService fileStorageService;
    private final MessageService messageService;

    public FileController(FileStorageService fileStorageService, MessageService messageService) {
        this.fileStorageService = fileStorageService;
        this.messageService = messageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("messageId") Integer messageId) {
        try {
            String fileUrl = fileStorageService.saveFile(file);
            Message updatedMessage = messageService.updateMessageWithFile(messageId, fileUrl,
                    file.getOriginalFilename(), file.getContentType());
            return ResponseEntity.ok(updatedMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
        }
    }
}
