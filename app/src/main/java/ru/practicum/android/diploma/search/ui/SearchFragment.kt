package ru.practicum.android.diploma.search.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.core.domain.model.SearchVacanciesResult
import ru.practicum.android.diploma.databinding.FragmentSearchBinding
import ru.practicum.android.diploma.search.domain.usecase.SearchVacancyUseCase
import ru.practicum.android.diploma.search.presentation.SearchState
import ru.practicum.android.diploma.search.presentation.SearchStatus
import ru.practicum.android.diploma.search.presentation.SearchViewModel

class SearchFragment : Fragment() {
    private val viewModel by viewModel<SearchViewModel>()
    private var vacancyAdapter: VacancyAdapter = VacancyAdapter { vacancy ->
        transitionToDetailedVacancy(vacancy.id)
    }
    private var isWasPaginationError = false
    private var isLastPageReached = false
    private var onScrollLister: RecyclerView.OnScrollListener? = null
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var vacancyAmount: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }
        setFragmentResultListener(SEARCH_FRAGMENT_KEY) { _, bundle ->
            if (bundle.containsKey(FILTER_APPLIED_KEY)) {
                viewModel.searchByButton(binding.searchEditText.text?.toString() ?: "")
            }
        }
        setupListeners()
        renderFilterStatus()
    }

    private fun setupListeners() {
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.searchByText(binding.searchEditText.text.toString())
            if (text.isNullOrEmpty()) {
                binding.icSearchOrCross.setImageResource(R.drawable.ic_search)
            } else {
                binding.icSearchOrCross.setImageResource(R.drawable.ic_close)
            }
        }
        binding.icSearchOrCross.setOnClickListener {
            binding.searchEditText.text = null
            viewModel.clearSearch()
        }
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.searchByButton(binding.searchEditText.text.toString())
                val inputMethodManager =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
            }
            true
        }
        binding.vacancyRecycler.adapter = vacancyAdapter
        binding.vacancyRecycler.layoutManager = LinearLayoutManager(requireContext())
        setPaginationListener()
        binding.searchToolbar.setOnMenuItemClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_filterFragment)
            true
        }
    }

    private fun setPaginationListener() {
        onScrollLister = object : RecyclerView.OnScrollListener() {
            private var lastVisibleItem = -1

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isLastPageReached) {
                    binding.paginationProgressBar.visibility = View.GONE
                    return
                }
                val currentLastVisibleItem = (recyclerView.layoutManager as LinearLayoutManager)
                    .findLastVisibleItemPosition()
                if (isTimeToGetNextPage(currentLastVisibleItem) && currentLastVisibleItem != lastVisibleItem) {
                    if (isScrolledToLastItem(currentLastVisibleItem) && !isWasPaginationError) {
                        binding.paginationProgressBar.show()
                    } else {
                        binding.paginationProgressBar.visibility = View.GONE
                    }
                    viewModel.searchByPage(
                        searchText = binding.searchEditText.text.toString(),
                        page = getNextPageIndex(),
                    )
                } else if (currentLastVisibleItem != lastVisibleItem) {
                    binding.paginationProgressBar.visibility = View.GONE
                }
                lastVisibleItem = currentLastVisibleItem
            }
        }
        onScrollLister?.let { binding.vacancyRecycler.addOnScrollListener(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onScrollLister?.let { binding.vacancyRecycler.removeOnScrollListener(it) }
    }

    private fun isTimeToGetNextPage(lastVisibleItem: Int): Boolean {
        return lastVisibleItem > vacancyAdapter.itemCount - PRE_PAGINATION_ITEM_COUNT
    }

    private fun getNextPageIndex(): Int {
        return vacancyAdapter.itemCount / SearchVacancyUseCase.DEFAULT_VACANCIES_PER_PAGE
    }

    private fun isScrolledToLastItem(lastVisibleItem: Int): Boolean {
        return lastVisibleItem + 1 >= vacancyAdapter.itemCount
    }

    private fun render(it: SearchState) {
        when (it) {
            is SearchState.Default -> {
                vacancyAdapter.updateList(emptyList())
                setStatus(SearchStatus.DEFAULT)
            }

            is SearchState.Loading -> {
                vacancyAdapter.updateList(emptyList())
                setStatus(SearchStatus.PROGRESS)
            }

            is SearchState.Content -> showContent(it.data!!)
            is SearchState.Pagination -> updateContent(it.data, it.error)
            is SearchState.ServerError -> {
                showError(R.drawable.placeholder_no_internet, R.string.placeholder_no_internet)
            }

            is SearchState.NetworkError -> {
                showError(R.drawable.placeholder_server_error, R.string.placeholder_server_error)
            }

            is SearchState.EmptyResult -> {
                showError(R.drawable.placeholder_nothing_found, R.string.placeholder_cannot_get_list_of_vacancy)
            }
        }
    }

    private fun updateContent(result: SearchVacanciesResult, error: SearchState?) {
        binding.paginationProgressBar.visibility = View.GONE
        if (result.vacancies.isNotEmpty()) {
            isWasPaginationError = false
            isLastPageReached = result.vacancies.size < SearchVacancyUseCase.DEFAULT_VACANCIES_PER_PAGE
            vacancyAdapter.pagination(result.vacancies)
            binding.tvVacancyAmount.text =
                requireContext().resources.getQuantityString(
                    R.plurals.vacancies,
                    vacancyAmount ?: 0,
                    vacancyAmount ?: 0
                )
        }
        if (result.numOfResults != 0) {
            setLblVacancyAmount(result.numOfResults)
        }
        if (error is SearchState.NetworkError) {
            isWasPaginationError = true
            binding.paginationProgressBar.visibility = View.GONE
            showToast(getString(R.string.pagination_error_error_connection))
        } else if (error is SearchState.ServerError) {
            isWasPaginationError = true
            binding.paginationProgressBar.visibility = View.GONE
            showToast(getString(R.string.pagination_error_server_error))
        }
    }

    private fun showToast(text: String) = Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()

    private fun showContent(searchVacanciesResult: SearchVacanciesResult) {
        isLastPageReached = searchVacanciesResult.vacancies.size < SearchVacancyUseCase.DEFAULT_VACANCIES_PER_PAGE
        vacancyAdapter.updateList(searchVacanciesResult.vacancies)
        setLblVacancyAmount(searchVacanciesResult.numOfResults)
        vacancyAmount = searchVacanciesResult.numOfResults
        setStatus(SearchStatus.SUCCESS)
    }

    private fun setLblVacancyAmount(numOfResults: Int) {
        binding.tvVacancyAmount.text =
            requireContext().resources.getQuantityString(R.plurals.vacancies, numOfResults, numOfResults)
    }

    private fun showError(@DrawableRes placeHolderId: Int, @StringRes placeHolderMsg: Int) {
        binding.errorPlaceholder.setImageResource(placeHolderId)
        binding.placeholderText.text = getString(placeHolderMsg)
        vacancyAdapter.updateList(emptyList())
        setStatus(SearchStatus.ERROR)
    }

    private fun setStatus(status: SearchStatus) {
        when (status) {
            SearchStatus.PROGRESS -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.placeholderSearch.visibility = View.GONE
                binding.vacancyRecycler.visibility = View.GONE
                binding.placeholderText.visibility = View.GONE
                binding.errorPlaceholder.visibility = View.GONE
                binding.tvVacancyAmount.visibility = View.GONE
            }

            SearchStatus.ERROR -> {
                binding.progressBar.visibility = View.GONE
                binding.placeholderSearch.visibility = View.GONE
                binding.vacancyRecycler.visibility = View.GONE
                binding.placeholderText.visibility = View.VISIBLE
                binding.errorPlaceholder.visibility = View.VISIBLE
                binding.tvVacancyAmount.visibility = View.GONE
            }

            SearchStatus.SUCCESS -> {
                binding.progressBar.visibility = View.GONE
                binding.placeholderSearch.visibility = View.GONE
                binding.vacancyRecycler.visibility = View.VISIBLE
                binding.placeholderText.visibility = View.GONE
                binding.errorPlaceholder.visibility = View.GONE
                binding.tvVacancyAmount.visibility = View.VISIBLE
                binding.paginationProgressBar.visibility = View.GONE
            }

            SearchStatus.DEFAULT -> {
                binding.progressBar.visibility = View.GONE
                binding.placeholderSearch.visibility = View.VISIBLE
                binding.vacancyRecycler.visibility = View.GONE
                binding.placeholderText.visibility = View.GONE
                binding.errorPlaceholder.visibility = View.GONE
                binding.tvVacancyAmount.visibility = View.GONE
            }
        }
    }

    private fun renderFilterStatus() {
        if (viewModel.isFilterApplied()) {
            binding.searchToolbar.menu.findItem(R.id.menu_logo).setIcon(R.drawable.ic_filter_on)
        } else {
            binding.searchToolbar.menu.findItem(R.id.menu_logo).setIcon(R.drawable.ic_filter)
        }
    }

    private fun transitionToDetailedVacancy(vacancyId: Long) {
        if (viewModel.clickDebounce()) {
            val action = SearchFragmentDirections.actionSearchFragmentToVacancyFragment(vacancyId)
            findNavController().navigate(action)
        }
    }

    companion object {
        private const val PRE_PAGINATION_ITEM_COUNT = 5
        const val SEARCH_FRAGMENT_KEY = "SEARCH_FRAGMENT_KEY"
        const val FILTER_APPLIED_KEY = "FILTER_APPLIED_KEY"
    }
}
