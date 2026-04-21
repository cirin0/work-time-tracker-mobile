package com.cirin0.worktimetracker.features.company.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.company.data.model.CompanyDetail
import retrofit2.http.GET

interface CompanyApi {
    @GET(Constants.ApiRoutes.COMPANY)
    suspend fun getCompany(): CompanyDetail
}
