package com.georgcantor.wallpaperapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.georgcantor.wallpaperapp.R
import com.georgcantor.wallpaperapp.ui.adapter.PicturesAdapter
import com.georgcantor.wallpaperapp.ui.util.DisposableManager
import com.georgcantor.wallpaperapp.ui.util.EndlessRecyclerViewScrollListener
import com.georgcantor.wallpaperapp.ui.util.HideNavScrollListener
import com.georgcantor.wallpaperapp.ui.util.UtilityMethods
import com.georgcantor.wallpaperapp.ui.util.hideAnimation
import com.georgcantor.wallpaperapp.ui.util.isNetworkAvailable
import com.georgcantor.wallpaperapp.ui.util.longToast
import com.georgcantor.wallpaperapp.ui.util.shortToast
import com.georgcantor.wallpaperapp.ui.util.showAnimation
import com.georgcantor.wallpaperapp.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.app_bar_main.navigation
import kotlinx.android.synthetic.main.fragment_common.animationView
import kotlinx.android.synthetic.main.fragment_common.noInternetImageView
import kotlinx.android.synthetic.main.fragment_common.recyclerView
import kotlinx.android.synthetic.main.fragment_common.refreshLayout
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class AudiFragment: Fragment() {

    companion object {
        const val REQUEST = "request"

        fun newInstance(arguments: String): AudiFragment {
            val fragment = AudiFragment()
            val args = Bundle()
            args.putString(REQUEST, arguments)
            fragment.arguments = args

            return fragment
        }
    }

    private lateinit var viewModel: SearchViewModel
    private var adapter: PicturesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireActivity().isNetworkAvailable()) {
            requireActivity().longToast(getString(R.string.check_internet))
        }
        viewModel = getViewModel { parametersOf() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_common, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!requireActivity().isNetworkAvailable()) {
            noInternetImageView.visibility = View.VISIBLE
        }

        refreshLayout.setOnRefreshListener {
            loadData(1)
            refreshLayout.isRefreshing = false
        }

        val gridLayoutManager = StaggeredGridLayoutManager(
            UtilityMethods.getScreenSize(requireContext()),
            StaggeredGridLayoutManager.VERTICAL
        )
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = gridLayoutManager

        val scrollListener = object : EndlessRecyclerViewScrollListener(gridLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                loadData(page)
            }
        }
        scrollListener.resetState()
        recyclerView.addOnScrollListener(scrollListener)
        adapter = PicturesAdapter(requireContext())
        recyclerView.adapter = adapter

        val hideScrollListener = object : HideNavScrollListener(requireActivity().navigation) {}
        recyclerView.addOnScrollListener(hideScrollListener)

        loadData(1)
    }

    private fun loadData(index: Int) {
        animationView?.showAnimation()

        val disposable = viewModel.getPics(arguments?.getString(REQUEST) ?: "", index)
            .subscribe({
                adapter?.setPicList(it)
                animationView?.hideAnimation()
            }, {
                animationView?.hideAnimation()
                requireActivity().shortToast(getString(R.string.something_went_wrong))
            })

        DisposableManager.add(disposable)
    }

    override fun onDestroy() {
        DisposableManager.dispose()
        super.onDestroy()
    }

}