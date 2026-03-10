package me.dailycode.sftp_java.cli;

import me.dailycode.sftp_java.service.SftpConnectionService;
import me.dailycode.sftp_java.service.SftpOperationService;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Component
@Command(name = "ls", description = "List remote directory contents", mixinStandardHelpOptions = true)
public class LsCommand implements Callable<Integer> {

    @Mixin
    private SftpCommand.ConnectionOptions connection;

    @Option(names = {"--sort"}, defaultValue = "asc", description = "Sort by modified time: asc (oldest first) or desc (newest first)")
    private String sort;

    @Option(names = {"--regex"}, description = "Regex pattern to filter filenames")
    private String regex;

    @Parameters(index = "0", description = "Remote directory path")
    private String remotePath;

    private final SftpConnectionService connectionService;
    private final SftpOperationService operationService;

    public LsCommand(SftpConnectionService connectionService, SftpOperationService operationService) {
        this.connectionService = connectionService;
        this.operationService = operationService;
    }

    @Override
    public Integer call() {
        SftpClient client = null;
        try {
            client = connectionService.connect(connection.host, connection.port, connection.user, connection.password);
            operationService.ls(client, remotePath, "desc".equalsIgnoreCase(sort), regex);
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } finally {
            connectionService.disconnect(client);
        }
    }
}
