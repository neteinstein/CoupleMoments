package org.neteinstein.couples.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.neteinstein.couples.domain.model.AppUpdate
import org.neteinstein.couples.domain.repository.UpdateRepository
import java.io.File

class DownloadAppUpdateUseCaseTest {
    private val repository: UpdateRepository = mockk()
    private val useCase = DownloadAppUpdateUseCase(repository)
    private val update = AppUpdate(versionName = "1.0.6", apkDownloadUrl = "https://example.com/app.apk")

    @Test
    fun `invoke returns downloaded file from repository`() =
        runTest {
            val file = File("/tmp/app.apk")
            coEvery { repository.downloadUpdate(update) } returns Result.success(file)

            val result = useCase(update)

            assertEquals(Result.success(file), result)
        }

    @Test
    fun `invoke passes the update to the repository`() =
        runTest {
            coEvery { repository.downloadUpdate(update) } returns Result.success(File("/tmp/app.apk"))

            useCase(update)

            coVerify { repository.downloadUpdate(update) }
        }
}
