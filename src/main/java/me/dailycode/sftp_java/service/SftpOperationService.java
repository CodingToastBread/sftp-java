package me.dailycode.sftp_java.service;

import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class SftpOperationService {

    private static final int BUFFER_SIZE = 8192;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public void ls(SftpClient client, String remotePath) throws IOException {
        try (SftpClient.CloseableHandle handle = client.openDir(remotePath)) {
            for (SftpClient.DirEntry entry : client.readDir(handle)) {
                String name = entry.getFilename();
                if (".".equals(name) || "..".equals(name)) continue;

                SftpClient.Attributes attrs = entry.getAttributes();
                long size = attrs.getSize();
                String modified = formatTime(attrs.getModifyTime());
                String type = attrs.isDirectory() ? "d" : "-";

                System.out.printf("%s %10d %s %s%n", type, size, modified, name);
            }
        }
    }

    public void get(SftpClient client, String remotePath, String localPath) throws IOException {
        try (InputStream in = client.read(remotePath);
             OutputStream out = new FileOutputStream(localPath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long total = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                total += bytesRead;
            }
            System.out.printf("Downloaded %d bytes: %s -> %s%n", total, remotePath, localPath);
        }
    }

    public void put(SftpClient client, String localPath, String remotePath) throws IOException {
        try (InputStream in = new FileInputStream(localPath);
             OutputStream out = client.write(remotePath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long total = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                total += bytesRead;
            }
            System.out.printf("Uploaded %d bytes: %s -> %s%n", total, localPath, remotePath);
        }
    }

    public void getByRegex(SftpClient client, String remoteDir, String localDir, String regex) throws IOException {
        Pattern pattern = Pattern.compile(regex);
        File localDirFile = new File(localDir);
        if (!localDirFile.exists()) {
            localDirFile.mkdirs();
        }

        List<String> matched = new ArrayList<>();
        try (SftpClient.CloseableHandle handle = client.openDir(remoteDir)) {
            for (SftpClient.DirEntry entry : client.readDir(handle)) {
                String name = entry.getFilename();
                if (".".equals(name) || "..".equals(name)) continue;
                if (entry.getAttributes().isDirectory()) continue;
                if (pattern.matcher(name).matches()) {
                    matched.add(name);
                }
            }
        }

        if (matched.isEmpty()) {
            System.out.println("No files matched: " + regex);
            return;
        }

        System.out.printf("Matched %d file(s)%n", matched.size());
        for (String name : matched) {
            String remotePath = remoteDir.endsWith("/") ? remoteDir + name : remoteDir + "/" + name;
            String localPath = localDir.endsWith(File.separator) ? localDir + name : localDir + File.separator + name;
            get(client, remotePath, localPath);
        }
    }

    public void rm(SftpClient client, String remotePath) throws IOException {
        client.remove(remotePath);
        System.out.printf("Deleted: %s%n", remotePath);
    }

    private String formatTime(FileTime fileTime) {
        if (fileTime == null) return "                   ";
        Instant instant = fileTime.toInstant();
        return DATE_FORMAT.format(instant);
    }
}
