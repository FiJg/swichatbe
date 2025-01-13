package cz.osu.chatappbe.services.utility;

import cz.osu.chatappbe.controllers.RabbitController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AvatarService {

    private static final Logger logger = LoggerFactory.getLogger(AvatarService.class);

    private static final String UPLOAD_DIR = "uploads/avatars/";

    public String saveAvatar(MultipartFile file, String username) throws IOException {
        if (!Files.exists(Paths.get(UPLOAD_DIR))) {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());
        String fileName = username + "." + fileExtension;
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.write(filePath, file.getBytes());


        return "/avatars/" + fileName;
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}