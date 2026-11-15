package com.example.data.catalog

import com.example.domain.model.Trail

object VerifiedIndianTrailsCatalog {
    val allTrails: List<Trail> by lazy {
        VerifiedTrailsHimalayasNortheast.trails + VerifiedTrailsGhatsSouth.trails
    }
}
