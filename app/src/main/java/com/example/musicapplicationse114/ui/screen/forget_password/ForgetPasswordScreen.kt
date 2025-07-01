package com.example.musicapplicationse114.ui.screen.forget_password

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.R
import com.example.musicapplicationse114.Screen
import com.example.musicapplicationse114.common.enum.LoadStatus

@Composable
fun ForgetPasswordScreen(
    navController: NavHostController,
    viewModel: ForgetPasswordViewModel,
    mainViewModel: MainViewModel
) {
    val state = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.value.status) {
        when (val status = state.value.status) {
            is LoadStatus.Success -> {
                if (status != LoadStatus.Init()) {
                    Toast.makeText(context, state.value.successMessage, Toast.LENGTH_SHORT).show()
                    
                    // Only navigate to login when password change is successful
                    if (state.value.currentStep == ForgetPasswordStep.SUCCESS) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ForgetPassword.route) { inclusive = true }
                        }
                    } else {
                        // Wait a bit to ensure UI has time to update the step, then reset status
                        kotlinx.coroutines.delay(100)
                        viewModel.reset()
                    }
                }
            }
            is LoadStatus.Error -> {
                mainViewModel.setError(status.description)
                viewModel.reset()
            }
            else -> {
                // do nothing
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            when (val status = state.value.status) {
                is LoadStatus.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    }
                }
                else -> {
                    Spacer(modifier = Modifier.height(60.dp))

                    // Back button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(
                            onClick = { 
                                when (state.value.currentStep) {
                                    ForgetPasswordStep.EMAIL_VERIFICATION -> {
                                        navController.navigateUp()
                                    }
                                    ForgetPasswordStep.OTP_VERIFICATION -> {
                                        viewModel.goBackToEmailStep()
                                    }
                                    ForgetPasswordStep.CHANGE_PASSWORD -> {
                                        viewModel.goBackToOtpStep()
                                    }
                                    ForgetPasswordStep.SUCCESS -> {
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.ForgetPassword.route) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.musico_with_icons),
                        contentDescription = "Musico logo with icons",
                        modifier = Modifier.size(width = 214.dp, height = 74.dp)
                    )

                    Spacer(modifier = Modifier.height(60.dp))

                    when (state.value.currentStep) {
                        ForgetPasswordStep.EMAIL_VERIFICATION -> {
                            EmailVerificationContent(
                                email = state.value.email,
                                onEmailChange = viewModel::updateEmail,
                                onVerifyClick = viewModel::verifyEmail
                            )
                        }
                        ForgetPasswordStep.OTP_VERIFICATION -> {
                            OtpVerificationContent(
                                otp = state.value.otp,
                                email = state.value.email,
                                onOtpChange = viewModel::updateOtp,
                                onVerifyClick = viewModel::verifyOtp
                            )
                        }
                        ForgetPasswordStep.CHANGE_PASSWORD -> {
                            ChangePasswordContent(
                                password = state.value.password,
                                confirmPassword = state.value.confirmPassword,
                                isShowPassword = state.value.isShowPassword,
                                isShowConfirmPassword = state.value.isShowConfirmPassword,
                                onPasswordChange = viewModel::updatePassword,
                                onConfirmPasswordChange = viewModel::updateConfirmPassword,
                                onToggleShowPassword = viewModel::toggleShowPassword,
                                onToggleShowConfirmPassword = viewModel::toggleShowConfirmPassword,
                                onChangePasswordClick = viewModel::changePassword
                            )
                        }
                        ForgetPasswordStep.SUCCESS -> {
                            SuccessContent()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailVerificationContent(
    email: String,
    onEmailChange: (String) -> Unit,
    onVerifyClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(
                text = "Forget Password",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Lock",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter your email to receive OTP",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        TextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(25.dp, shape = RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onVerifyClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B5998)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Text("Send OTP", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(5.dp))
                Icon(Icons.Filled.Send, contentDescription = null)
            }
        }
    }
}

@Composable
private fun OtpVerificationContent(
    otp: String,
    email: String,
    onOtpChange: (String) -> Unit,
    onVerifyClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(
                text = "Verify OTP",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = "Security",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter the OTP sent to:",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = email,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(40.dp))

        TextField(
            value = otp,
            onValueChange = onOtpChange,
            label = { Text("OTP Code") },
            leadingIcon = { Icon(Icons.Filled.VpnKey, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(25.dp, shape = RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onVerifyClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B5998)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Text("Verify OTP", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(5.dp))
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
            }
        }
    }
}

@Composable
private fun ChangePasswordContent(
    password: String,
    confirmPassword: String,
    isShowPassword: Boolean,
    isShowConfirmPassword: Boolean,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleShowPassword: () -> Unit,
    onToggleShowConfirmPassword: () -> Unit,
    onChangePasswordClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(
                text = "New Password",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.Filled.Key,
                contentDescription = "Key",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create a new password",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        TextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("New Password") },
            visualTransformation = if (isShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onToggleShowPassword) {
                    Icon(
                        imageVector = if (isShowPassword) Icons.Default.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(25.dp, shape = RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm Password") },
            visualTransformation = if (isShowConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onToggleShowConfirmPassword) {
                    Icon(
                        imageVector = if (isShowConfirmPassword) Icons.Default.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(25.dp, shape = RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onChangePasswordClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B5998)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Text("Change Password", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(5.dp))
                Icon(Icons.Filled.Save, contentDescription = null)
            }
        }
    }
}

@Composable
private fun SuccessContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Success",
            modifier = Modifier.size(80.dp),
            tint = Color.Green
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Password Changed!",
            fontSize = 28.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your password has been changed successfully.",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You can now login with your new password.",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgetPasswordScreenPreview() {
    val navController = rememberNavController()
    ForgetPasswordScreen(
        navController = navController,
        viewModel = ForgetPasswordViewModel(null),
        mainViewModel = MainViewModel()
    )
}
