package com.sajdah.app.ui.screens.zakat

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.sajdah.app.utils.ZakatResult

@Composable
fun ZakatScreen(viewModel: ZakatViewModel = hiltViewModel(), onBack: () -> Unit = {}) {
    val zakatResult by viewModel.zakatResult.collectAsState()

    var cash by remember { mutableStateOf("") }
    var gold by remember { mutableStateOf("") }
    var silver by remember { mutableStateOf("") }
    var business by remember { mutableStateOf("") }
    var savings by remember { mutableStateOf("") }
    var investments by remember { mutableStateOf("") }
    var debts by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Zakat Calculator",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                ZakatInputField("Cash in Hand/Bank", cash) { cash = it }
                ZakatInputField("Gold Value", gold) { gold = it }
                ZakatInputField("Silver Value", silver) { silver = it }
                ZakatInputField("Business Assets", business) { business = it }
                ZakatInputField("Savings", savings) { savings = it }
                ZakatInputField("Investments", investments) { investments = it }
                ZakatInputField("Debts / Liabilities", debts) { debts = it }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.calculateZakat(
                            cash.toDoubleOrNull() ?: 0.0,
                            gold.toDoubleOrNull() ?: 0.0,
                            silver.toDoubleOrNull() ?: 0.0,
                            business.toDoubleOrNull() ?: 0.0,
                            savings.toDoubleOrNull() ?: 0.0,
                            investments.toDoubleOrNull() ?: 0.0,
                            debts.toDoubleOrNull() ?: 0.0
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Calculate Zakat", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = zakatResult != null,
            enter = fadeIn(tween(600)) + expandVertically(expandFrom = Alignment.Top) + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + shrinkVertically()
        ) {
            zakatResult?.let { result ->
                ZakatResultCard(result)
            }
        }
    }
}

@Composable
fun ZakatInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = FontFamily.Serif) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    )
}

@Composable
fun ZakatResultCard(result: ZakatResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (result.isEligible) "Zakat is Payable" else "Zakat is not Payable",
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Assets", color = Color.White.copy(alpha = 0.8f))
                    Text("$${String.format("%.2f", result.totalAssets)}", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Deducted Debts", color = Color.White.copy(alpha = 0.8f))
                    Text("-$${String.format("%.2f", result.deductedDebts)}", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Wealth", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    Text("$${String.format("%.2f", result.netWealth)}", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), thickness = 2.dp)
                Spacer(modifier = Modifier.height(24.dp))

                Text("Total Zakat Amount", color = Color.White.copy(alpha = 0.9f), fontFamily = FontFamily.Serif)
                Text(
                    text = "$${String.format("%.2f", result.zakatAmount)}",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
