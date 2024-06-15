package ru.practicum.android.diploma.filter.industry.domain.usecase

import ru.practicum.android.diploma.filter.domain.api.FilterRepository
import ru.practicum.android.diploma.filter.domain.models.FilterType
import ru.practicum.android.diploma.filter.industry.domain.model.Industry

class GetIndustryFilterUseCase(
    private val filterRepository: FilterRepository
) {
    fun execute(): Industry? {
        val filter = filterRepository.getFilters().find { it is FilterType.Industry }
        return if (filter is FilterType.Industry) {
            Industry(id = filter.id, name = filter.name)
        } else {
            null
        }
    }
}
