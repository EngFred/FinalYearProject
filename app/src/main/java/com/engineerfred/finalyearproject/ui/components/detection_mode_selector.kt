package com.engineerfred.finalyearproject.ui.components


import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engineerfred.finalyearproject.ui.theme.LightTeal

@Composable
fun DetectionModeSelector(
    onLocalDetect: () -> Unit,
    onRemoteDetect: () -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if ( enabled ) {
                    expanded = !expanded
                } else {
                    Toast.makeText(context, "Please wait!", Toast.LENGTH_SHORT).show()
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                "Choose Detection Mode",
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    ),
                )
            )
        }

        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier.padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LightTeal),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column{
                    Text(
                        text = "Detect Locally",
                        modifier = Modifier.
                        clickable {
                                expanded = false
                                onLocalDetect()
                            }.
                        fillMaxWidth().padding(8.dp),
                        style = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            ),
                        )
                    )
                    HorizontalDivider()
                    Text(
                        text = "Detect Remotely (Internet Required)",
                        modifier = Modifier
                            .clickable {
                                expanded = false
                                onRemoteDetect()
                            }.fillMaxWidth()
                            .padding(8.dp),
                        style = TextStyle(
                            color = Color.Cyan,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            ),
                        )
                    )
                }
            }
        }
    }
}
