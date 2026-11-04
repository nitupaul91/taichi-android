package taichi.walking.seniors.beginners.taichi.account

import com.sageai.id.IDService
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException

class AccountDeletionServiceTest {

    private val api = mock<AccountApi>()
    private val localAccountData = mock<LocalAccountData>()
    private val deletedIds = mutableListOf<String>()

    private fun service(userId: String, deviceId: String): AccountDeletionService {
        val idService = mock<IDService> {
            onBlocking { getUserID() } doReturn userId
            onBlocking { getDeviceID() } doReturn deviceId
        }
        return AccountDeletionService(api, idService, localAccountData)
    }

    private fun serverAnswers(response: (String) -> Response<DeleteAccountResponse>) = runBlocking {
        whenever(api.deleteAccount(any())).thenAnswer { invocation ->
            val userId = invocation.getArgument<DeleteAccountBody>(0).userId
            deletedIds += userId
            response(userId)
        }
    }

    @Test
    fun `confirmed deletion wipes local data and retires the id`() = runBlocking {
        serverAnswers { Response.success(DeleteAccountResponse(deleted = true)) }

        service(userId = "a1b2c3d4e5f60718", deviceId = "a1b2c3d4e5f60718").deleteAccount()

        assertEquals(listOf("a1b2c3d4e5f60718"), deletedIds)
        verifyBlocking(localAccountData) { wipe(listOf("a1b2c3d4e5f60718")) }
    }

    @Test
    fun `a purchase-restored id and the device id are both deleted`() = runBlocking {
        serverAnswers { Response.success(DeleteAccountResponse(deleted = true)) }

        service(userId = "restored-account-id", deviceId = "a1b2c3d4e5f60718").deleteAccount()

        assertEquals(listOf("restored-account-id", "a1b2c3d4e5f60718"), deletedIds)
        verifyBlocking(localAccountData) { wipe(listOf("restored-account-id", "a1b2c3d4e5f60718")) }
    }

    @Test
    fun `an unconfirmed deletion touches nothing locally`() = runBlocking {
        serverAnswers { Response.success(DeleteAccountResponse(deleted = false)) }

        val error = assertThrows<AccountDeletionException> {
            runBlocking { service(userId = "user-1", deviceId = "user-1").deleteAccount() }
        }

        assertEquals("not_confirmed", error.reason)
        verifyBlocking(localAccountData, never()) { wipe(any()) }
    }

    @Test
    fun `a server error touches nothing locally`() = runBlocking {
        serverAnswers { Response.error(500, "".toResponseBody(null)) }

        val error = assertThrows<AccountDeletionException> {
            runBlocking { service(userId = "user-1", deviceId = "user-1").deleteAccount() }
        }

        assertEquals("http#500", error.reason)
        verifyBlocking(localAccountData, never()) { wipe(any()) }
    }

    @Test
    fun `when the second id fails nothing is wiped, so a retry deletes both again`() = runBlocking {
        serverAnswers { userId ->
            if (userId == "a1b2c3d4e5f60718") {
                Response.error(503, "".toResponseBody(null))
            } else {
                Response.success(DeleteAccountResponse(deleted = true))
            }
        }

        assertThrows<AccountDeletionException> {
            runBlocking { service(userId = "restored-account-id", deviceId = "a1b2c3d4e5f60718").deleteAccount() }
        }

        verifyBlocking(localAccountData, never()) { wipe(any()) }
    }

    @Test
    fun `no connection propagates and touches nothing locally`() = runBlocking {
        // thenAnswer, not thenThrow: Mockito rejects checked exceptions a method does not declare.
        whenever(api.deleteAccount(any())).thenAnswer { throw IOException("offline") }

        assertThrows<IOException> {
            runBlocking { service(userId = "user-1", deviceId = "user-1").deleteAccount() }
        }

        verifyBlocking(localAccountData, never()) { wipe(any()) }
    }

    @Test
    fun `play subscriptions link targets the active product when known`() {
        assertEquals(
            "https://play.google.com/store/account/subscriptions?sku=taichi_yearly_v1&package=taichi.walking.seniors.beginners",
            PlaySubscriptionManagement.subscriptionsUrl("taichi.walking.seniors.beginners", "taichi_yearly_v1")
        )
        assertEquals(
            "https://play.google.com/store/account/subscriptions",
            PlaySubscriptionManagement.subscriptionsUrl("taichi.walking.seniors.beginners", null)
        )
    }
}
