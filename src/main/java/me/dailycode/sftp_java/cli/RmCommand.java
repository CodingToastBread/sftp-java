package me.dailycode.sftp_java.cli;

import me.dailycode.sftp_java.service.SftpConnectionService;
import me.dailycode.sftp_java.service.SftpOperationService;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Scanner;
import java.util.concurrent.Callable;

@Component
@Command(name = "rm", description = "Delete a remote file", mixinStandardHelpOptions = true)
public class RmCommand implements Callable<Integer> {

    @Mixin
    private SftpCommand.ConnectionOptions connection;

    @Option(names = {"-f", "--force"}, description = "Skip confirmation prompt")
    private boolean force;

    @Parameters(index = "0", description = "Remote file path to delete")
    private String remotePath;

    private final SftpConnectionService connectionService;
    private final SftpOperationService operationService;

    public RmCommand(SftpConnectionService connectionService, SftpOperationService operationService) {
        this.connectionService = connectionService;
        this.operationService = operationService;
    }

    @Override
    public Integer call() {
        if (!force) {
            System.out.printf("Delete '%s'? (y/N): ", remotePath);
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine().trim();
            if (!answer.equalsIgnoreCase("y")) {
                System.out.println("Cancelled.");
                return 0;
            }
        }

        SftpClient client = null;
        try {
            client = connectionService.connect(connection.host, connection.port, connection.user, connection.password);
            operationService.rm(client, remotePath);
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } finally {
            connectionService.disconnect(client);
        }
    }
}
