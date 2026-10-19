package us.bringardner.parley.files.sftp.mina;

import java.io.IOException;
import java.util.Collection;

import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClient.OpenMode;
import org.apache.sshd.sftp.client.extensions.openssh.OpenSSHFsyncExtension;
import org.apache.sshd.sftp.client.impl.AbstractSftpClient;
import org.apache.sshd.sftp.client.impl.SftpOutputStreamAsync;

import us.bringardner.parley.files.Syncable;

/**
 * MINA's pipelined writer, made {@link Syncable}: {@link #sync()} waits for every write to be
 * acknowledged ({@code flush}), then asks the server to sync the file ({@code fsync@openssh.com}) if
 * it offers that.
 */
final class MinaSyncableOutput extends SftpOutputStreamAsync implements Syncable {

	private final SftpClient sftp;

	MinaSyncableOutput(AbstractSftpClient sftp, int bufferSize, String path, Collection<OpenMode> modes) throws IOException {
		super(sftp, bufferSize, path, modes);
		this.sftp = sftp;
	}

	@Override
	public void sync() throws IOException {
		flush();
		fsync(sftp, handle);
	}

	/** fsync@openssh.com on a handle, if the server offers it; nothing otherwise. */
	static void fsync(SftpClient sftp, SftpClient.Handle handle) throws IOException {
		OpenSSHFsyncExtension ext = sftp.getExtension(OpenSSHFsyncExtension.class);
		if( ext != null && ext.isSupported() ) {
			ext.fsync(handle);
		}
	}
}
