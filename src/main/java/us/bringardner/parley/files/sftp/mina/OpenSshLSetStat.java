package us.bringardner.parley.files.sftp.mina;

import java.io.IOException;

import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.sftp.client.RawSftpClient;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClient.Attributes;
import org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension;
import org.apache.sshd.sftp.common.SftpHelper;

/**
 * OpenSSH's "lsetstat@openssh.com" SFTP extension (PROTOCOL, 3.7): SETSTAT that doesn't follow a
 * symbolic link, so its own owner and times change (chown -h, touch -h). MINA SSHD has the server
 * side of it but no client.
 */
class OpenSshLSetStat extends AbstractSftpClientExtension {

	static final String NAME = "lsetstat@openssh.com";

	OpenSshLSetStat(SftpClient client) {
		super(NAME, client, (RawSftpClient) client, client.getServerExtensions());
	}

	/** True if the client can send it and the server announced it. */
	static boolean isAvailable(SftpClient client) {
		return client instanceof RawSftpClient && client.getServerExtensions().containsKey(NAME);
	}

	/** Changes the fields set in 'attrs' of the path itself. */
	void set(String path, Attributes attrs) throws IOException {
		Buffer buffer = getCommandBuffer(path.length() + 64);
		buffer.putString(path);
		SftpHelper.writeAttributes(buffer, attrs, getClient().getVersion());
		sendAndCheckExtendedCommandStatus(buffer);
	}
}
