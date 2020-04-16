package max.mini.mvi.elm.test.user.list

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.swiperefreshlayout.widget.swipeRefreshLayout
import dev.inkremental.dsl.android.*
import dev.inkremental.dsl.android.Size.*
import dev.inkremental.dsl.android.widget.linearLayout
import dev.inkremental.dsl.android.widget.progressBar
import dev.inkremental.dsl.android.widget.textView
import dev.inkremental.dsl.android.widget.toolbar
import dev.inkremental.dsl.androidx.recyclerview.RenderableRecyclerViewAdapter
import dev.inkremental.dsl.androidx.recyclerview.adapter
import dev.inkremental.dsl.androidx.recyclerview.linearLayoutManager
import dev.inkremental.dsl.androidx.recyclerview.widget.recyclerView
import dev.inkremental.r
import dev.inkremental.skip
import max.mini.mvi.elm.common_ui.getColorWithId
import max.mini.mvi.elm.test.R
import max.mini.mvi.elm.test.base.ControllerFragment

class UsersFragment : ControllerFragment<UserListModel, UserListEvent, UserListEffect>() {

    override fun renderViewModel(viewModel: UserListModel) {
        linearLayout {

            size(MATCH, MATCH)
            orientation(LinearLayout.VERTICAL)

            toolbar {
                size(MATCH, WRAP)
                title("USERS")
                titleTextColor(Color.WHITE)
                backgroundColor(r.getColorWithId(R.color.colorPrimary))
            }

            swipeRefreshLayout {

                size(MATCH, EXACT(Px(0)))
                weight(1f)
                refreshing(viewModel.refreshing)
                onRefresh {
                    sendEvent(UserListEvent.RefreshRequest)
                }
                skip() //skip circle progress loader

                recyclerView {
                    size(MATCH, MATCH)
                    linearLayoutManager()
                    hasFixedSize(true)

                    adapter(RenderableRecyclerViewAdapter.withItems(
                        viewModel.users
                    ) { position, user ->
                        linearLayout {
                            orientation(LinearLayout.VERTICAL)
                            padding(Dip(16))
                            backgroundColor(if (user.picked) Color.LTGRAY else Color.TRANSPARENT)
                            onClick {
                                sendEvent(UserListEvent.UserWithPositionClick(position))
                            }

                            textView {
                                text(user.name)
                            }

                            textView {
                                text(user.email)
                                margin(
                                    Dip(0),
                                    Dip(8),
                                    Dip(0),
                                    Dip(0)
                                )
                            }
                        }
                    })
                }
            }
        }
        progressBar {
            size(WRAP, WRAP)
            layoutGravity(Gravity.CENTER)
            visibility(if (viewModel.loading) View.VISIBLE else View.GONE)
        }

    }

}
