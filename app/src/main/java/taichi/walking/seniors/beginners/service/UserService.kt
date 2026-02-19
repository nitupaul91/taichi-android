package taichi.walking.seniors.beginners.service

import taichi.walking.seniors.beginners.data.api.UserApi
import android.content.Context
import com.sageai.id.IDService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserService @Inject constructor(
    private val userApi: UserApi,
    private val idService: IDService,
    @ApplicationContext private val context: Context,
) {

}