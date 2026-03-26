package es.ehfacturas.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CommandInputBar(
    textoManual: String,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onMicTap: () -> Unit,
    estaEscuchando: Boolean,
    procesando: Boolean,
    modifier: Modifier = Modifier
) {
    val micColor by animateColorAsState(
        targetValue = if (estaEscuchando) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
        label = "micColor"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Campo de texto
            OutlinedTextField(
                value = textoManual,
                onValueChange = onTextoChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (estaEscuchando) "Escuchando..."
                        else "Escribe o habla..."
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                enabled = !procesando && !estaEscuchando,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // Botón enviar (solo visible con texto)
            if (textoManual.isNotBlank()) {
                FilledIconButton(
                    onClick = onEnviar,
                    enabled = !procesando,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }

            // Botón micrófono
            FilledIconButton(
                onClick = onMicTap,
                enabled = !procesando,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = micColor
                )
            ) {
                Icon(
                    imageVector = if (estaEscuchando) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (estaEscuchando) "Parar" else "Hablar",
                    tint = Color.White
                )
            }
        }
    }
}
