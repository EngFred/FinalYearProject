package com.engineerfred.finalyearproject.ui.screen.username

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.engineerfred.finalyearproject.ui.theme.LightRed

@Composable
fun UsernameScreen(
    onUsernameSaved: () -> Unit,
    viewModel: UsernameViewModel = hiltViewModel()
) {

    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quickly, We would love to know who is using the app",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = Color.Black,
                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                    blurRadius = 2f
                )
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.username ?: "",
            onValueChange = {
                viewModel.changeUsername(it)
            },
            label = { Text("Enter username") },
            isError = uiState.usernameErr != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorTextColor = LightRed,
                errorCursorColor = LightRed,
                errorPlaceholderColor = LightRed,
                errorIndicatorColor = LightRed,
                errorLabelColor = LightRed,
                errorContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
            )
        )

        Text(
            text = uiState.usernameErr ?: "",
            color = LightRed,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 5.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if( uiState.usernameErr.isNullOrEmpty() && uiState.username.isNullOrEmpty().not() ) {
                    viewModel.saveUsername()
                    onUsernameSaved()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = Color.White,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth(.5f)
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = uiState.usernameErr.isNullOrEmpty() && uiState.username.isNullOrEmpty().not()
        ) {
            Text(
                "Save",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color.White,
                )
            )
        }
    }
}