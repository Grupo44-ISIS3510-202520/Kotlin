package com.example.brigadeapp.data.repository

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.brigadeapp.domain.model.Report
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportRepositoryImplTest {

    private lateinit var repository: ReportRepositoryImpl
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    @Before
    fun setup() {
        FirebaseApp.initializeApp(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        repository = ReportRepositoryImpl(firestore, storage)
    }

    @Test
    fun uploadReportToFirestoreAndStorage() = runBlocking {
        val fakeReport = Report(
            type = "Incendio",
            place = "Bloque B",
            time = "10:45",
            description = "Simulación de prueba de subida de reporte",
            followUp = true,
            imageUri = null, // si quieres probar imagen coloca un Uri válido
            audioUri = null,
            timestamp = System.currentTimeMillis()
        )

        repository.submitReport(fakeReport)
        println("✅ Reporte subido correctamente a Firestore")
    }
}
