package org.neteinstein.couples.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.neteinstein.couples.domain.model.AppUpdate
import org.neteinstein.couples.domain.repository.UpdateRepository

class DownloadAppUpdateUseCaseTest {
    private val repository: UpdateRepository = mockk()
    private val useCase = DownloadAppUpdateUseCase(repository)
    private val update = AppUpdate(versionName = "1.0.6", apkDownloadUrl = "https://example.com/app.apk")

    @Test
    fun `invoke returns downloaded bytes from repository`() =
        runTest {
            val bytes = byteArrayOf(1, 2, 3)
            coEvery { repository.downloadUpdate(update) } returns Result.success(bytes)

            val result = useCase(update)

            assertEquals(Result.success(bytes), result)
        }

    @Test
    fun `invoke passes the update to the repository`() =
        runTest {
            coEvery { repository.downloadUpdate(update) } returns Result.success(byteArrayOf(1, 2, 3))

            useCase(update)

            coVerify { repository.downloadUpdate(update) }
        }
}
