package app.adon.suiengine.renderer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun DialogErrorStack(
    errors: List<String>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cabeçalho do Erro
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ícone ou indicador visual de erro
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.error)
                    )
                    Text(
                        text = "SUIEngine Error Trace",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Bloco do Stack Trace com visual de console
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(0.dp)
                ) {
                    // Linha vertical lateral simulando um bloco de código/stack
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((errors.size * 22).dp) // Altura dinâmica aproximada baseada nas linhas
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            errors.forEachIndexed { index, line ->
                                val isLast = index == errors.lastIndex
                                val indentFactor = if (isLast) 0 else 1
                                Text(
                                    text = if (isLast) "➔ $line" else line,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = if (isLast) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(start = (index * 8 * indentFactor).dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}