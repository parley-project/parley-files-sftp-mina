# Changelog

## parley-files-sftp-mina 1.0.0 (2026-10-21)

The first release. The Apache MINA SSHD provider for parley-files-sftp. It was part of parley-files-sftp (package
`us.bringardner.parley.files.sftp.client.mina`, before that BjlFileSystemSftp's
`us.bringardner.io.filesource.sftp.client.mina`) and is now a project of its own, found on the class path
through `ServiceLoader`.

- Package: `us.bringardner.parley.files.sftp.mina`.
- Module name (`Automatic-Module-Name`): `us.bringardner.parley.files.sftp.mina`.
- ssh-agent login (`auth=Agent`) is refused with an `IOException` that says so.
- Tested with parley-files-sftp's provider test suite and its own tests.
