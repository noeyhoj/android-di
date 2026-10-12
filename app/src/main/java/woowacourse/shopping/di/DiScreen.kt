package woowacourse.shopping.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.harodi.DiManager
import com.harodi.ScopeKey
import woowacourse.shopping.ui.cart.CartViewModel
import woowacourse.shopping.ui.cart.DateFormatter
import java.util.UUID

fun screenScopeKey(
    parentKey: ScopeKey,
    entryId: String,
): ScopeKey =
    ScopeKey(
        parentKey = parentKey,
        uuid = UUID.nameUUIDFromBytes(entryId.toByteArray(Charsets.UTF_8)),
        scopeKind = Scope.SCREEN,
    )

@Composable
fun DiScreen(
    diManager: DiManager,
    parentKey: ScopeKey,
    navBackStackEntry: NavBackStackEntry,
    screen: @Composable (CartViewModel, DateFormatter) -> Unit,
) {
    val scopeKey =
        remember(parentKey, navBackStackEntry.id) {
            screenScopeKey(parentKey, navBackStackEntry.id)
        }
    val cartViewModel: CartViewModel =
        viewModel(
            viewModelStoreOwner = navBackStackEntry,
            factory = DiViewModelFactory(diManager, parentKey, screenScopeKey = scopeKey),
        )
    val dateFormatter = diManager.resolve(DateFormatter::class.java, scopeKey)

    screen(cartViewModel, dateFormatter)
}
