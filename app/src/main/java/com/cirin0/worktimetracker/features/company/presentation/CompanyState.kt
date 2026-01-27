package com.cirin0.worktimetracker.features.company.presentation

import com.cirin0.worktimetracker.features.company.data.model.CompanyDetail

data class CompanyState(
    val isLoading: Boolean = false,
    val company: CompanyDetail? = null,
    val error: String? = null
)
