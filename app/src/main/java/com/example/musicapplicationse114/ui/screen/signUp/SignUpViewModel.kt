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
    var isShowPassword: Boolean = false,
    var isShowConfirmPassword: Boolean = false,
    val status: LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val mainLog: MainLog?,
    private val api: Api?
): ViewModel() {
    val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updateEmail(email: String)
    {
        _uiState.value = _uiState.value.copy(email = email)

    }

    fun updatePassword(password: String)
    {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateConfirmPassword(confirmPassword: String)
    {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    fun reset()
    {
        _uiState.value = _uiState.value.copy(status = LoadStatus.Init())

    }

    fun changIsShowPassword()
    {
        _uiState.value = _uiState.value.copy(isShowPassword = !_uiState.value.isShowPassword)

    }

    fun changIsShowConfirmPassword(){
        _uiState.value = _uiState.value.copy(isShowConfirmPassword = !_uiState.value.isShowConfirmPassword)
    }

    fun updateSuccessMessage(successMessage: String )
    {
        _uiState.value = _uiState.value.copy(successMessage = successMessage)
    }

    fun updateErrorMessage(errorMessage: String)
    {
        _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
    }

    fun signUp(){
        _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
        viewModelScope.launch {
            try {
                val result = api?.register(UserSignUpRequest(_uiState.value.username, _uiState.value.password, _uiState.value.email,_uiState.value.email, "SSG11gjh", role = Role.USER.name))
                if(result != null && result.isSuccessful){
                    val accessToken = result.body()?.access_token
                    if(accessToken != null){
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Success())
                        Log.e("SignUpResult", "SUcesssssssssssss")
                        updateSuccessMessage("Đăng kí thành công")
                        //Save Token sau khi đăng nhập ở đâu đó
                    }else{
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error(result.body()?.message.toString()))
                        updateErrorMessage("Đăng kí thất bại")
                        Log.e("SIGNUP", "FAILEDDDDDDDDDDDDDDD")
                        Log.e("SignUpError", "Response body: ${result.body()?.toString()}")
                        Log.e("SignUpError", "Response code: ${result.code()}")
                        Log.e("SignUpError", "AccessToken: ${result.body()?.access_token}")
                    }

                }
                else
                {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(result?.body()?.message.toString()))
                    updateErrorMessage("Đăng kí thất bại")
                    Log.e("SIGNUP", "FAILEDDDDDDDDDDDDDDD")
                }

            }catch (ex: Exception){
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(ex.message.toString()))
                mainLog?.e("SignUpViewModel", ex.message.toString())
                updateErrorMessage("Lỗi kết nối tới máy chủ")
            }
        }
    }


}