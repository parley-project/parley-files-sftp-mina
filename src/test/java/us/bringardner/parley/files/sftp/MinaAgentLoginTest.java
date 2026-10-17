package us.bringardner.parley.files.sftp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import us.bringardner.parley.files.sftp.mina.MinaProvider;

/**
 * The mina provider's known limitation (see its README): it can't log in with an ssh-agent, and
 * says so instead of trying something else. The suite skips its agent tests here (agentLogin is
 * declared unsupported in the pom).
 */
public class MinaAgentLoginTest {

	@Test
	void agentLoginIsRefusedWithAMessageThatSaysSo() throws IOException {
		SftpFileSourceFactory f = new SftpFileSourceFactory();
		Properties p = TestServer.properties(MinaProvider.NAME, TestServer.port());
		p.setProperty(SftpFileSourceFactory.PROP_PASSWORD, "");
		p.setProperty(SftpFileSourceFactory.PROP_AUTH, SftpFileSourceFactory.AUTH_AGENT);
		IOException e = assertThrows(IOException.class, () -> f.connect(p));
		assertTrue(e.getMessage().contains("The mina SSH provider can't log in with an ssh-agent"), e.getMessage());
	}
}
