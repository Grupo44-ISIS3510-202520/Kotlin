package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.entity.Protocol
import com.example.brigadeapp.domain.repository.ProtocolRepository
import javax.inject.Inject

class GetUpdatedProtocolsUseCase @Inject constructor(
    private val repo: ProtocolRepository
) {
    suspend operator fun invoke(localVersions: Map<String, Int>): List<Protocol> {
        return repo.getUpdatedProtocols(localVersions)
    }
}