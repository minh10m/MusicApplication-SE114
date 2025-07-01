package com.example.musicapplicationse114.ui.screen.forget_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.ChangePasswordRequest
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgetPasswordUiState(
    val email: String = "",
    val otp: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val currentStep: ForgetPasswordStep = ForgetPasswordStep.EMAIL_VERIFICATION,
    val isShowPassword: Boolean = false,
    val isShowConfirmPassword: Boolean = false,
    val successMessage: String = "",
    val status: LoadStatus = LoadStatus.Init()
)

enum class ForgetPasswordStep {
    EMAIL_VERIFICATION,
    OTP_VERIFICATION,
    CHANGE_PASSWORD,
    SUCCESS
}

@HiltViewModel
class ForgetPasswordViewModel @Inject constructor(
    private val api: Api?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updateOtp(otp: String) {
        _uiState.value = _uiState.value.copy(otp = otp)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    fun toggleShowPassword() {
        _uiState.value = _uiState.value.copy(isShowPassword = !_uiState.value.isShowPassword)
    }

    fun toggleShowConfirmPassword() {
        _uiState.value = _uiState.value.copy(isShowConfirmPassword = !_uiState.value.isShowConfirmPassword)
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(status = LoadStatus.Init())
    }

    fun goToNextStep() {
        when (_uiState.value.currentStep) {
            ForgetPasswordStep.EMAIL_VERIFICATION -> {
                _uiState.value = _uiState.value.copy(currentStep = ForgetPasswordStep.OTP_VERIFICATION)
            }
            ForgetPasswordStep.OTP_VERIFICATION -> {
                _uiState.value = _uiState.value.copy(currentStep = ForgetPasswordStep.CHANGE_PASSWORD)
            }
            ForgetPasswordStep.CHANGE_PASSWORD -> {
                _uiState.value = _uiState.value.copy(currentStep = ForgetPasswordStep.SUCCESS)
            }
            ForgetPasswordStep.SUCCESS -> {
                // Do nothing, already at the final step
            }
        }
    }

    fun goBackToEmailStep() {
        _uiState.value = _uiState.value.copy(
            currentStep = ForgetPasswordStep.EMAIL_VERIFICATION,
            status = LoadStatus.Init()
        )
    }

    fun goBackToOtpStep() {
        _uiState.value = _uiState.value.copy(
            currentStep = ForgetPasswordStep.OTP_VERIFICATION,
            status = LoadStatus.Init()
        )
    }

    fun verifyEmail() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                
                // Real API call
                val result = api?.verifyEmail(_uiState.value.email)
                
                android.util.Log.d("ForgetPassword", "API Response Code: ${result?.code()}")
                android.util.Log.d("ForgetPassword", "API Response Success: ${result?.isSuccessful}")
                android.util.Log.d("ForgetPassword", "API Response Body: ${result?.body()}")
                android.util.Log.d("ForgetPassword", "API Response Error: ${result?.errorBody()?.string()}")
                
                if (result != null && result.isSuccessful) {
                    val responseText = result.body()?.string() ?: "Email verification sent successfully"
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Success(),
                        successMessage = responseText,
                        currentStep = ForgetPasswordStep.OTP_VERIFICATION
                    )
                } else {
                    // Log the error response for debugging
                    val errorBody = result?.errorBody()?.string() ?: "Unknown error"
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Failed to send verification email: $errorBody")
                    )
                }
            } catch (ex: Exception) {
                // Log the exception for debugging
                android.util.Log.e("ForgetPassword", "Exception: ${ex.message}", ex)
                val errorMessage = ex.message ?: "Unknown error occurred"
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error("Network error: $errorMessage")
                )
            }
        }
    }

    fun verifyOtp() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val otpInt = _uiState.value.otp.toIntOrNull()
                if (otpInt == null) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Invalid OTP format"))
                    return@launch
                }
                
                // Real API call
                val result = api?.verifyOtp(otpInt, _uiState.value.email)
                if (result != null && result.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Success(),
                        currentStep = ForgetPasswordStep.CHANGE_PASSWORD
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error(result?.errorBody()?.string() ?: "Invalid OTP")
                    )
                }
            } catch (ex: Exception) {
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error(ex.message ?: "Unknown error occurred")
                )
            }
        }
    }

    fun changePassword() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                
                if (_uiState.value.password != _uiState.value.confirmPassword) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Passwords do not match"))
                    return@launch
                }

                if (_uiState.value.password.isEmpty()) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Password cannot be empty"))
                    return@launch
                }

                // Real API call
                val request = ChangePasswordRequest(_uiState.value.password, _uiState.value.confirmPassword)
                val result = api?.changePassword(_uiState.value.email, request)
                
                if (result != null && result.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Success(),
                        currentStep = ForgetPasswordStep.SUCCESS
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error(result?.errorBody()?.string() ?: "Failed to change password")
                    )
                }
            } catch (ex: Exception) {
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error(ex.message ?: "Unknown error occurred")
                )
            }
        }
    }
}
