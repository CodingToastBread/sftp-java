package me.dailycode.sftp_java.service;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;

@Service
public class SftpConnectionService {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);

    public SftpClient connect(String host, int port, String username, String password) throws IOException {
        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.setServerKeyVerifier((session, remoteAddress, serverKey) -> true);
        sshClient.start();

        ClientSession session = sshClient.connect(username, host, port)
                .verify(CONNECT_TIMEOUT)
                .getSession();

        session.addPasswordIdentity(password);
        session.auth().verify(CONNECT_TIMEOUT);

        return SftpClientFactory.instance().createSftpClient(session);
    }

    public void disconnect(SftpClient sftpClient) {
        if (sftpClient == null) return;
        try {
            ClientSession session = sftpClient.getSession();
            sftpClient.close();
            if (session != null) {
                SshClient sshClient = (SshClient) session.getFactoryManager();
                session.close();
                sshClient.stop();
            }
        } catch (IOException ignored) {
        }
    }
}
