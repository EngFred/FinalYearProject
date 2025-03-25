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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engineerfred.finalyearproject.R
import com.engineerfred.finalyearproject.domain.model.LiteModel
import com.engineerfred.finalyearproject.ui.theme.LightTeal

@Composable
fun DetectionModeSelector(
    onDetect: (LiteModel) -> Unit,
    detectionModel: LiteModel?,
    enabled: Boolean = true,
    isDetecting: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val buttonText = when(detectionModel) {
        LiteModel.FAST -> "BoneDetect-F"
        LiteModel.BALANCED -> "BoneDetect-B"
        LiteModel.PRECISION -> "BoneDetect-P"
        LiteModel.EXTENDED -> "BoneDetect-X"
        else -> "Detect Fractures"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                expanded = false
                detectionModel?.let {
                    onDetect(it)
                } ?: Toast.makeText(context, "Choose a model first", Toast.LENGTH_SHORT).show()
            },
            enabled = isDetecting.not(),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if( isDetecting.not() ) {
                Text(
                    text = buttonText,
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
                LiteModel.entries.forEachIndexed { index, model ->
                    val modelName = when(model) {
                        LiteModel.FAST -> "Detect with BoneDetect-F (Fast)"
                        LiteModel.BALANCED -> "Detect with BoneDetect-B (Balanced)"
                        LiteModel.PRECISION -> "Detect with BoneDetect-P (Precision)"
                        LiteModel.EXTENDED -> "Detect with BoneDetect-X (Extended)"
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    onDetect(model)
                                    expanded = false
                                }
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                        ) {
                            Text(
                                text = modelName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp),
                                style = TextStyle(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    letterSpacing = 0.5.sp,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    ),
                                )
                            )
                            val icon = if( detectionModel ==  model ) R.drawable.ic_circle_check else R.drawable.ic_circle_unchecked
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        if (index < LiteModel.entries.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

