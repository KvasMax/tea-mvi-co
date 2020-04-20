package max.mini.mvi.elm.test.user.detail

import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import androidx.swiperefreshlayout.widget.swipeRefreshLayout
import dev.inkremental.dsl.android.*
import dev.inkremental.dsl.android.Size.MATCH
import dev.inkremental.dsl.android.Size.WRAP
import dev.inkremental.dsl.android.widget.*
import dev.inkremental.r
import max.mini.mvi.elm.common_ui.getColorWithId
import max.mini.mvi.elm.test.R
import max.mini.mvi.elm.test.base.ControllerFragment
import javax.inject.Inject

class UserInfoFragment @Inject constructor(
) : ControllerFragment<UserInfoViewModel, UserInfoDataModel, UserInfoEvent, UserInfoEffect>() {

    override fun renderViewModel(
        viewModel: UserInfoViewModel
    ) {
        linearLayout {
            size(MATCH, MATCH)
            orientation(LinearLayout.VERTICAL)

            toolbar {
                size(MATCH, WRAP)
                title("USER INFO")
                titleTextColor(Color.WHITE)
                backgroundColor(r.getColorWithId(R.color.colorPrimary))
            }

            swipeRefreshLayout {
                size(MATCH, Size.EXACT(Px(0)))
                weight(1f)
                refreshing(viewModel.refreshing)
                onRefresh {
                    sendEvent(UserInfoEvent.RefreshRequest)
                }

                frameLayout {
                    size(MATCH, MATCH)

                    progressBar {
                        size(WRAP, WRAP)
                        layoutGravity(Gravity.CENTER)
                        visibility(viewModel.loading)
                    }
                    textView {
                        size(WRAP, WRAP)
                        layoutGravity(Gravity.CENTER)
                        gravity(Gravity.CENTER)
                        text(
                            arrayOf(
                                viewModel.name,
                                viewModel.email,
                                viewModel.phoneNumber,
                                viewModel.website
                            ).filterNotNull()
                                .joinToString(separator = "\n")
                        )
                        onClick {
                            sendEvent(UserInfoEvent.Pick)
                        }
                    }
                }
            }
        }
    }

}