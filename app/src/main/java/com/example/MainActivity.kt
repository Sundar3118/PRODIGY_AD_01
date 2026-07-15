package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFDFCF5) // Soft warm natural cream backdrop
                ) {
                    val calculatorViewModel: CalculatorViewModel = viewModel()
                    CalculatorScreen(viewModel = calculatorViewModel)
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val currentInput by viewModel.currentInput.collectAsState()
    val expressionDisplay by viewModel.expressionDisplay.collectAsState()
    val liveResult by viewModel.liveResult.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFDFCF5))
            .statusBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- DISPLAY SECTION (Top Panel) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            val expressionScrollState = rememberScrollState()
            val inputScrollState = rememberScrollState()

            // 1. Full Formula Display Line
            Text(
                text = expressionDisplay,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF75786A) // Muted sage/olive
                ),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(expressionScrollState)
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Active Operand / Result Display Line
            val displayText = if (currentInput.isEmpty() && expressionDisplay.isEmpty()) "0" else currentInput
            val displayColor = if (displayText == "Error") Color(0xFFBA1A1A) else Color(0xFF1A1C18)

            Text(
                text = displayText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (displayText.length > 8) 42.sp else 64.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    color = displayColor,
                    letterSpacing = (-1.5).sp
                ),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(inputScrollState)
                    .testTag("display_result"),
                textAlign = TextAlign.End
            )

            // 3. Real-Time Live Preview Result
            if (liveResult.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "= $liveResult",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 24.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        color = Color(0x991A1C18)
                    ),
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }

            // Decorative modern organic divider accent
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 4.dp)
                    .background(Color(0x33386663), shape = CircleShape)
            )
        }

        // --- KEYPAD SECTION (Bottom Panel) ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            color = Color(0xFFF1F1E6),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1: AC, ±, %, ÷
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculatorKey(
                        text = "AC",
                        textColor = Color(0xFF386663),
                        backgroundColor = Color(0xFFE1E4D5),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_clear"),
                        onClick = { viewModel.onClear() }
                    )
                    CalculatorKey(
                        text = "±",
                        textColor = Color(0xFF386663),
                        backgroundColor = Color(0xFFE1E4D5),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_toggle_sign"),
                        onClick = { viewModel.onToggleSign() }
                    )
                    CalculatorKey(
                        text = "%",
                        textColor = Color(0xFF386663),
                        backgroundColor = Color(0xFFE1E4D5),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_percentage"),
                        onClick = { viewModel.onPercentage() }
                    )
                    CalculatorKey(
                        text = "÷",
                        textColor = Color(0xFF00210E),
                        backgroundColor = Color(0xFFD3E8D3),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_operator_divide"),
                        onClick = { viewModel.onOperator("÷") }
                    )
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculatorKey(
                        text = "7",
                        textColor = Color(0xFF1A1C18),
                        backgroundColor = Color(0xFFFCF9ED),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_7"),
                        onClick = { viewModel.onDigit("7") }
                    )
                    CalculatorKey(
                        text = "8",
                        textColor = Color(0xFF1A1C18),
                        backgroundColor = Color(0xFFFCF9ED),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_8"),
                        onClick = { viewModel.onDigit("8") }
                    )
                    CalculatorKey(
                        text = "9",
                        textColor = Color(0xFF1A1C18),
                        backgroundColor = Color(0xFFFCF9ED),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_9"),
                        onClick = { viewModel.onDigit("9") }
                    )
                    CalculatorKey(
                        text = "×",
                        textColor = Color(0xFF00210E),
                        backgroundColor = Color(0xFFD3E8D3),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_operator_multiply"),
                        onClick = { viewModel.onOperator("×") }
                    )
                }

                // Row 3: 4, 5, 6, -
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculatorKey(
                        text = "4",
                        textColor = Color(0xFF1A1C18),
                        backgroundColor = Color(0xFFFCF9ED),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_4"),
                        onClick = { viewModel.onDigit("4") }
                    )
                    CalculatorKey(
                        text = "5",
                        textColor = Color(0xFF1A1C18),
                        backgroundColor = Color(0xFFFCF9ED),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_5"),
                        onClick = { viewModel.onDigit("5") }
                    )
                    CalculatorKey(
                        text = "6",
                        textColor = Color(0xFF1A1C18),
                        backgroundColor = Color(0xFFFCF9ED),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_6"),
                        onClick = { viewModel.onDigit("6") }
                    )
                    CalculatorKey(
                        text = "−",
                        textColor = Color(0xFF00210E),
                        backgroundColor = Color(0xFFD3E8D3),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_operator_subtract"),
                        onClick = { viewModel.onOperator("-") }
                    )
                }

                // Row 4: 1, 2, 3, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculatorKey(
                        text = "1",
                        textColor = Color(0xFF1A1C18),
                        backgroundColor = Color(0xFFFCF9ED),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_1"),
                        onClick = { viewModel.onDigit("1") }
                    )
                    CalculatorKey(
                        text = "2",
                        textColor = Color(0xFF1A1C18),
                        backgroundColor = Color(0xFFFCF9ED),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_2"),
                        onClick = { viewModel.onDigit("2") }
                    )
                    CalculatorKey(
                        text = "3",
                        textColor = Color(0xFF1A1C18),
                        backgroundColor = Color(0xFFFCF9ED),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_3"),
                        onClick = { viewModel.onDigit("3") }
                    )
                    CalculatorKey(
                        text = "+",
                        textColor = Color(0xFF00210E),
                        backgroundColor = Color(0xFFD3E8D3),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_operator_add"),
                        onClick = { viewModel.onOperator("+") }
                    )
                }

                // Row 5: 0, ., ⌫, =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculatorKey(
                        text = "0",
                        textColor = Color(0xFF1A1C18),
                        backgroundColor = Color(0xFFFCF9ED),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_0"),
                        onClick = { viewModel.onDigit("0") }
                    )
                    CalculatorKey(
                        text = ".",
                        textColor = Color(0xFF1A1C18),
                        backgroundColor = Color(0xFFFCF9ED),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_dot"),
                        onClick = { viewModel.onDigit(".") }
                    )
                    // Backspace Key with Natural Tones visual design
                    Card(
                        onClick = { viewModel.onBackspace() },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF9ED)),
                        modifier = Modifier
                            .height(72.dp)
                            .weight(1f)
                            .testTag("btn_backspace")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = Color(0xFF1A1C18),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Equals Key in solid accent deep green
                    CalculatorKey(
                        text = "=",
                        textColor = Color.White,
                        backgroundColor = Color(0xFF386663),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_equals"),
                        onClick = { viewModel.onEqual() }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorKey(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier.height(72.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = if (text.length > 2) 18.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
        }
    }
}
