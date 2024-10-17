package com.dair.cais.fileattachement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileAttachmentService {

    private final StorageService storageService;

    @Autowired
    public FileAttachmentService(StorageService storageService) {
        this.storageService = storageService;
    }

    public FileAttachment uploadAttachment(MultipartFile file, String alertId, String createdBy, String comment) throws IOException {
        String originalFilename = file.getOriginalFilename();

        // You can still generate a unique filename for internal tracking or other purposes
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // Store the file in S3 using the original filename
        String storedFilename = storageService.store(alertId, originalFilename, file.getInputStream());

        // Prepare the file attachment metadata with both original and unique filenames
        FileAttachment attachment = new FileAttachment();
        attachment.setAlertId(alertId);
        attachment.setFileName(uniqueFilename); // Store original file name in metadata
        attachment.setUniqueFileName(originalFilename); // Store unique name for internal reference
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setCreatedBy(createdBy);
        attachment.setCreatedDate(LocalDateTime.now());
        attachment.setComment(comment);

        return attachment;
    }


    // Method to upload multiple files
    public List<FileAttachment> uploadMultipleAttachments(List<MultipartFile> files, String alertId, String createdBy, String comment) throws IOException {
        List<FileAttachment> attachments = new ArrayList<>();

        // Loop through each file and upload them
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();

            // Keep the original filename instead of generating a unique name
            String storedFilename = storageService.store(alertId, originalFilename, file.getInputStream());

            FileAttachment attachment = new FileAttachment();
            attachment.setAlertId(alertId);
            attachment.setFileName(originalFilename);
            attachment.setUniqueFileName(storedFilename); // Stored with the original name
            attachment.setFileType(file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setCreatedBy(createdBy);
            attachment.setCreatedDate(LocalDateTime.now());
            attachment.setComment(comment);

            attachments.add(attachment);

            //add file name and other attributes in the rdbms table info_alert.cm_attachements
        }

        return attachments;
    }

    public List<String> getAttachmentsByAlertId(String alertId) throws IOException {
        return storageService.listByAlertId(alertId);
    }

    public byte[] downloadAttachment(String alertId, String fileName) throws IOException {
        return storageService.retrieve(alertId, fileName);
    }

    public FileAttachment getAttachmentMetadata(String alertId, String fileName) {
        // This method would typically retrieve metadata from a database
        // For this example, we're creating a dummy FileAttachment object
        FileAttachment attachment = new FileAttachment();
        attachment.setAlertId(alertId);
        attachment.setFileName(fileName);
        attachment.setUniqueFileName(fileName);

        // Set the file type based on the file extension
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        String mimeType = getMimeTypeForExtension(fileExtension);
        attachment.setFileType(mimeType);

        // Set other metadata fields as needed
        return attachment;
    }

    private String getMimeTypeForExtension(String extension) {
        switch (extension.toLowerCase()) {
            // Text
            case "txt": return "text/plain";
            case "css": return "text/css";
            case "csv": return "text/csv";
            case "html": return "text/html";
            case "xml": return "text/xml";

            // Image
            case "gif": return "image/gif";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "tiff": return "image/tiff";
            case "bmp": return "image/bmp";
            case "webp": return "image/webp";
            case "svg": return "image/svg+xml";

            // Audio
            case "mp3": return "audio/mpeg";
            case "wav": return "audio/wav";
            case "ogg": return "audio/ogg";

            // Video
            case "mp4": return "video/mp4";
            case "avi": return "video/x-msvideo";
            case "wmv": return "video/x-ms-wmv";
            case "flv": return "video/x-flv";
            case "webm": return "video/webm";

            // Application
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "zip": return "application/zip";
            case "rar": return "application/x-rar-compressed";
            case "7z": return "application/x-7z-compressed";
            case "tar": return "application/x-tar";
            case "gz": return "application/gzip";
            case "json": return "application/json";
            case "js": return "application/javascript";

            // Font
            case "ttf": return "font/ttf";
            case "otf": return "font/otf";
            case "woff": return "font/woff";
            case "woff2": return "font/woff2";

            // Other
            case "ico": return "image/x-icon";
            case "swf": return "application/x-shockwave-flash";
            case "exe": return "application/x-msdownload";
            case "dll": return "application/x-msdownload";

            default: return "application/octet-stream";
        }
    }
}
