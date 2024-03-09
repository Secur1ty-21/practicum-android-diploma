package ru.practicum.android.diploma.filter.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding
import ru.practicum.android.diploma.filter.presentation.FilterState
import ru.practicum.android.diploma.filter.presentation.FilterViewModel
import ru.practicum.android.diploma.search.ui.SearchFragment

class FilterFragment : Fragment() {
    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<FilterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        getData()
    }

    private fun initListeners() {
        binding.filterToolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.placeOfJobNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_filterFragment_to_placeSelectorFragment)
        }
        binding.branchOfJobNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_filterFragment_to_branchFragment)
        }
        changeIcon(binding.branchOfJobText, binding.branchOfJobIcon)
        changeIcon(binding.placeOfJobText, binding.placeOfJobIcon)
        binding.salaryInputEditText.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                binding.clearInput.visibility = View.GONE
            } else {
                binding.clearInput.visibility = View.VISIBLE
            }
            viewModel.onInputSalaryEvent(text.toString())
            setFilterBtnVisibility(viewModel.state.value ?: FilterState())
        }
        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onCheckChangeEvent(isChecked)
            setFilterBtnVisibility(viewModel.state.value ?: FilterState())
        }
        binding.clearInput.setOnClickListener { onBtnClearClick() }
        binding.btApproveFilters.setOnClickListener { onBtnApproveClick() }
        binding.btResetFilters.setOnClickListener {
            viewModel.onBtnResetClickEvent()
        }
    }

    private fun onBtnApproveClick() {
        viewModel.onBtnSaveClickEvent(
            salary = binding.salaryInputEditText.text?.toString() ?: "",
            isChecked = binding.checkbox.isChecked
        )
        setFragmentResult(
            SearchFragment.SEARCH_FRAGMENT_KEY,
            bundleOf(SearchFragment.FILTER_APPLIED_KEY to true)
        )
        findNavController().popBackStack()
    }

    private fun onBtnClearClick() {
        binding.salaryInputEditText.setText("")
        setFilterBtnVisibility(viewModel.state.value?.copy(salary = "") ?: FilterState())
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(binding.salaryInputEditText.windowToken, 0)
    }

    private fun getData() {
        viewModel.init()
        viewModel.state.observe(viewLifecycleOwner) {
            binding.placeOfJobText.setText(it.country?.name ?: "")
            if (it.country != null && it.area != null) {
                binding.placeOfJobText.setText(
                    getString(
                        R.string.tv_job_title_and_region,
                        it.country.name,
                        it.area.name
                    )
                )
            } else if (it.area != null) {
                binding.placeOfJobText.setText(it.area.name)
            }
            binding.branchOfJobText.setText(it.industry?.name ?: "")
            binding.checkbox.isChecked = it.isNotShowWithoutSalary
            binding.salaryInputEditText.setText(if (it.salary == null) "" else it.salary.toString())
            setFilterBtnVisibility(it)
        }
    }

    private fun setFilterBtnVisibility(filterState: FilterState) {
        if (isAvailableApplyFilters(filterState)) {
            binding.btResetFilters.isVisible = true
            binding.btApproveFilters.isVisible = true
        } else {
            binding.btResetFilters.isVisible = false
            binding.btApproveFilters.isVisible = false
        }
    }

    private fun isAvailableApplyFilters(state: FilterState): Boolean {
        return state.country != null || state.area != null || !binding.salaryInputEditText.text.isNullOrEmpty()
            || binding.checkbox.isChecked || state.industry != null
    }

    private fun changeIcon(editText: EditText, view: ImageView) {
        editText.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }
    }
}
