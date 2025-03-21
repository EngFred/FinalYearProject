package com.engineerfred.finalyearproject.ui.components


import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engineerfred.finalyearproject.R
import com.engineerfred.finalyearproject.domain.model.Detector
import com.engineerfred.finalyearproject.ui.theme.LightTeal
import com.engineerfred.finalyearproject.ui.theme.box

@Composable
fun DetectionModeSelector(
    onDetect: (Detector) -> Unit,
    detectionModel: Detector?,
    enabled: Boolean = true,
    isDetecting: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var selectedModel by remember {
        mutableStateOf(detectionModel?.name)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if ( selectedModel == null ) {
                    if ( enabled ) {
                        expanded = !expanded
                    } else {
                        Toast.makeText(context, "Please wait!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    selectedModel?.let {
                        expanded = false
                        onDetect(Detector.valueOf(it))
                    }
                }
            },
            enabled = isDetecting.not(),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if( isDetecting.not() ) {
                val btnText = if(selectedModel == null ) {
                    "Choose detection model"
                } else {
                    when(selectedModel) {
                        Detector.DETECTOR_1.name -> "Detect with Model 1"
                        Detector.DETECTOR_2.name -> "Detect with Model 2"
                        else -> "Detect with Model 3"
                    }
                }
                Text(
                    text = btnText,
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 18.sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        ),
                    )
                )
                IconButton(
                    onClick = {
                        if ( enabled ) {
                            expanded = !expanded
                        } else {
                            Toast.makeText(context, "Please wait!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            }
        }

        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier.padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LightTeal),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Detector.entries.forEachIndexed { index, detector ->
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    selectedModel = detector.name
                                    onDetect(detector)
                                    expanded = false
                                }
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                        ) {
                            Text(
                                text = "Use Model ${index + 1}",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp),
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
                            val icon = if( selectedModel ==  detector.name ) R.drawable.ic_circle_check else R.drawable.ic_circle_unchecked
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = null,
                                tint = box
                            )
                        }
                        if (index < Detector.entries.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

