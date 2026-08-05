package org.neteinstein.family.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.neteinstein.family.domain.model.AppUpdate
import org.neteinstein.family.domain.repository.UpdateRepository

class DownloadAppUpdateUseCaseTest {

    private val repository: UpdateRepository = mockk()
    private val useCase = DownloadAppUpdateUseCase(repository)
    private val update = AppUpdate(versionName = "1.0.6", apkDownloadUrl = "https://example.com/app.apk")

    @Test
    fun `invoke returns downloaded file from repository`() = runTest {
        val file = File("/tmp/app.apk")
        coEvery { repository.downloadUpdate(update) } returns Result.success(file)

        val result = useCase(update)

        assertEquals(Result.success(file), result)
    }

    @Test
    fun `invoke passes the update to the repository`() = runTest {
        coEvery { repository.downloadUpdate(update) } returns Result.success(File("/tmp/app.apk"))

        useCase(update)

        coVerify { repository.downloadUpdate(update) }
    }
}
