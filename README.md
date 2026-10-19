# parley-files-sftp-mina

The [Apache MINA SSHD](https://mina.apache.org/sshd-project/) provider for [parley-files-sftp](https://github.com/parley-project/parley-files-sftp).

```xml
<dependency>
    <groupId>us.bringardner.parley</groupId>
    <artifactId>parley-files-sftp-mina</artifactId>
    <version>1.0.0</version>
</dependency>
```

It brings parley-files-sftp with it.

On the class path it is used when it is the only provider there, or when it is chosen: the
`implementation` connection property or `-Dparley.sftp.implementation=mina`.

## Limitation: no ssh-agent login

**This provider can't log in with an ssh-agent** (`auth=Agent`). A connection asking for it fails with an
`IOException` that says so. Log in with a password or a private key, or use another provider (such as
parley-files-sftp-parley-ssh) for agent login.

Everything else works, including writing at any position in a file in one request. Ed25519 keys work on
Java 11 and later (with `net.i2p.crypto:eddsa`, which comes with it).


## Locks and FileChannel

As parley-files-sftp describes: `PROCESS` locks, atomic `createNewFile()` (an exclusive open), `map()` of a local
copy in `READ_ONLY` and `PRIVATE` modes, and `force()` that sends what is held, then asks the server to sync the
file with `fsync@openssh.com` when it offers that.

## Testing

`mvn verify` runs parley-files-sftp's provider test suite against this provider (the tests jar, through
surefire's `dependenciesToScan`), with `parley.sftp.test.unsupported=agentLogin` in the pom: the suite skips what the library can't do, and this project's own tests: that agent login is refused with a message that says so (`MinaAgentLoginTest`). See "Testing a provider" in parley-files-sftp's README; the
suite's server is chosen with `-Dparley.sftp.test.server` as described there.
