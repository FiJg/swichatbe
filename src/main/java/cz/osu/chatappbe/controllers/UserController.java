package cz.osu.chatappbe.controllers;

import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.services.models.UserService;
import cz.osu.chatappbe.services.utility.AvatarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private UserService userService;

    @PostMapping("/{username}/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@PathVariable String username, @RequestParam("file") MultipartFile file) {
        try {

            String avatarUrl = avatarService.saveAvatar(file, username);
            String fullAvatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads")
                    .path(avatarUrl)
                    .toUriString(); // Save avatar URL in DB
            userService.updateAvatarUrl(username, fullAvatarUrl);


            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", fullAvatarUrl); // Return the avatar URL

            logger.info("Uploading avatar for user: {}", username);
            logger.info("File received: {} (size: {})", file.getOriginalFilename(), file.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error uploading avatar: " + e.getMessage()));
        }
    }

    @GetMapping("/{username}/avatar")
    public ResponseEntity<Map<String, String>> getUserAvatar(@PathVariable String username) {
        Optional<ChatUser> userOptional = userService.get(username);
        if (userOptional.isPresent()) {
            String avatarUrl = userOptional.get().getAvatarUrl();
            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", avatarUrl);
            return ResponseEntity.ok(response);
        } else {
            logger.warn("User not found: {}", username);
            return ResponseEntity.status(404).body(Map.of("error", "F:User not found"));
        }
    }





}