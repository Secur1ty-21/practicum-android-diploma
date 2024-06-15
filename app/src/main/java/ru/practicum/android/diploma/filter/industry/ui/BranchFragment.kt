package ru.practicum.android.diploma.filter.industry.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentBranchBinding
import ru.practicum.android.diploma.filter.industry.domain.model.Industry
import ru.practicum.android.diploma.filter.industry.presentation.BranchAdapter
import ru.practicum.android.diploma.filter.industry.presentation.BranchScreenState
import ru.practicum.android.diploma.filter.industry.presentation.BranchViewModel

class BranchFragment : Fragment() {
    private var _binding: FragmentBranchBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<BranchViewModel>()
    private var branchAdapter: BranchAdapter = BranchAdapter { industry ->
        viewModel.onSelectIndustryEvent(industry)
    }
    private var selectedIndustry: Industry? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBranchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getBranches(binding.search.text.toString(), selectedIndustry)
        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }
        initListeners()
        binding.branchRecycler.adapter = branchAdapter
        binding.branchRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.branchRecycler.isVisible = true
        binding.btnSave.isVisible = false
    }

    private fun render(branchScreenState: BranchScreenState) {
        when (branchScreenState) {
            is BranchScreenState.Error -> {
                binding.errorPlaceholder.setImageDrawable(
                    AppCompatResources.getDrawable(requireContext(), R.drawable.placeholder_region_response_error)
                )
                binding.placeholderText.text = getString(R.string.placeholder_cannot_get_list_of_regions)
                showError()
            }

            is BranchScreenState.Content -> {
                binding.errorPlaceholder.isVisible = false
                binding.placeholderText.isVisible = false
                showContent(branchScreenState)
            }

            is BranchScreenState.Empty -> {
                binding.errorPlaceholder.setImageDrawable(
                    AppCompatResources.getDrawable(requireContext(), R.drawable.placeholder_nothing_found)
                )
                binding.placeholderText.text = getString(R.string.placeholder_no_such_industry)
                showError()
            }
        }
    }

    private fun showError() {
        binding.branchRecycler.isVisible = false
        binding.btnSave.isVisible = false
        binding.errorPlaceholder.isVisible = true
        binding.placeholderText.isVisible = true
    }

    private fun showContent(content: BranchScreenState.Content) {
        val newIndustries = content.branches.map { it to false }.toMutableList()
        if (content.selectedIndustry != null) {
            for (i in newIndustries.indices) {
                if (selectedIndustry?.id == newIndustries[i].first.id) {
                    newIndustries[i] = newIndustries[i].copy(second = false)
                    break
                }
            }
            for (i in newIndustries.indices) {
                if (content.selectedIndustry.id == newIndustries[i].first.id) {
                    newIndustries[i] = newIndustries[i].copy(second = true)
                    break
                }
            }
            selectedIndustry = content.selectedIndustry
            binding.btnSave.isVisible = true
        } else {
            binding.btnSave.isVisible = false
        }
        branchAdapter.branches.clear()
        branchAdapter.branches.addAll(newIndustries)
        branchAdapter.notifyDataSetChanged()
        binding.branchRecycler.isVisible = true
    }

    private fun initListeners() {
        binding.filterToolbarBranch.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnSave.setOnClickListener {
            if (viewModel.clickDebounce()) {
                selectedIndustry?.let { industry ->
                    viewModel.saveIndustry(industry)
                }
                findNavController().popBackStack()
            }
        }
        binding.search.doOnTextChanged { text, _, _, _ ->
            viewModel.getBranches(binding.search.text.toString(), selectedIndustry)
            if (text.isNullOrEmpty()) {
                binding.btnClear.setImageResource(R.drawable.ic_search)
            } else {
                binding.btnClear.setImageResource(R.drawable.ic_close)
            }
        }
        binding.btnClear.setOnClickListener {
            binding.search.text = null
        }
        binding.search.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.getBranches(binding.search.text.toString(), selectedIndustry)
                val inputMethodManager =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(binding.search.windowToken, 0)
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
