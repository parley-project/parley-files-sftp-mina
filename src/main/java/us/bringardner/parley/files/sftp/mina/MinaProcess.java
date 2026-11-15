package us.bringardner.parley.files.sftp.mina;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;

import us.bringardner.parley.files.sftp.spi.SshExitStatus;

/** A command running on the server over a MINA exec channel (SshConnection.start). */
final class MinaProcess extends Process {

	private final ChannelExec channel;

	/** @param channel an exec channel opened with no streams of its own, so it has inverted ones */
	MinaProcess(ChannelExec channel) {
		this.channel = channel;
	}

	@Override
	public OutputStream getOutputStream() {
		return channel.getInvertedIn();
	}

	@Override
	public InputStream getInputStream() {
		return channel.getInvertedOut();
	}

	@Override
	public InputStream getErrorStream() {
		return channel.getInvertedErr();
	}

	@Override
	public int waitFor() throws InterruptedException {
		while( !waitFor(1, TimeUnit.SECONDS)) {
			// still running
		}
		return exitValue();
	}

	@Override
	public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
		if( Thread.interrupted()) {
			throw new InterruptedException();
		}
		channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), unit.toMillis(timeout));
		return ended();
	}

	/** the channel has closed (MINA's own closed state can lag behind the event) */
	private boolean ended() {
		return channel.getChannelState().contains(ClientChannelEvent.CLOSED);
	}

	@Override
	public int exitValue() {
		if( !ended()) {
			throw new IllegalThreadStateException("The command is still running");
		}
		return SshExitStatus.of(channel.getExitStatus(), channel.getExitSignal());
	}

	@Override
	public boolean isAlive() {
		return !ended();
	}

	@Override
	public void destroy() {
		channel.close(true);
	}
}
