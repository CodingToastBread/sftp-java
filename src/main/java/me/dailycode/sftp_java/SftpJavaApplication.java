package me.dailycode.sftp_java;

import me.dailycode.sftp_java.cli.SftpCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
public class SftpJavaApplication implements CommandLineRunner, ExitCodeGenerator {

    private final IFactory factory;
    private int exitCode;

    public SftpJavaApplication(IFactory factory) {
        this.factory = factory;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(SftpJavaApplication.class, args)));
    }

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(SftpCommand.class, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
