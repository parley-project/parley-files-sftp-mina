# Changelog

## parley-files-sftp-mina 2.0.0 (unreleased)

### Changed (needs a configuration change)

- The provider is chosen with `parley.files.sftp.implementation=mina` (was `parley.sftp.implementation`),
  and the shared suite's settings are `parley.files.sftp.test.*`.

## parley-files-sftp-mina 1.1.0 (unreleased)

### Added

- `sync()` (FileChannel.force()) asks the server to sync the file with `fsync@openssh.com`, when it offers
  it, for streams and random access.

## parley-files-sftp-mina 1.0.0 (2026-10-21)

The first release. The Apache MINA SSHD provider for parley-files-sftp. It was part of parley-files-sftp (package
`us.bringardner.parley.files.sftp.client.mina`, before that BjlFileSystemSftp's
`us.bringardner.io.filesource.sftp.client.mina`) and is now a project of its own, found on the class path
through `ServiceLoader`.

- Package: `us.bringardner.parley.files.sftp.mina`.
- Module name (`Automatic-Module-Name`): `us.bringardner.parley.files.sftp.mina`.
- ssh-agent login (`auth=Agent`) is refused with an `IOException` that says so.
- Tested with parley-files-sftp's provider test suite and its own tests.
