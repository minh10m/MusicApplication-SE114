package com.example.musicapplicationse114.ui.screen.forget_password

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Send
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

    // Reset to initial state when entering the screen - use timestamp to ensure fresh start
    LaunchedEffect(key1 = "enter_screen") {
        viewModel.resetToInitialState()
    }

    // Handle back button behavior
    BackHandler {
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

    LaunchedEffect(state.value.status, state.value.currentStep) {
        when (state.value.status) {
            is LoadStatus.Success -> {
                if (state.value.status != LoadStatus.Init()) {
                    Toast.makeText(context, state.value.successMessage, Toast.LENGTH_SHORT).show()

                    // For success case, just reset status, don't auto-navigate
                    // User will navigate manually by clicking "Back to Login" button
                    if (state.value.currentStep != ForgetPasswordStep.SUCCESS) {
                        // Wait a bit to ensure UI has time to update the step, then reset status
                        kotlinx.coroutines.delay(100)
                        viewModel.reset()
                    }
                }
            }
            is LoadStatus.Error -> {
                // Only show errors that are not email or OTP specific through mainViewModel
                if (state.value.emailError.isEmpty() && state.value.otpError.isEmpty()) {
                    mainViewModel.setError((state.value.status as LoadStatus.Error).description)
                }
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
            when (state.value.status) {
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
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                                emailError = state.value.emailError,
                                onEmailChange = viewModel::updateEmail,
                                onVerifyClick = viewModel::verifyEmail
                            )
                        }
                        ForgetPasswordStep.OTP_VERIFICATION -> {
                            OtpVerificationContent(
                                otp = state.value.otp,
                                email = state.value.email,
                                otpError = state.value.otpError,
                                canResendOtp = state.value.canResendOtp,
                                resendCooldown = state.value.resendCooldown,
                                onOtpChange = viewModel::updateOtp,
                                onVerifyClick = viewModel::verifyOtp,
                                onResendClick = viewModel::resendOtp
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
                            SuccessContent(
                                onBackToLoginClick = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.ForgetPassword.route) { inclusive = true }
                                    }
                                }
                            )
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
    emailError: String,
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
            placeholder = { Text("Enter your email address") },
            singleLine = true,
            isError = emailError.isNotEmpty() || (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()),
            supportingText = {
                when {
                    emailError.isNotEmpty() -> {
                        Text(
                            text = emailError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                    email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        Text(
                            text = "Please enter a valid email address",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(25.dp, shape = RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onVerifyClick,
            enabled = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && emailError.isEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && emailError.isEmpty())
                    Color(0xFF3B5998) else Color.Gray,
                disabledContainerColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Text("Send OTP", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(5.dp))
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }
}

@Composable
private fun OtpVerificationContent(
    otp: String,
    email: String,
    otpError: String,
    canResendOtp: Boolean,
    resendCooldown: Int,
    onOtpChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendClick: () -> Unit
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
            onValueChange = { newOtp ->
                // Only allow numbers and max 6 digits
                if (newOtp.all { it.isDigit() } && newOtp.length <= 6) {
                    onOtpChange(newOtp)
                }
            },
            label = { Text("OTP Code") },
            leadingIcon = { Icon(Icons.Filled.VpnKey, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("Enter 6-digit OTP") },
            singleLine = true,
            isError = otpError.isNotEmpty(),
            supportingText = {
                if (otpError.isNotEmpty()) {
                    Text(
                        text = otpError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(25.dp, shape = RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onVerifyClick,
            enabled = otp.length == 6 && otpError.isEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (otp.length == 6 && otpError.isEmpty()) Color(0xFF3B5998) else Color.Gray,
                disabledContainerColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Text("Verify OTP", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(5.dp))
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Resend OTP section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Didn't receive OTP?",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))

            if (canResendOtp) {
                TextButton(
                    onClick = onResendClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3B5998))
                ) {
                    Text(
                        text = "Resend OTP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = if (resendCooldown > 0) "Resend in ${resendCooldown}s" else "Resend OTP",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
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
            placeholder = { Text("Enter a strong password") },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(25.dp, shape = RoundedCornerShape(20.dp))
        )

        // Password requirements
        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Password Requirements:",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                val requirements = listOf(
                    "At least 8 characters" to (password.length >= 8),
                    "One uppercase letter" to password.any { it.isUpperCase() },
                    "One lowercase letter" to password.any { it.isLowerCase() },
                    "One number" to password.any { it.isDigit() },
                    "One special character" to password.any { it in "!@#$%^&*()_+-=[]{}|;:,.<>?" }
                )

                requirements.forEach { (requirement, isMet) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isMet) Color.Green else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = requirement,
                            fontSize = 12.sp,
                            color = if (isMet) Color.Green else Color.Gray
                        )
                    }
                }
            }
        }

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
            placeholder = { Text("Confirm your password") },
            singleLine = true,
            isError = confirmPassword.isNotEmpty() && password != confirmPassword,
            supportingText = {
                if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                    Text(
                        text = "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                } else if (confirmPassword.isNotEmpty() && password == confirmPassword) {
                    Text(
                        text = "Passwords match",
                        color = Color.Green,
                        fontSize = 12.sp
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(25.dp, shape = RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(28.dp))

        val isPasswordValid = password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { it in "!@#$%^&*()_+-=[]{}|;:,.<>?" }
        val isFormValid = isPasswordValid && password == confirmPassword && confirmPassword.isNotEmpty()

        Button(
            onClick = onChangePasswordClick,
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) Color(0xFF3B5998) else Color.Gray,
                disabledContainerColor = Color.Gray
            ),
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
private fun SuccessContent(
    onBackToLoginClick: () -> Unit
) {
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

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onBackToLoginClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B5998)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Text("Back to Login", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(5.dp))
                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
            }
        }
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