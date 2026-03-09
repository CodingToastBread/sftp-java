package me.dailycode.sftp_java.cli;

import me.dailycode.sftp_java.service.SftpConnectionService;
import me.dailycode.sftp_java.service.SftpOperationService;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Component
@Command(name = "put", description = "Upload a file to remote server", mixinStandardHelpOptions = true)
public class PutCommand implements Callable<Integer> {

    @Mixin
    private SftpCommand.ConnectionOptions connection;

    @Parameters(index = "0", description = "Local file path")
    private String localPath;

    @Parameters(index = "1", description = "Remote file path")
    private String remotePath;

    private final SftpConnectionService connectionService;
    private final SftpOperationService operationService;

    public PutCommand(SftpConnectionService connectionService, SftpOperationService operationService) {
        this.connectionService = connectionService;
        this.operationService = operationService;
    }

    @Override
    public Integer call() {
        SftpClient client = null;
        try {
            client = connectionService.connect(connection.host, connection.port, connection.user, connection.password);
            operationService.put(client, localPath, remotePath);
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } finally {
            connectionService.disconnect(client);
        }
    }
}
