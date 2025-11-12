package com.example.brigadeapp.di

import com.example.brigadeapp.data.repository.FirebaseAuthRepository
import com.example.brigadeapp.data.repository.FirestoreUserRepository
import com.example.brigadeapp.domain.repository.AuthRepository
import com.example.brigadeapp.domain.repository.UserRepository
import com.example.brigadeapp.domain.usecase.GetCurrentUserUseCase
import com.example.brigadeapp.domain.usecase.ObserveAuthStateUseCase
import com.example.brigadeapp.domain.usecase.RegisterWithEmail
import com.example.brigadeapp.domain.usecase.SendEmailVerification
import com.example.brigadeapp.domain.usecase.SendPasswordResetEmail
import com.example.brigadeapp.domain.usecase.SignInWithEmail
import com.example.brigadeapp.viewmodel.utils.AuthErrorMapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.brigadeapp.domain.entity.AuthClient
import com.example.brigadeapp.domain.entity.FirebaseAuthClient


@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    // FirebaseAuth
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideAuthClient(): AuthClient = FirebaseAuthClient()


    // Repositories
    @Singleton
    @Provides
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository =
        FirebaseAuthRepository(auth)

    @Singleton
    @Provides
    fun provideUserRepository(db: FirebaseFirestore): UserRepository =
        FirestoreUserRepository(db)

    // Use cases
    @Singleton @Provides fun provideSignInWithEmail(repo: AuthRepository) =
        SignInWithEmail(repo)

    @Singleton @Provides fun provideRegisterWithEmail(authRepo: AuthRepository, userRepo: UserRepository) =
        RegisterWithEmail(authRepo, userRepo)

    @Singleton @Provides fun provideSendPasswordResetEmail(repo: AuthRepository) =
        SendPasswordResetEmail(repo)

    @Singleton @Provides fun provideSendEmailVerification(repo: AuthRepository) =
        SendEmailVerification(repo) // ← añade este binding

    @Singleton @Provides fun provideObserveAuthStateUseCase(repo: AuthRepository) =
        ObserveAuthStateUseCase(repo)

    @Singleton @Provides fun provideGetCurrentUserUseCase(repo: AuthRepository) =
        GetCurrentUserUseCase(repo)

    @Singleton @Provides
    fun provideAuthErrorMapper(): com.example.brigadeapp.viewmodel.utils.AuthErrorMapper =
        com.example.brigadeapp.viewmodel.utils.AuthErrorMapper()

}
