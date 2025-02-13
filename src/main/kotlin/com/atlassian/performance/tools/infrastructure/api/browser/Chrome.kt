package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.ChromedriverInstaller
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration.ofMinutes


/**
 * We have no control over the chrome version. We install the latest stable chrome version. It may cause not repeatable builds.
 */
class Chrome : Browser {
    private val ubuntu = Ubuntu()

    override fun install(ssh: SshConnection) {
        ubuntu.addKey(ssh, "78BD65473CB3BD13")
        ubuntu.addRepository(ssh, "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main", "google-chrome")
        Ubuntu().install(ssh, listOf("google-chrome-stable"), ofMinutes(5))
        val installedMinorVersion = getInstalledMinorVersion(ssh)

        val version = ChromedriverInstaller.getLatestVersion(installedMinorVersion)
        ChromedriverInstaller(URI("https://chromedriver.storage.googleapis.com/$version/chromedriver_linux64.zip")).install(ssh)

    }

    private fun getInstalledMinorVersion(ssh: SshConnection): String? {
        val versionString = ssh.execute("/usr/bin/google-chrome --version").output
        return Regex("Google Chrome ([0-9]+\\.[0-9]+\\.[0-9]+)\\.[0-9]+").find(versionString)?.groupValues?.getOrNull(1)
    }
}
