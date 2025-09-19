package com.cognitiveassistant.viewmodel

import androidx.lifecycle.ViewModel
import com.cognitiveassistant.model.Patient
import com.cognitiveassistant.repository.InMemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PatientViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    init {
        val generatedId = InMemoryRepository.generatePatientId()
        _uiState.value = _uiState.value.copy(patientId = generatedId)
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, errorMessage = null)
    }

    fun updateAge(age: String) {
        _uiState.value = _uiState.value.copy(age = age, errorMessage = null)
    }

    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phoneNumber, errorMessage = null)
    }

    fun savePatient(onSuccess: (Patient) -> Unit) {
        val state = _uiState.value

        if (state.name.isBlank() || state.age.isBlank() || state.phoneNumber.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please fill all fields")
            return
        }

        val ageInt = state.age.toIntOrNull()
        if (ageInt == null || ageInt <= 0) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid age")
            return
        }

        val patient = Patient(
            id = state.patientId,
            name = state.name,
            age = ageInt,
            phoneNumber = state.phoneNumber
        )

        InMemoryRepository.savePatient(patient)
        onSuccess(patient)
    }
}

data class PatientUiState(
    val patientId: String = "",
    val name: String = "",
    val age: String = "",
    val phoneNumber: String = "",
    val errorMessage: String? = null
)