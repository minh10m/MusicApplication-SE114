package com.example.musicapplicationse114.ui.screen.signUp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.common.enum.Role
import com.example.musicapplicationse114.model.UserSignUpRequest
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.repositories.MainLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val successMessage: String = "",
    val errorMessage: String = "",
    val confirmPassword: String = "",
    val emailError: String = "", // Thêm trường này
    var isShowPassword: Boolean = false,
    var isShowConfirmPassword: Boolean = false,
    val status: LoadStatus = LoadStatus.Init()
)

data class PasswordValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val mainLog: MainLog?,
    private val api: Api?
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username, errorMessage = "")
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = "", errorMessage = "")
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = "")
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, errorMessage = "")
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(
            status = LoadStatus.Init(),
            errorMessage = "",
            emailError = ""
        )
    }

    fun changIsShowPassword() {
        _uiState.value = _uiState.value.copy(isShowPassword = !_uiState.value.isShowPassword)
    }

    fun changIsShowConfirmPassword() {
        _uiState.value = _uiState.value.copy(isShowConfirmPassword = !_uiState.value.isShowConfirmPassword)
    }

    fun updateSuccessMessage(successMessage: String) {
        _uiState.value = _uiState.value.copy(successMessage = successMessage)
    }

    fun updateErrorMessage(errorMessage: String) {
        _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
    }

    private fun validatePassword(password: String): PasswordValidationResult {
        if (password.isEmpty()) {
            return PasswordValidationResult(false, "Mật khẩu không được để trống")
        }
        if (password.length < 8) {
            return PasswordValidationResult(false, "Mật khẩu phải có ít nhất 8 ký tự")
        }
        if (!password.any { it.isUpperCase() }) {
            return PasswordValidationResult(false, "Mật khẩu phải chứa ít nhất một chữ hoa")
        }
        if (!password.any { it.isLowerCase() }) {
            return PasswordValidationResult(false, "Mật khẩu phải chứa ít nhất một chữ thường")
        }
        if (!password.any { it.isDigit() }) {
            return PasswordValidationResult(false, "Mật khẩu phải chứa ít nhất một số")
        }
        val specialCharacters = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        if (!password.any { it in specialCharacters }) {
            return PasswordValidationResult(false, "Mật khẩu phải chứa ít nhất một ký tự đặc biệt (!@#$%^&*()_+-=[]{}|;:,.<>?)")
        }
        return PasswordValidationResult(true, "")
    }

    fun signUp() {
        viewModelScope.launch {
            try {
                // Kiểm tra username
                if (_uiState.value.username.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Init(),
                        errorMessage = "Vui lòng nhập tên người dùng"
                    )
                    return@launch
                }

                // Kiểm tra email
                if (_uiState.value.email.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Init(),
                        emailError = "Vui lòng nhập địa chỉ email"
                    )
                    return@launch
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Init(),
                        emailError = "Vui lòng nhập địa chỉ email hợp lệ"
                    )
                    return@launch
                }

                // Kiểm tra mật khẩu
                val passwordValidation = validatePassword(_uiState.value.password)
                if (!passwordValidation.isValid) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error(passwordValidation.errorMessage),
                        errorMessage = passwordValidation.errorMessage
                    )
                    return@launch
                }

                // Kiểm tra xác nhận mật khẩu
                if (_uiState.value.password != _uiState.value.confirmPassword) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Mật khẩu không khớp"),
                        errorMessage = "Mật khẩu không khớp"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val result = api?.register(
                    UserSignUpRequest(
                        _uiState.value.username,
                        _uiState.value.password,
                        _uiState.value.email,
                        _uiState.value.email,
                        "SSG11gjh",
                        role = Role.USER.name
                    )
                )
                if (result != null && result.isSuccessful) {
                    val accessToken = result.body()?.access_token
                    if (accessToken != null) {
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Success())
                        updateSuccessMessage("Đăng ký thành công")
                        Log.e("SignUpResult", "SUcesssssssssssss")
                    } else {
                        val errorMessage = when (result.code()) {
                            400 -> "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại."
                            409 -> "Email hoặc tên người dùng đã tồn tại."
                            else -> "Đăng ký thất bại: ${result.body()?.message ?: "Lỗi không xác định"}"
                        }
                        _uiState.value = _uiState.value.copy(
                            status = LoadStatus.Error(errorMessage),
                            errorMessage = errorMessage
                        )
                        Log.e("SIGNUP", "FAILEDDDDDDDDDDDDDDD")
                        Log.e("SignUpError", "Response body: ${result.body()?.toString()}")
                        Log.e("SignUpError", "Response code: ${result.code()}")
                        Log.e("SignUpError", "AccessToken: ${result.body()?.access_token}")
                    }
                } else {
                    val errorMessage = when (result?.code()) {
                        400 -> "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại."
                        409 -> "Email hoặc tên người dùng đã tồn tại."
                        else -> "Đăng ký thất bại: ${result?.body()?.message ?: "Lỗi không xác định"}"
                    }
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error(errorMessage),
                        errorMessage = errorMessage
                    )
                    Log.e("SIGNUP", "FAILEDDDDDDDDDDDDDDD")
                }
            } catch (ex: Exception) {
                val errorMessage = when {
                    ex.message?.contains("timeout", ignoreCase = true) == true -> "Hết thời gian kết nối. Vui lòng thử lại."
                    ex.message?.contains("network", ignoreCase = true) == true -> "Lỗi mạng. Vui lòng kiểm tra kết nối."
                    else -> "Lỗi kết nối tới máy chủ"
                }
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error(errorMessage),
                    errorMessage = errorMessage
                )
                mainLog?.e("SignUpViewModel", ex.message.toString())
            }
        }
    }
}