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

    fun verifyEmail() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val result = api?.verifyEmail(_uiState.value.email)
                if (result != null && result.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Success(),
                        successMessage = result.body()?.message ?: "Email verification sent successfully",
                        currentStep = ForgetPasswordStep.OTP_VERIFICATION
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error(result?.body()?.message ?: "Failed to send verification email")
                    )
                }
            } catch (ex: Exception) {
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error(ex.message ?: "Unknown error occurred")
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
                val result = api?.verifyOtp(otpInt, _uiState.value.email)
                if (result != null && result.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Success(),
                        successMessage = result.body()?.message ?: "OTP verified successfully",
                        currentStep = ForgetPasswordStep.CHANGE_PASSWORD
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error(result?.body()?.message ?: "Invalid OTP")
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

                val request = ChangePasswordRequest(_uiState.value.password, _uiState.value.confirmPassword)
                val result = api?.changePassword(_uiState.value.email, request)
                
                if (result != null && result.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Success(),
                        successMessage = result.body()?.message ?: "Password changed successfully",
                        currentStep = ForgetPasswordStep.SUCCESS
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error(result?.body()?.message ?: "Failed to change password")
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
