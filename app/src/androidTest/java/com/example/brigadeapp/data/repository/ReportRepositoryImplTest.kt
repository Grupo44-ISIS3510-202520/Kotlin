package com.example.brigadeapp.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.brigadeapp.domain.entity.Report
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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


}
