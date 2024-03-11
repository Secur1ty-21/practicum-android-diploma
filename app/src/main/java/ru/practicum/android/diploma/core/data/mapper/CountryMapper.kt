package ru.practicum.android.diploma.core.data.mapper

import ru.practicum.android.diploma.core.data.network.dto.CountryDto
import ru.practicum.android.diploma.core.domain.model.Country

fun mapToDomain(country: CountryDto): Country {
    return with(country) {
        Country(
            id = id,
            name = name
        )
    }
}
