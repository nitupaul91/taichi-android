package taichi.walking.seniors.beginners.taichi.account

import com.sageai.id.IDService
import javax.inject.Inject
import javax.inject.Singleton

/** A deletion the server did not confirm; [reason] is the analytics outcome detail. */
class AccountDeletionException(val reason: String) : Exception("Account deletion failed: $reason")

/**
 * Deletes the user's account on the server, then wipes everything on the device that identified
 * them, so the app behaves like a fresh install under a brand-new user id. Mirrors the iOS
 * `AccountDeletionService`.
 */
@Singleton
class AccountDeletionService @Inject constructor(
    private val accountApi: AccountApi,
    private val idService: IDService,
    private val localAccountData: LocalAccountData,
) {

    /**
     * Nothing local is touched unless the server confirms every deletion, so a failed attempt leaves
     * the user exactly where they were and they can retry (the endpoint is idempotent).
     *
     * Android can hold data under two ids for one person: until a Play purchase restores the account
     * id, the install runs, uploads its config and onboarding answers under ANDROID_ID. Both go.
     */
    suspend fun deleteAccount() {
        val accountIds = listOfNotNull(
            idService.getUserID(),
            runCatching { idService.getDeviceID() }.getOrNull()
        )
            .filter { it.isNotBlank() }
            .distinct()

        accountIds.forEach { userId -> deleteOnServer(userId) }

        localAccountData.wipe(retiredUserIds = accountIds)
    }

    private suspend fun deleteOnServer(userId: String) {
        val response = accountApi.deleteAccount(DeleteAccountBody(userId))
        if (!response.isSuccessful) {
            throw AccountDeletionException("http#${response.code()}")
        }
        // The server answered 2xx but did not confirm the deletion.
        if (response.body()?.deleted != true) {
            throw AccountDeletionException("not_confirmed")
        }
    }
}
