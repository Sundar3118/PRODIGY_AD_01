package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class CalculatorViewModel : ViewModel() {

    // List of tokens in the current formula (e.g. ["12", "+", "3.5", "×"])
    private val tokens = mutableListOf<String>()

    // Current active operand being entered by the user
    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()

    // The display expression shown on the upper line (e.g., "12 + 3.5 ×")
    private val _expressionDisplay = MutableStateFlow("")
    val expressionDisplay: StateFlow<String> = _expressionDisplay.asStateFlow()

    // Real-time live result shown under the active input
    private val _liveResult = MutableStateFlow("")
    val liveResult: StateFlow<String> = _liveResult.asStateFlow()

    // Flag indicating if the last operation was pressing "=" (evaluation completed)
    private var justCalculated = false

    fun onDigit(digit: String) {
        if (justCalculated) {
            _currentInput.value = ""
            tokens.clear()
            justCalculated = false
        }

        val current = _currentInput.value

        // Prevent multiple decimal points
        if (digit == "." && current.contains(".")) {
            return
        }

        // Prevent multiple leading zeros
        if (digit == "0" && current == "0") {
            return
        }

        // If typing a digit when current is "0", replace it (unless decimal is being typed)
        if (digit != "." && current == "0") {
            _currentInput.value = digit
        } else {
            _currentInput.value = current + digit
        }

        updateDisplayAndLiveResult()
    }

    fun onOperator(operator: String) {
        // If we just calculated a result, we can continue using that result with the new operator
        if (justCalculated) {
            justCalculated = false
        }

        val current = _currentInput.value

        if (current.isNotEmpty()) {
            // Push active input to tokens list
            tokens.add(current)
            _currentInput.value = ""
        }

        if (tokens.isNotEmpty()) {
            val lastToken = tokens.last()
            if (isOperator(lastToken)) {
                // Replace last operator with new one
                tokens[tokens.size - 1] = operator
            } else {
                tokens.add(operator)
            }
        } else if (operator == "-") {
            // Allow starting a negative number if the expression is empty
            _currentInput.value = "-"
        }

        updateDisplayAndLiveResult()
    }

    fun onClear() {
        tokens.clear()
        _currentInput.value = ""
        _expressionDisplay.value = ""
        _liveResult.value = ""
        justCalculated = false
    }

    fun onBackspace() {
        if (justCalculated) {
            // Backspacing on a completed calculation clears the result
            onClear()
            return
        }

        val current = _currentInput.value
        if (current.isNotEmpty()) {
            // Remove last character from the active number input
            _currentInput.value = current.substring(0, current.length - 1)
        } else if (tokens.isNotEmpty()) {
            // If active input is empty, pop the last operator or operand from the tokens
            val removed = tokens.removeAt(tokens.size - 1)
            // If the popped token was an operator, check if the previous token is a number
            // and move it back into _currentInput so the user can edit it.
            if (isOperator(removed)) {
                if (tokens.isNotEmpty() && !isOperator(tokens.last())) {
                    _currentInput.value = tokens.removeAt(tokens.size - 1)
                }
            }
        }

        updateDisplayAndLiveResult()
    }

    fun onToggleSign() {
        if (justCalculated) {
            justCalculated = false
        }

        val current = _currentInput.value
        if (current.isNotEmpty() && current != "-") {
            _currentInput.value = if (current.startsWith("-")) {
                current.substring(1)
            } else {
                "-$current"
            }
        } else if (current.isEmpty() && tokens.isNotEmpty() && !isOperator(tokens.last())) {
            // Toggle the sign of the last operand in tokens by moving it to input
            val lastOperand = tokens.removeAt(tokens.size - 1)
            _currentInput.value = if (lastOperand.startsWith("-")) {
                lastOperand.substring(1)
            } else {
                "-$lastOperand"
            }
        } else if (current == "-") {
            _currentInput.value = ""
        } else {
            _currentInput.value = "-"
        }

        updateDisplayAndLiveResult()
    }

    fun onPercentage() {
        if (justCalculated) {
            justCalculated = false
        }

        val current = _currentInput.value
        if (current.isNotEmpty()) {
            val value = current.toDoubleOrNull()
            if (value != null) {
                _currentInput.value = formatNumber(value / 100.0)
            }
        } else if (tokens.isNotEmpty() && !isOperator(tokens.last())) {
            val lastOperand = tokens.removeAt(tokens.size - 1)
            val value = lastOperand.toDoubleOrNull()
            if (value != null) {
                _currentInput.value = formatNumber(value / 100.0)
            }
        }
        updateDisplayAndLiveResult()
    }

    fun onEqual() {
        val current = _currentInput.value
        if (current.isNotEmpty()) {
            tokens.add(current)
            _currentInput.value = ""
        }

        if (tokens.isEmpty()) {
            return
        }

        // Evaluate the full list of tokens
        val resultValue = evaluateTokens(tokens)
        if (resultValue != null) {
            // Format equation for display before clearing tokens
            _expressionDisplay.value = tokens.joinToString(" ")
            _currentInput.value = formatNumber(resultValue)
            tokens.clear()
            _liveResult.value = ""
            justCalculated = true
        } else {
            _currentInput.value = "Error"
            tokens.clear()
            _liveResult.value = ""
            justCalculated = true
        }
    }

    private fun isOperator(token: String): Boolean {
        return token == "+" || token == "-" || token == "×" || token == "÷" || token == "%"
    }

    private fun updateDisplayAndLiveResult() {
        val current = _currentInput.value
        val fullTokens = if (current.isNotEmpty()) tokens + current else tokens

        // Build display expression
        _expressionDisplay.value = tokens.joinToString(" ")

        // Compute live preview of the current partial expression
        if (fullTokens.size >= 3) {
            // Clean trailing operator for evaluation
            val evaluableTokens = if (isOperator(fullTokens.last())) {
                fullTokens.dropLast(1)
            } else {
                fullTokens
            }

            val eval = evaluateTokens(evaluableTokens)
            if (eval != null) {
                _liveResult.value = formatNumber(eval)
            } else {
                _liveResult.value = ""
            }
        } else {
            _liveResult.value = ""
        }
    }

    private fun evaluateTokens(inputTokens: List<String>): Double? {
        if (inputTokens.isEmpty()) return null

        // Pass 1: Handle multiplication (×), division (÷), and modulo (%) under operator precedence
        val firstPass = mutableListOf<String>()
        var i = 0
        while (i < inputTokens.size) {
            val token = inputTokens[i]
            if (token == "×" || token == "÷" || token == "%") {
                if (firstPass.isEmpty() || i + 1 >= inputTokens.size) return null
                val prevOperandStr = firstPass.removeAt(firstPass.size - 1)
                val nextOperandStr = inputTokens[i + 1]
                val prevVal = prevOperandStr.toDoubleOrNull() ?: return null
                val nextVal = nextOperandStr.toDoubleOrNull() ?: return null

                val result = when (token) {
                    "×" -> prevVal * nextVal
                    "÷" -> {
                        if (nextVal == 0.0) return null // Division by zero
                        prevVal / nextVal
                    }
                    "%" -> prevVal % nextVal
                    else -> 0.0
                }
                firstPass.add(result.toString())
                i += 2
            } else {
                firstPass.add(token)
                i++
            }
        }

        if (firstPass.isEmpty()) return null

        // Pass 2: Handle addition (+) and subtraction (-)
        var result = firstPass[0].toDoubleOrNull() ?: return null
        var j = 1
        while (j < firstPass.size) {
            val operator = firstPass[j]
            if (j + 1 >= firstPass.size) return null
            val nextVal = firstPass[j + 1].toDoubleOrNull() ?: return null

            result = when (operator) {
                "+" -> result + nextVal
                "-" -> result - nextVal
                else -> return null
            }
            j += 2
        }

        return result
    }

    private fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"

        // Check if practically an integer (within small delta)
        val epsilon = 1e-9
        if (Math.abs(value - Math.round(value)) < epsilon) {
            val longVal = Math.round(value)
            if (longVal >= 1_000_000_000_000L || longVal <= -1_000_000_000_000L) {
                return String.format(Locale.US, "%.6e", value)
            }
            return longVal.toString()
        }

        // Limit decimal places to 10 to avoid float-point rounding noise
        val formatted = String.format(Locale.US, "%.10f", value).replace(Regex("0+$"), "")
        val clean = if (formatted.endsWith(".")) formatted.substring(0, formatted.length - 1) else formatted

        if (clean.length > 15) {
            return String.format(Locale.US, "%.6e", value)
        }
        return clean
    }
}
