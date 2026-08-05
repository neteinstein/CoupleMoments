package org.neteinstein.family.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.neteinstein.family.domain.model.AppUpdate
import org.neteinstein.family.domain.model.UpdateCheckResult
import org.neteinstein.family.domain.repository.UpdateRepository

class CheckForUpdateUseCaseTest {

    private val repository: UpdateRepository = mockk()
    private val useCase = CheckForUpdateUseCase(repository)

    @Test
    fun `invoke returns UpToDate from repository`() = runTest {
        val result = UpdateCheckResult.UpToDate(currentVersionName = "1.0.5")
        coEvery { repository.checkForUpdate() } returns Result.success(result)

        val actual = useCase()

        assertEquals(Result.success(result), actual)
    }

    @Test
    fun `invoke returns UpdateAvailable from repository`() = runTest {
        val update = AppUpdate(versionName = "1.0.6", apkDownloadUrl = "https://example.com/app.apk")
        val result = UpdateCheckResult.UpdateAvailable(update)
        coEvery { repository.checkForUpdate() } returns Result.success(result)

        val actual = useCase()

        assertEquals(Result.success(result), actual)
    }

    @Test
    fun `invoke propagates a failure result`() = runTest {
        val failure = Result.failure<UpdateCheckResult>(IllegalStateException("network error"))
        coEvery { repository.checkForUpdate() } returns failure

        val actual = useCase()

        assertEquals(failure.isFailure, actual.isFailure)
    }
}
