package woowacourse.shopping.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.google.common.truth.Truth.assertThat
import com.harodi.DependencyKey
import com.harodi.DiManager
import com.harodi.ScopeKey
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import woowacourse.shopping.ui.cart.DateFormatter

@RunWith(RobolectricTestRunner::class)
class DiScreenScopeTest {
    @Test
    fun `같은 엔트리에서 화면 의존성을 재사용하고 ViewModel이 정리될 때 해당 스코프만 제거한다`() {
        val diManager = DiManager()
        val applicationScope = ScopeKey(parentKey = null, scopeKind = Scope.APPLICATION)
        diManager.addScopePolicy(DateFormatter::class.java, null, Scope.SCREEN)
        diManager.addScopePolicy(Context::class.java, null, Scope.APPLICATION)
        diManager.addScope(applicationScope, Context::class.java, null, RuntimeEnvironment.getApplication())

        val firstEntry = createEntryOwner()
        val secondEntry = createEntryOwner()
        val firstKey = screenScopeKey(applicationScope, "cart-entry-1")
        val secondKey = screenScopeKey(applicationScope, "cart-entry-2")
        val firstViewModel =
            ViewModelProvider(
                firstEntry,
                DiViewModelFactory(diManager, applicationScope, firstKey),
            )[TestViewModel::class.java]
        val firstFormatter = diManager.resolve(DateFormatter::class.java, firstKey)
        val secondViewModel =
            ViewModelProvider(
                secondEntry,
                DiViewModelFactory(diManager, applicationScope, secondKey),
            )[TestViewModel::class.java]
        val secondFormatter = diManager.resolve(DateFormatter::class.java, secondKey)

        val restoredKey = screenScopeKey(applicationScope, "cart-entry-1")
        val returnedViewModel =
            ViewModelProvider(
                firstEntry,
                DiViewModelFactory(diManager, applicationScope, restoredKey),
            )[TestViewModel::class.java]
        val returnedFormatter = diManager.resolve(DateFormatter::class.java, restoredKey)

        assertThat(restoredKey).isEqualTo(firstKey)
        assertThat(returnedViewModel).isSameInstanceAs(firstViewModel)
        assertThat(returnedFormatter).isSameInstanceAs(firstFormatter)
        assertThat(secondViewModel).isNotSameInstanceAs(firstViewModel)
        assertThat(secondFormatter).isNotSameInstanceAs(firstFormatter)

        firstEntry.viewModelStore.clear()

        val formatterKey = DependencyKey(DateFormatter::class.java, null)
        assertThat(diManager.searchScope(firstKey, formatterKey)).isNull()
        assertThat(diManager.searchScope(secondKey, formatterKey)).isSameInstanceAs(secondFormatter)

        val reenteredKey = screenScopeKey(applicationScope, "cart-entry-3")
        val reenteredFormatter = diManager.resolve(DateFormatter::class.java, reenteredKey)
        assertThat(reenteredFormatter).isNotSameInstanceAs(firstFormatter)
    }

    private fun createEntryOwner(): ViewModelStoreOwner =
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }

    class TestViewModel : ViewModel()
}
