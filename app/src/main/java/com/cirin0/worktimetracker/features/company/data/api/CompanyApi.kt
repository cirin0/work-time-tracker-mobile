package com.cirin0.worktimetracker.features.company.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.company.data.model.CompanyDetail
import retrofit2.http.GET
import retrofit2.http.Path

interface CompanyApi {
    @GET(Constants.ApiRoutes.COMPANIES + "/{id}")
    suspend fun getCompanyById(@Path("id") id: Int): CompanyDetail
}
