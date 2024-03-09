package ru.practicum.android.diploma.core.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.core.data.NetworkClient
import ru.practicum.android.diploma.core.data.NetworkClient.Companion.EXCEPTION_ERROR_CODE
import ru.practicum.android.diploma.core.data.NetworkClient.Companion.NETWORK_ERROR_CODE
import ru.practicum.android.diploma.core.data.NetworkClient.Companion.SUCCESSFUL_CODE
import ru.practicum.android.diploma.core.data.network.dto.CountryResponse
import ru.practicum.android.diploma.core.data.network.dto.GetAreasResponse
import ru.practicum.android.diploma.core.data.network.dto.GetIndustriesResponse
import ru.practicum.android.diploma.core.data.network.dto.Response
import ru.practicum.android.diploma.core.domain.model.SearchFilterParameters
import java.io.IOException
import java.net.SocketTimeoutException

class RetrofitNetworkClient(
    private val hhApi: HhApi,
    private val connectionChecker: ConnectionChecker,
) : NetworkClient {

    override suspend fun getVacanciesByPage(
        searchText: String,
        filterParameters: SearchFilterParameters,
        page: Int,
        perPage: Int
    ): Response {
        return executeRequestGetVacanciesByPage(searchText, filterParameters, page, perPage, hhApi)
    }

    private suspend fun executeRequestGetVacanciesByPage(
        searchText: String,
        filterParameters: SearchFilterParameters,
        page: Int,
        perPage: Int,
        hhApi: HhApi
    ): Response {
        val queryMap = mutableMapOf(
            HhApiQuery.PAGE.value to page.toString(),
            HhApiQuery.PER_PAGE.value to perPage.toString(),
            HhApiQuery.SEARCH_TEXT.value to searchText
        )
        queryMap.putAll(getQueryFilterMap(filterParameters))
        return runRequestOnIoThreads { hhApi.getVacancies(queryMap) }
    }

    private fun getQueryFilterMap(filterParameters: SearchFilterParameters): Map<String, String> {
        val filterMap = mutableMapOf<String, String>()
        if (filterParameters.salary.isNotEmpty()) {
            filterMap[HhApiQuery.SALARY_FILTER.value] = filterParameters.salary
        }
        if (filterParameters.industriesId.isNotEmpty()) {
            filterMap[HhApiQuery.INDUSTRY_FILTER.value] = filterParameters.industriesId
        }
        if (filterParameters.regionId.isNotEmpty()) {
            filterMap[HhApiQuery.REGION_FILTER.value] = filterParameters.regionId
        }
        if (filterParameters.isOnlyWithSalary) {
            filterMap[HhApiQuery.IS_ONLY_WITH_SALARY_FILTER.value] = "true"
        }
        return filterMap
    }

    override suspend fun getDetailVacancyById(id: Long): Response {
        return runRequestOnIoThreads { hhApi.getVacancy(id) }
    }

    override suspend fun getCountries(): Response {
        return runRequestOnIoThreads {
            val retrofitResponse = hhApi.getCountries()
            if (retrofitResponse.isSuccessful) {
                retrofit2.Response.success(CountryResponse(retrofitResponse.body() ?: emptyList()))
            } else {
                retrofit2.Response.error(retrofitResponse.code(), retrofitResponse.errorBody()!!)
            }
        }
    }

    override suspend fun getIndustries(): Response {
        return runRequestOnIoThreads {
            val retrofitResponse = hhApi.getIndustries()
            if (retrofitResponse.isSuccessful) {
                retrofit2.Response.success(GetIndustriesResponse(retrofitResponse.body() ?: emptyList()))
            } else {
                retrofit2.Response.error(retrofitResponse.code(), retrofitResponse.errorBody()!!)
            }
        }
    }

    override suspend fun getAreas(): Response {
        return runRequestOnIoThreads {
            val retrofitResponse = hhApi.getAreas()
            if (retrofitResponse.isSuccessful) {
                retrofit2.Response.success(GetAreasResponse(retrofitResponse.body() ?: emptyList()))
            } else {
                retrofit2.Response.error(retrofitResponse.code(), retrofitResponse.errorBody()!!)
            }
        }
    }

    private suspend fun <T : Response> runRequestOnIoThreads(request: suspend () -> retrofit2.Response<T>): Response {
        return withContext(Dispatchers.IO) {
            if (!connectionChecker.isConnected()) {
                Response().apply { resultCode = NETWORK_ERROR_CODE }
            } else {
                getResponse { request() }
            }
        }
    }

    private suspend fun <T : Response> getResponse(request: suspend () -> retrofit2.Response<T>): Response {
        return try {
            val retrofitResponse = request()
            val body = retrofitResponse.body()
            if (retrofitResponse.isSuccessful && body != null) {
                body.apply { resultCode = SUCCESSFUL_CODE }
            } else {
                Response().apply { resultCode = retrofitResponse.code() }
            }
        } catch (e: SocketTimeoutException) {
            Log.e(RetrofitNetworkClient::class.java.simpleName, e.stackTraceToString())
            Response().apply { resultCode = NETWORK_ERROR_CODE }
        } catch (e: IOException) {
            Log.e(RetrofitNetworkClient::class.java.simpleName, e.stackTraceToString())
            Response().apply { resultCode = EXCEPTION_ERROR_CODE }
        }
    }
}
