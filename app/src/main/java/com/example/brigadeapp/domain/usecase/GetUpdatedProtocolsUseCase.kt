package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.repository.ProtocolRepository
import javax.inject.Inject

class GetUpdatedProtocolsUseCase @Inject constructor(
     val repo: ProtocolRepository
) {
    suspend operator fun invoke(lastSession: Long): Int {
        val protocols = repo.getProtocols()
        return protocols.size
    }
}
