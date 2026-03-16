package com.cirin0.worktimetracker.features.company.data.repository

import com.cirin0.worktimetracker.core.database.dao.CompanyDao
import com.cirin0.worktimetracker.core.database.entity.toCachedEntity
import com.cirin0.worktimetracker.core.database.entity.toCompanyDetail
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
    private val companyDao: CompanyDao,
    private val connectivityObserver: ConnectivityObserver,
    @param:Named(Constants.NAMED_IMAGE_URL) private val imageBaseUrl: String
) {
    suspend fun getCompanyById(companyId: Int): ApiResponse<CompanyDetail> {
        if (!connectivityObserver.isConnected()) {
            val cachedCompany = companyDao.getCompany()
            return if (cachedCompany != null && cachedCompany.companyId == companyId) {
                ApiResponse.Success(cachedCompany.toCompanyDetail(), fromCache = true)
            } else {
                ApiResponse.Error("Немає підключення до інтернету")
            }
        }

        val result = apiCall {
            val rawCompany = companyApi.getCompanyById(companyId)
            val company = rawCompany.copy(
                logo = rawCompany.logo?.let { path ->
                    if (path.startsWith("http")) path else "$imageBaseUrl$path"
                },
                manager = rawCompany.manager.copy(
                    avatar = rawCompany.manager.avatar?.let { path ->
                        if (path.startsWith("http")) path else "$imageBaseUrl$path"
                    }
                ),
                employees = rawCompany.employees?.map { employee ->
                    employee.copy(
                        avatar = employee.avatar?.let { path ->
                            if (path.startsWith("http")) path else "$imageBaseUrl$path"
                        }
                    )
                }
            )

            companyDao.insertCompany(company.toCachedEntity())
            company
        }

        return if (result is ApiResponse.Error) {
            val cachedCompany = companyDao.getCompany()
            if (cachedCompany != null && cachedCompany.companyId == companyId) {
                ApiResponse.Success(cachedCompany.toCompanyDetail(), fromCache = true)
            } else {
                result
            }
        } else {
            result
        }
    }
}
