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
@Command(name = "get", description = "Download file(s) from remote server", mixinStandardHelpOptions = true)
public class GetCommand implements Callable<Integer> {

    @Mixin
    private SftpCommand.ConnectionOptions connection;

    @Option(names = {"--regex"}, description = "Regex pattern to match filenames (remote path becomes directory)")
    private String regex;

    @Parameters(index = "0", description = "Remote file path (or directory when --regex)")
    private String remotePath;

    @Parameters(index = "1", description = "Local file path (or directory when --regex)")
    private String localPath;

    private final SftpConnectionService connectionService;
    private final SftpOperationService operationService;

    public GetCommand(SftpConnectionService connectionService, SftpOperationService operationService) {
        this.connectionService = connectionService;
        this.operationService = operationService;
    }

    @Override
    public Integer call() {
        SftpClient client = null;
        try {
            client = connectionService.connect(connection.host, connection.port, connection.user, connection.password);
            if (regex != null) {
                operationService.getByRegex(client, remotePath, localPath, regex);
            } else {
                operationService.get(client, remotePath, localPath);
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } finally {
            connectionService.disconnect(client);
        }
    }
}
