package ru.practicum.android.diploma.filter.industry.presentation

import ru.practicum.android.diploma.filter.industry.domain.model.Industry

sealed class BranchScreenState {
    class Content(
        val branches: ArrayList<Industry>,
        val selectedIndustry: Industry?,
    ) : BranchScreenState()

    data object Error : BranchScreenState()
    data object Empty : BranchScreenState()
}
