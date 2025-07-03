package com.example.musicapplicationse114.ui.screen.forget_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.ChangePasswordRequest
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
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
    val canResendOtp: Boolean = false,
    val resendCooldown: Int = 0, // seconds remaining
    val emailError: String = "", // Email specific error
    val otpError: String = "", // OTP specific error
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
    
    private var cooldownJob: Job? = null
    
    companion object {
        private const val RESEND_COOLDOWN_SECONDS = 60 // 1 minute cooldown
        private const val API_TIMEOUT_MS = 15000L // 15 seconds timeout
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = "" // Clear error when user types
        )
    }

    fun updateOtp(otp: String) {
        _uiState.value = _uiState.value.copy(
            otp = otp,
            otpError = "" // Clear error when user types
        )
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
        _uiState.value = _uiState.value.copy(
            status = LoadStatus.Init(),
            emailError = "",
            otpError = ""
        )
    }
    
    fun resetToInitialState() {
        cooldownJob?.cancel()
        _uiState.value = ForgetPasswordUiState()
    }
    
    fun clearOtpOnError() {
        _uiState.value = _uiState.value.copy(otp = "")
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
            status = LoadStatus.Init(),
            canResendOtp = false,
            resendCooldown = 0
        )
        cooldownJob?.cancel()
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
                // Validate email format first
                if (_uiState.value.email.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Init(), // Reset loading state
                        emailError = "Please enter your email address"
                    )
                    return@launch
                }
                
                if (!isValidEmail(_uiState.value.email)) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Init(), // Reset loading state
                        emailError = "Please enter a valid email address (example@domain.com)"
                    )
                    return@launch
                }
                
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                
                android.util.Log.d("ForgetPassword", "Verifying email: ${_uiState.value.email}")
                
                // Real API call with timeout
                val result = withTimeout(API_TIMEOUT_MS) {
                    api?.verifyEmail(_uiState.value.email)
                }
                
                android.util.Log.d("ForgetPassword", "Email API Response Code: ${result?.code()}")
                android.util.Log.d("ForgetPassword", "Email API Response Success: ${result?.isSuccessful}")
                android.util.Log.d("ForgetPassword", "Email API Response Body: ${result?.body()}")
                android.util.Log.d("ForgetPassword", "Email API Response Error: ${result?.errorBody()?.string()}")
                
                if (result != null && result.isSuccessful) {
                    val responseText = result.body()?.string() ?: "OTP sent to your email successfully"
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Success(),
                        successMessage = "Mã xác thực đã được gửi đến email của bạn",
                        currentStep = ForgetPasswordStep.OTP_VERIFICATION,
                        canResendOtp = false,
                        resendCooldown = RESEND_COOLDOWN_SECONDS
                    )
                    startResendCooldown()
                } else {
                    // Handle specific error messages from backend
                    val errorBody = result?.errorBody()?.string()
                    android.util.Log.d("ForgetPassword", "Error body: $errorBody")
                    
                    // Try to parse JSON error response for detailed message
                    val detailedErrorMessage = try {
                        if (errorBody != null) {
                            val jsonObject = org.json.JSONObject(errorBody)
                            when {
                                jsonObject.has("details") -> jsonObject.getString("details")
                                jsonObject.has("message") -> jsonObject.getString("message")
                                jsonObject.has("error") -> jsonObject.getString("error")
                                else -> null
                            }
                        } else null
                    } catch (e: Exception) {
                        android.util.Log.d("ForgetPassword", "Failed to parse JSON error: ${e.message}")
                        null
                    }
                    
                    val errorMessage = when (result?.code()) {
                        401 -> {
                            // Handle UNAUTHORIZED - Email does not exist
                            when {
                                detailedErrorMessage?.contains("Email does not exist", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                detailedErrorMessage?.contains("Authentication Failed", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                errorBody?.contains("Email does not exist", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                errorBody?.contains("Authentication Failed", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                else -> "Email does not exist"
                            }
                        }
                        404 -> "Email does not exist"
                        400 -> "Invalid email format"
                        429 -> "Too many requests. Please try again later" // Rate limiting
                        500 -> "Server error. Please try again later"
                        503 -> "Email service temporarily unavailable"
                        else -> {
                            // Check for specific error messages in response body or detailed JSON message
                            when {
                                detailedErrorMessage?.contains("Email does not exist", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                detailedErrorMessage?.contains("does not exist", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                detailedErrorMessage?.contains("not found", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                detailedErrorMessage?.contains("Authentication Failed", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                detailedErrorMessage?.contains("invalid", ignoreCase = true) == true -> 
                                    "Invalid email format"
                                errorBody?.contains("Email does not exist", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                errorBody?.contains("does not exist", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                errorBody?.contains("not found", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                errorBody?.contains("Authentication Failed", ignoreCase = true) == true -> 
                                    "Email does not exist"
                                errorBody?.contains("invalid", ignoreCase = true) == true -> 
                                    "Invalid email format"
                                else -> detailedErrorMessage ?: errorBody ?: "Failed to send verification email. Please try again"
                            }
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Init(), // Reset loading state
                        emailError = errorMessage
                    )
                }
            } catch (ex: Exception) {
                // Log the exception for debugging
                android.util.Log.e("ForgetPassword", "Email Exception: ${ex.message}", ex)
                val errorMessage = when {
                    ex is TimeoutCancellationException -> 
                        "Request timeout. Please check your connection and try again"
                    ex.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Connection timeout. Please check your internet connection"
                    ex.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your internet connection"
                    ex.message?.contains("ConnectException", ignoreCase = true) == true -> 
                        "Cannot connect to server. Please try again later"
                    else -> "Network error occurred. Please try again"
                }
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Init(), // Reset loading state
                    emailError = errorMessage
                )
            }
        }
    }

    fun verifyOtp() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                
                // Validate OTP format
                if (_uiState.value.otp.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Init(), // Reset loading state
                        otpError = "Please enter OTP code"
                    )
                    return@launch
                }
                
                if (_uiState.value.otp.length != 6) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Init(), // Reset loading state
                        otpError = "OTP must be 6 digits"
                    )
                    return@launch
                }
                
                val otpInt = _uiState.value.otp.toIntOrNull()
                if (otpInt == null) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Init(), // Reset loading state
                        otpError = "OTP must contain only numbers"
                    )
                    return@launch
                }
                
                android.util.Log.d("ForgetPassword", "Verifying OTP: ${_uiState.value.otp} for email: ${_uiState.value.email}")
                
                // Real API call with timeout
                val result = withTimeout(API_TIMEOUT_MS) {
                    api?.verifyOtp(otpInt, _uiState.value.email)
                }
                
                android.util.Log.d("ForgetPassword", "OTP API Response Code: ${result?.code()}")
                android.util.Log.d("ForgetPassword", "OTP API Response Success: ${result?.isSuccessful}")
                android.util.Log.d("ForgetPassword", "OTP API Response Body: ${result?.body()}")
                android.util.Log.d("ForgetPassword", "OTP API Response Error: ${result?.errorBody()?.string()}")
                
                if (result != null && result.isSuccessful) {
                    val responseText = result.body()?.string() ?: "OTP verified successfully"
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Success(),
                        successMessage = "Xác thực OTP thành công",
                        currentStep = ForgetPasswordStep.CHANGE_PASSWORD
                    )
                } else {
                    // Handle specific OTP error messages
                    val errorBody = result?.errorBody()?.string()
                    android.util.Log.d("ForgetPassword", "OTP Error body: $errorBody")
                    
                    // Try to parse JSON error response for detailed message
                    val detailedErrorMessage = try {
                        if (errorBody != null) {
                            val jsonObject = org.json.JSONObject(errorBody)
                            when {
                                jsonObject.has("details") -> jsonObject.getString("details")
                                jsonObject.has("message") -> jsonObject.getString("message")
                                jsonObject.has("error") -> jsonObject.getString("error")
                                else -> null
                            }
                        } else null
                    } catch (e: Exception) {
                        android.util.Log.d("ForgetPassword", "Failed to parse JSON error: ${e.message}")
                        null
                    }
                    
                    val errorMessage = when (result?.code()) {
                        401 -> {
                            // Handle UNAUTHORIZED - Invalid OTP or expired
                            when {
                                detailedErrorMessage?.contains("Invalid OTP", ignoreCase = true) == true -> "Invalid OTP code"
                                detailedErrorMessage?.contains("OTP expired", ignoreCase = true) == true -> "OTP has expired. Please request a new one"
                                detailedErrorMessage?.contains("expired", ignoreCase = true) == true -> "OTP has expired. Please request a new one"
                                detailedErrorMessage?.contains("Authentication Failed", ignoreCase = true) == true -> "Invalid OTP code"
                                errorBody?.contains("Invalid OTP", ignoreCase = true) == true -> "Invalid OTP code"
                                errorBody?.contains("OTP expired", ignoreCase = true) == true -> "OTP has expired. Please request a new one"
                                errorBody?.contains("expired", ignoreCase = true) == true -> "OTP has expired. Please request a new one"
                                errorBody?.contains("Authentication Failed", ignoreCase = true) == true -> "Invalid OTP code"
                                else -> "Invalid OTP code"
                            }
                        }
                        404 -> "Invalid OTP or email not found"
                        417 -> "OTP has expired. Please request a new one" // HTTP 417 = EXPECTATION_FAILED
                        400 -> "Invalid OTP format"
                        422 -> "Invalid OTP code" // HTTP 422 = UNPROCESSABLE_ENTITY
                        else -> {
                            when {
                                detailedErrorMessage?.contains("Invalid OTP", ignoreCase = true) == true -> "Invalid OTP code"
                                detailedErrorMessage?.contains("OTP expired", ignoreCase = true) == true -> "OTP has expired. Please request a new one"
                                detailedErrorMessage?.contains("expired", ignoreCase = true) == true -> "OTP has expired. Please request a new one"
                                detailedErrorMessage?.contains("not found", ignoreCase = true) == true -> "Invalid OTP or email not found"
                                detailedErrorMessage?.contains("Authentication Failed", ignoreCase = true) == true -> "Invalid OTP code"
                                errorBody?.contains("Invalid OTP", ignoreCase = true) == true -> "Invalid OTP code"
                                errorBody?.contains("OTP expired", ignoreCase = true) == true -> "OTP has expired. Please request a new one"
                                errorBody?.contains("expired", ignoreCase = true) == true -> "OTP has expired. Please request a new one"
                                errorBody?.contains("not found", ignoreCase = true) == true -> "Invalid OTP or email not found"
                                errorBody?.contains("Authentication Failed", ignoreCase = true) == true -> "Invalid OTP code"
                                else -> detailedErrorMessage ?: "Invalid OTP code. Please try again"
                            }
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Init(), // Reset loading state
                        otpError = errorMessage
                    )
                }
            } catch (ex: Exception) {
                android.util.Log.e("ForgetPassword", "OTP Exception: ${ex.message}", ex)
                val errorMessage = when {
                    ex is TimeoutCancellationException -> 
                        "Request timeout. Please try again"
                    ex.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Connection timeout. Please try again"
                    ex.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your connection"
                    else -> "Network error occurred. Please try again"
                }
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Init(), // Reset loading state
                    otpError = errorMessage
                )
            }
        }
    }

    fun changePassword() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                
                // Validate password strength
                val passwordValidation = validatePassword(_uiState.value.password)
                if (!passwordValidation.isValid) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(passwordValidation.errorMessage))
                    return@launch
                }
                
                if (_uiState.value.password != _uiState.value.confirmPassword) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Passwords do not match"))
                    return@launch
                }

                // Real API call with timeout
                val request = ChangePasswordRequest(_uiState.value.password, _uiState.value.confirmPassword)
                val result = withTimeout(API_TIMEOUT_MS) {
                    api?.changePassword(_uiState.value.email, request)
                }
                
                if (result != null && result.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Success(),
                        successMessage = "Thay đổi mật khẩu thành công",
                        currentStep = ForgetPasswordStep.SUCCESS
                    )
                } else {
                    val errorBody = result?.errorBody()?.string()
                    android.util.Log.d("ForgetPassword", "Change Password Error body: $errorBody")
                    
                    // Try to parse JSON error response for detailed message
                    val detailedErrorMessage = try {
                        if (errorBody != null) {
                            val jsonObject = org.json.JSONObject(errorBody)
                            when {
                                jsonObject.has("details") -> jsonObject.getString("details")
                                jsonObject.has("message") -> jsonObject.getString("message")
                                jsonObject.has("error") -> jsonObject.getString("error")
                                else -> null
                            }
                        } else null
                    } catch (e: Exception) {
                        android.util.Log.d("ForgetPassword", "Failed to parse JSON error: ${e.message}")
                        null
                    }
                    
                    val errorMessage = when (result?.code()) {
                        401 -> {
                            when {
                                detailedErrorMessage?.contains("Authentication Failed", ignoreCase = true) == true -> 
                                    "Session expired. Please restart the forgot password process"
                                errorBody?.contains("Authentication Failed", ignoreCase = true) == true -> 
                                    "Session expired. Please restart the forgot password process"
                                else -> "Authentication failed. Please restart the process"
                            }
                        }
                        404 -> "Email not found"
                        400 -> "Invalid password format"
                        417 -> {
                            // EXPECTATION_FAILED - Password mismatch from backend
                            when {
                                detailedErrorMessage?.contains("Please enter the password again", ignoreCase = true) == true -> 
                                    "Passwords do not match"
                                errorBody?.contains("Please enter the password again", ignoreCase = true) == true -> 
                                    "Passwords do not match"
                                else -> "Invalid password format"
                            }
                        }
                        500 -> "Server error. Please try again later"
                        else -> {
                            detailedErrorMessage ?: errorBody ?: "Failed to change password"
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error(errorMessage)
                    )
                }
            } catch (ex: Exception) {
                android.util.Log.e("ForgetPassword", "Change Password Exception: ${ex.message}", ex)
                val errorMessage = when {
                    ex is TimeoutCancellationException -> 
                        "Request timeout. Please try again"
                    ex.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Connection timeout. Please try again"
                    ex.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your connection"
                    else -> "Network error occurred. Please try again"
                }
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error(errorMessage)
                )
            }
        }
    }

    // Email validation function
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Password validation function
    private fun validatePassword(password: String): PasswordValidationResult {
        if (password.isEmpty()) {
            return PasswordValidationResult(false, "Password cannot be empty")
        }
        
        if (password.length < 8) {
            return PasswordValidationResult(false, "Password must be at least 8 characters long")
        }
        
        if (!password.any { it.isUpperCase() }) {
            return PasswordValidationResult(false, "Password must contain at least one uppercase letter")
        }
        
        if (!password.any { it.isLowerCase() }) {
            return PasswordValidationResult(false, "Password must contain at least one lowercase letter")
        }
        
        if (!password.any { it.isDigit() }) {
            return PasswordValidationResult(false, "Password must contain at least one number")
        }
        
        val specialCharacters = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        if (!password.any { it in specialCharacters }) {
            return PasswordValidationResult(false, "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)")
        }
        
        return PasswordValidationResult(true, "")
    }
    
    // Resend OTP function
    fun resendOtp() {
        if (!_uiState.value.canResendOtp) return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                
                android.util.Log.d("ForgetPassword", "Resending OTP to: ${_uiState.value.email}")
                
                // Real API call - same as verifyEmail with timeout
                val result = withTimeout(API_TIMEOUT_MS) {
                    api?.verifyEmail(_uiState.value.email)
                }
                
                android.util.Log.d("ForgetPassword", "Resend OTP API Response Code: ${result?.code()}")
                android.util.Log.d("ForgetPassword", "Resend OTP API Response Success: ${result?.isSuccessful}")
                
                if (result != null && result.isSuccessful) {
                    val responseText = result.body()?.string() ?: "New OTP sent to your email"
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Success(),
                        successMessage = "Mã xác thực đã được gửi tới email của bạn",
                        canResendOtp = false,
                        resendCooldown = RESEND_COOLDOWN_SECONDS,
                        otp = "" // Clear previous OTP
                    )
                    startResendCooldown()
                } else {
                    val errorBody = result?.errorBody()?.string()
                    android.util.Log.d("ForgetPassword", "Resend OTP Error body: $errorBody")
                    
                    // Try to parse JSON error response for detailed message
                    val detailedErrorMessage = try {
                        if (errorBody != null) {
                            val jsonObject = org.json.JSONObject(errorBody)
                            when {
                                jsonObject.has("details") -> jsonObject.getString("details")
                                jsonObject.has("message") -> jsonObject.getString("message")
                                jsonObject.has("error") -> jsonObject.getString("error")
                                else -> null
                            }
                        } else null
                    } catch (e: Exception) {
                        android.util.Log.d("ForgetPassword", "Failed to parse JSON error: ${e.message}")
                        null
                    }
                    
                    val errorMessage = when (result?.code()) {
                        401 -> {
                            when {
                                detailedErrorMessage?.contains("Email does not exist", ignoreCase = true) == true -> 
                                    "This email address is not registered with us"
                                detailedErrorMessage?.contains("Authentication Failed", ignoreCase = true) == true -> 
                                    "This email address is not registered with us"
                                errorBody?.contains("Email does not exist", ignoreCase = true) == true -> 
                                    "This email address is not registered with us"
                                errorBody?.contains("Authentication Failed", ignoreCase = true) == true -> 
                                    "This email address is not registered with us"
                                else -> "This email address is not registered with us"
                            }
                        }
                        404 -> "This email address is not registered with us"
                        429 -> "Too many requests. Please wait before requesting again"
                        500 -> "Server error. Please try again later"
                        else -> detailedErrorMessage ?: "Failed to resend OTP. Please try again"
                    }
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error(errorMessage)
                    )
                }
            } catch (ex: Exception) {
                android.util.Log.e("ForgetPassword", "Resend OTP Exception: ${ex.message}", ex)
                val errorMessage = when {
                    ex is TimeoutCancellationException -> 
                        "Request timeout. Please try again"
                    ex.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Connection timeout. Please try again"
                    ex.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your connection"
                    else -> "Network error occurred. Please try again"
                }
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error(errorMessage)
                )
            }
        }
    }
    
    // Start cooldown timer for resend button
    private fun startResendCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            var remaining = RESEND_COOLDOWN_SECONDS
            while (remaining > 0) {
                _uiState.value = _uiState.value.copy(
                    resendCooldown = remaining,
                    canResendOtp = false
                )
                delay(1000) // Wait 1 second
                remaining--
            }
            _uiState.value = _uiState.value.copy(
                resendCooldown = 0,
                canResendOtp = true
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel()
    }
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)
