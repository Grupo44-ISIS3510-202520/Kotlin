package com.example.brigadeapp.model.usecase

import com.example.brigadeapp.model.repository.interfaces.ProtocolRepository
import javax.inject.Inject

class GetUpdatedProtocolsUseCase @Inject constructor(
     val repo: ProtocolRepository
) {
    suspend operator fun invoke(lastSession: Long): Int {
        val protocols = repo.getProtocols()
        return protocols.size
    }
}
