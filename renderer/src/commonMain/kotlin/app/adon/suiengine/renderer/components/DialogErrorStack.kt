package app.adon.suiengine.renderer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun DialogErrorStack(stack: List<List<String>>, onDismiss: ()->Unit) {

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {

            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {

                //header
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

                Spacer(Modifier.height(12.dp))

                stack.forEach { ss ->
                    Column(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .fillMaxWidth()
                            .background(Color.Gray.copy(0.1f))
                            .padding(8.dp)
                        ,
                    ) {
                        Single(ss.dropLast(1))
                        Text(
                            text = "➔ ${ss.last()}",
                            modifier = Modifier
                                .padding(4.dp)
                            ,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

            }
        }
    }

}

@Composable
private fun Single(list: List<String>) {
    if (list.isEmpty()) return

    //
    Text(
        text = list.first(),
        modifier = Modifier
            .padding(4.dp)
        ,
        fontFamily = FontFamily.Monospace,
    )
    Column(
        modifier = Modifier
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                drawLine(
                    color = Color.Red.copy(alpha = 0.1f),
                    start = Offset(x = strokeWidth / 2, y = 0f),
                    end = Offset(x = strokeWidth / 2, y = size.height),
                    strokeWidth = strokeWidth
                )
            }
            .padding(start = 8.dp)
        ,
    ) {
        Single(list.drop(1))
    }
}