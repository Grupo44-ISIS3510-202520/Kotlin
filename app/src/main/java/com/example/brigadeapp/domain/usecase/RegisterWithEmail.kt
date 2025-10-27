package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.entity.UserProfile
import com.example.brigadeapp.domain.repository.AuthRepository
import com.example.brigadeapp.domain.repository.UserRepository
import com.example.brigadeapp.domain.utils.validateBloodGroup
import com.example.brigadeapp.domain.utils.validateEmailDomain
import com.example.brigadeapp.domain.utils.validateLastName
import com.example.brigadeapp.domain.utils.validateName
import com.example.brigadeapp.domain.utils.validatePassword
import com.example.brigadeapp.domain.utils.validatePasswordConfirm
import com.example.brigadeapp.domain.utils.validateRole
import com.example.brigadeapp.domain.utils.validateUniandesCode
import javax.inject.Inject

class RegisterWithEmail @Inject constructor(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        lastName: String,
        uniandesCode: String,
        bloodGroup: String,
        role: String
    ) {
        val problems = listOfNotNull(
            validateEmailDomain(email),
            validatePassword(password),
            validatePasswordConfirm(confirmPassword, password),
            validateName(name),
            validateLastName(lastName),
            validateUniandesCode(uniandesCode),
            validateBloodGroup(bloodGroup),
            validateRole(role),
        )
        if (problems.isNotEmpty()) throw IllegalArgumentException(problems.first())

        val authUser = authRepo.registerWithEmail(email.trim(), password)

        val profile = UserProfile(
            uid = authUser.uid,
            email = email.trim(),
            name = name.trim(),
            lastName = lastName.trim(),
            uniandesCode = uniandesCode.trim(),
            bloodGroup = bloodGroup,
            role = role
        )
        userRepo.saveProfile(profile)
    }
}
