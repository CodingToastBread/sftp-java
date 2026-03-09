package me.dailycode.sftp_java.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "sftp",
        description = "SFTP CLI Tool — ls, get, put operations",
        mixinStandardHelpOptions = true,
        subcommands = {LsCommand.class, GetCommand.class, PutCommand.class, RmCommand.class}
)
public class SftpCommand implements Runnable {

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }

    public static class ConnectionOptions {

        @CommandLine.Option(names = {"-H", "--host"}, required = true, description = "SFTP server host")
        public String host;

        @CommandLine.Option(names = {"-P", "--port"}, defaultValue = "22", description = "SFTP server port (default: 22)")
        public int port;

        @CommandLine.Option(names = {"-u", "--user"}, required = true, description = "Username")
        public String user;

        @CommandLine.Option(names = {"-p", "--password"}, required = true, description = "Password")
        public String password;
    }
}
