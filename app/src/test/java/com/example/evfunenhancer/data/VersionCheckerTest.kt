package com.example.evfunenhancer.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionCheckerTest {

    @Test fun `minor bump is newer`() {
        assertTrue(isNewerVersion("1.3", "1.4"))
    }

    @Test fun `major bump is newer`() {
        assertTrue(isNewerVersion("1.9", "2.0"))
    }

    @Test fun `patch bump is newer`() {
        assertTrue(isNewerVersion("1.4.0", "1.4.1"))
    }

    @Test fun `v prefix stripped from latest`() {
        assertTrue(isNewerVersion("1.0", "v1.4"))
    }

    @Test fun `same version is not newer`() {
        assertFalse(isNewerVersion("1.4", "1.4"))
    }

    @Test fun `older latest is not newer`() {
        assertFalse(isNewerVersion("2.0", "1.9"))
    }

    @Test fun `malformed latest returns false`() {
        assertFalse(isNewerVersion("1.0", "not-a-version"))
    }

    @Test fun `empty latest returns false`() {
        assertFalse(isNewerVersion("1.0", ""))
    }
}
