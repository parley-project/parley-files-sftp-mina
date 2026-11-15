package us.bringardner.parley.files.sftp.mina;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.session.ConnectionService;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.client.SftpVersionSelector;

import us.bringardner.parley.files.sftp.spi.SftpChannel;
import us.bringardner.parley.files.sftp.spi.SshConnection;

class MinaConnection implements SshConnection {

	private final SshClient client;
	private final ClientSession session;
	private final Duration timeout;

	MinaConnection(SshClient client, ClientSession session, Duration timeout) {
		this.client = client;
		this.session = session;
		this.timeout = timeout;
	}

	@Override
	public boolean isConnected() {
		return session.isOpen();
	}

	@Override
	public SftpChannel openSftp() throws IOException {
		// SFTP version 3: numeric uid/gid and the ls -l long names, like OpenSSH
		// and JSch. Later versions send owner/group as names instead.
		SftpClient sftp = SftpClientFactory.instance().createSftpClient(session, SftpVersionSelector.fixedVersionSelector(3));
		return new MinaSftpChannel(sftp);
	}

	@Override
	public ExecResult exec(String command, long commandTimeoutMs) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		try (ChannelExec exec = session.createExecChannel(command)) {
			exec.setOut(out);
			exec.setErr(err);
			exec.open().verify(timeout);
			Set<ClientChannelEvent> events = exec.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), commandTimeoutMs);
			if( !events.contains(ClientChannelEvent.CLOSED)) {
				throw new IOException("Command did not finish within "+commandTimeoutMs/1000+" s: "+command);
			}
			Integer status = exec.getExitStatus();
			return new ExecResult(status == null ? -1 : status,
					out.toString(StandardCharsets.UTF_8.name()),
					err.toString(StandardCharsets.UTF_8.name()));
		}
	}

	/**
	 * An exec channel that asks the server to say whether it runs the command (want-reply), and
	 * records the answer: MINA only logs a refusal and closes the channel.
	 */
	private static final class AnsweredExec extends ChannelExec {
		final CompletableFuture<Boolean> accepted = new CompletableFuture<>();

		AnsweredExec(String command) {
			super(command, null, null);
			CoreModuleProperties.REQUEST_EXEC_REPLY.set(this, true);
		}

		@Override
		public void handleSuccess() throws IOException {
			super.handleSuccess();
			accepted.complete(true);
		}

		@Override
		public void handleFailure() throws IOException {
			accepted.complete(false);
			super.handleFailure();
		}
	}

	@Override
	public Process start(String command) throws IOException {
		// no streams of our own: the channel then has piped ones for the Process
		AnsweredExec channel = new AnsweredExec(command);
		session.getService(ConnectionService.class).registerChannel(channel);
		try {
			channel.open().verify(timeout);
			if( !channel.accepted.get(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
				throw new IOException("The server refused to run the command");
			}
		} catch (InterruptedException e) {
			channel.close(true);
			Thread.currentThread().interrupt();
			throw new java.io.InterruptedIOException("Interrupted while starting: "+command);
		} catch (ExecutionException | TimeoutException e) {
			channel.close(true);
			throw new IOException("The server didn't say whether it runs the command: "+e, e);
		} catch (IOException | RuntimeException e) {
			channel.close(true);
			throw e;
		}
		return new MinaProcess(channel);
	}

	@Override
	public void close() {
		try {
			session.close(false);
		} finally {
			client.stop();
		}
	}
}
