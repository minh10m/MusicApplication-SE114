package com.example.musicapplicationse114.ui.screen.signUp

import android.media.Image
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.R
import com.example.musicapplicationse114.Screen
import com.example.musicapplicationse114.common.enum.LoadStatus

@Composable
fun SignUpScreen(navController: NavController, viewModel: SignUpViewModel, mainViewModel: MainViewModel) {
    val state = viewModel.uiState.collectAsState()
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Top để mình kiểm soát thứ tự dễ hơn
        ) {
            if(state.value.status is LoadStatus.Loading){
                Box(modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center)
                {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                }
            }
            else if(state.value.status is LoadStatus.Success){
                LaunchedEffect(Unit) {
                    Log.e("SIGNUP", "SUCCESSSSSSS")
                    Toast.makeText(context, state.value.successMessage, Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.Login.route)
                }
            }
            else {
                if(state.value.status is LoadStatus.Error){
                    viewModel.reset()
                    Toast.makeText(context, state.value.errorMessage, Toast.LENGTH_SHORT).show()
                }
                Spacer(modifier = Modifier.height(80.dp)) // Cách lề trên

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.musico_with_icons),
                    contentDescription = "Musico logo with icons",
                    modifier = Modifier
                        .size(width = 214.dp, height = 74.dp)
                )

                Spacer(modifier = Modifier.height(70.dp)) // Khoảng cách dưới logo

                // Login Text
                Row()
                {
                    Text(
                        text = "Create Account",
                        fontSize = 33.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign up Text
                Text(
                    text = "Please sign up to continue.",
                    fontSize = 20.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(60.dp))

                //username
                TextField(
                    value = state.value.username, onValueChange = {
                        viewModel.updateUsername(it)},
                    label = {Text("Username")},
                    leadingIcon = {Icon(Icons.Filled.Face, contentDescription = null)},
                    modifier = Modifier
                        .shadow(25.dp, shape = RoundedCornerShape(20.dp), )
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = state.value.email, onValueChange = { viewModel.updateEmail(it)
                    },
                    label = {Text("Email")},
                    leadingIcon = {Icon(Icons.Filled.Person, contentDescription = null)},
                    modifier = Modifier
                        .shadow(25.dp, shape = RoundedCornerShape(20.dp), )
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = state.value.password, onValueChange = {viewModel.updatePassword(it)},
                    label = {Text("Password")},
                    visualTransformation = if(state.value.isShowPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    leadingIcon = {Icon(Icons.Filled.Lock, contentDescription = null)},
                    trailingIcon = { IconButton(onClick = {viewModel.changIsShowPassword()}){
                        Icon(imageVector = if(state.value.isShowPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null)
                    } },
                    modifier = Modifier.shadow(25.dp, shape = RoundedCornerShape(20.dp))
                )


                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = state.value.confirmPassword, onValueChange = {viewModel.updateConfirmPassword(it)},
                    label = {Text("Confirm Password")},
                    visualTransformation = if(state.value.isShowConfirmPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    leadingIcon = {Icon(Icons.Filled.Lock, contentDescription = null)},
                    trailingIcon = { IconButton(onClick = {viewModel.changIsShowConfirmPassword()}){
                        Icon(imageVector = if(state.value.isShowConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null)
                    } },
                    modifier = Modifier.shadow(25.dp, shape = RoundedCornerShape(20.dp))
                )

                Spacer(modifier = Modifier.height(28.dp))

                //Sign up button
                Button(onClick = {viewModel.signUp()},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B5998))) {
                    Row {
                        Text(
                            "Sign Up",
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Already have an account?",
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(1.5.dp))
                    TextButton(onClick = {navController.navigate(Screen.Login.route)}) {
                        Text(
                            "Sign In",
                            fontSize = 22.sp,
                            color = Color(0xFF3B5998)
                        )
                    }
                }
            }

        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview()
{
    val navController = rememberNavController()
    SignUpScreen(navController = navController, viewModel = SignUpViewModel(null, null), mainViewModel = MainViewModel())
}


