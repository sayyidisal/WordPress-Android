package org.wordpress.android.viewmodel.pages

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.wordpress.android.R
import org.wordpress.android.R.string
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.PageStore
import org.wordpress.android.ui.pages.PageItem
import org.wordpress.android.ui.pages.PageItem.Divider
import org.wordpress.android.ui.pages.PageItem.Empty
import org.wordpress.android.ui.pages.PageItem.ParentPage
import org.wordpress.android.viewmodel.ResourceProvider
import javax.inject.Inject

class PageParentViewModel
@Inject constructor(private val pageStore: PageStore, private val resourceProvider: ResourceProvider) : ViewModel() {
    private val _pages: MutableLiveData<List<PageItem>> = MutableLiveData()
    val pages: LiveData<List<PageItem>>
        get() = _pages

    private var isStarted: Boolean = false
    private lateinit var site: SiteModel
    private lateinit var currentParent: ParentPage

    fun start(site: SiteModel) {
        this.site = site

        if (!isStarted) {
            _pages.postValue(listOf(Empty(string.empty_list_default)))
            isStarted = true

            loadPages()
        }
    }

    private fun loadPages() = launch(CommonPool) {
        currentParent = ParentPage(0, resourceProvider.getString(R.string.top_level), true)
        val parents = mutableListOf(
                currentParent,
                Divider(resourceProvider.getString(R.string.pages))
        )

        parents.addAll(pageStore.getPagesFromDb(site).map { ParentPage(it.remoteId, it.title, false) })
        _pages.postValue(parents)
    }

    fun onParentSelected(page: ParentPage) {
        currentParent.isSelected = false
        currentParent = page
        currentParent.isSelected = true
    }
}
