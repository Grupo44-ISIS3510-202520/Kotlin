package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.repository.ProtocolRepository

class GetUpdatedProtocolsUseCase(
    private val repo: ProtocolRepository
) {
    suspend operator fun invoke(sinceTs: Long): Int {
        return repo.getUpdatedSince(sinceTs).size
    }
}
