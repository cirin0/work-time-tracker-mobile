package com.cirin0.worktimetracker.features.company.data.repository

import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.core.utils.ConnectivityObserver
import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.company.data.api.CompanyApi
import com.cirin0.worktimetracker.features.company.data.model.CompanyDetail
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton

@Singleton
class CompanyRepository @Inject constructor(
    private val companyApi: CompanyApi,
    private val connectivityObserver: ConnectivityObserver,
    @param:Named(Constants.NAMED_IMAGE_URL) private val imageBaseUrl: String
) {
    suspend fun getCompanyById(companyId: Int): ApiResponse<CompanyDetail> {
        if (!connectivityObserver.isConnected()) {
            return ApiResponse.Error("Немає підключення до інтернету")
        }

        return apiCall {
            val company = companyApi.getCompanyById(companyId)
            company.copy(
                logo = company.logo?.let { path ->
                    if (path.startsWith("http")) path
                    else "$imageBaseUrl$path"
                },
                employees = company.employees.map { employee ->
                    employee.copy(
                        avatar = employee.avatar?.let { path ->
                            if (path.startsWith("http")) path
                            else "$imageBaseUrl$path"
                        }
                    )
                }
            )
        }
    }
}
