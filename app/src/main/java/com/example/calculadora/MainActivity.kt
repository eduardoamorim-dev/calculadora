package com.example.calculadora

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MainActivity : Activity() {

    private lateinit var tvDisplay: TextView

    // Variáveis para controlar o estado da calculadora
    private var currentNumber = ""
    private var previousNumber = ""
    private var currentOperator = ""
    private var isNewCalculation = true
    private var lastOperatorPressed = false

    // Formato para números decimais
    private val decimalFormat = DecimalFormat("#.##########",
        DecimalFormatSymbols(Locale.US))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupButtonListeners()
    }

    private fun initializeViews() {
        tvDisplay = findViewById(R.id.tvDisplay)
    }

    private fun setupButtonListeners() {
        // Botões numéricos
        val numberButtons = arrayOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        numberButtons.forEach { buttonId ->
            findViewById<Button>(buttonId).setOnClickListener {
                onNumberClick(it as Button)
            }
        }

        // Botões de operadores
        findViewById<Button>(R.id.btnAdd).setOnClickListener { onOperatorClick("+") }
        findViewById<Button>(R.id.btnSubtract).setOnClickListener { onOperatorClick("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { onOperatorClick("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { onOperatorClick("÷") }

        // Outros botões
        findViewById<Button>(R.id.btnEquals).setOnClickListener { onEqualsClick() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { onClearClick() }
        findViewById<Button>(R.id.btnDecimal).setOnClickListener { onDecimalClick() }
        findViewById<Button>(R.id.btnPlusMinus).setOnClickListener { onPlusMinusClick() }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { onPercentClick() }
    }

    private fun onNumberClick(button: Button) {
        val number = button.text.toString()

        if (isNewCalculation) {
            currentNumber = number
            isNewCalculation = false
        } else {
            // Limita o número de dígitos para evitar overflow
            if (currentNumber.length < 10) {
                currentNumber += number
            }
        }

        lastOperatorPressed = false
        updateDisplay(formatNumber(currentNumber.toDouble()))
    }

    private fun onOperatorClick(operator: String) {
        // Previne operadores consecutivos
        if (lastOperatorPressed && currentOperator.isNotEmpty()) {
            currentOperator = operator
            return
        }

        if (currentNumber.isNotEmpty()) {
            if (previousNumber.isNotEmpty() && !lastOperatorPressed) {
                calculateResult()
            }

            previousNumber = currentNumber
            currentNumber = ""
            currentOperator = operator
            lastOperatorPressed = true
            isNewCalculation = false
        }
    }

    private fun onEqualsClick() {
        if (previousNumber.isNotEmpty() && currentNumber.isNotEmpty() && currentOperator.isNotEmpty()) {
            calculateResult()
            previousNumber = ""
            currentOperator = ""
            isNewCalculation = true
        }
        lastOperatorPressed = false
    }

    private fun calculateResult() {
        try {
            val prev = previousNumber.toDouble()
            val curr = currentNumber.toDouble()

            val result = when (currentOperator) {
                "+" -> prev + curr
                "-" -> prev - curr
                "×" -> prev * curr
                "÷" -> {
                    if (curr == 0.0) {
                        showError("Não é possível dividir por zero!")
                        return
                    }
                    prev / curr
                }
                else -> curr
            }

            // Verifica se o resultado é muito grande
            if (result.isInfinite() || result.isNaN()) {
                showError("Erro: Resultado inválido")
                return
            }

            currentNumber = result.toString()
            updateDisplay(formatNumber(result))

        } catch (e: Exception) {
            showError("Erro no cálculo")
        }
    }

    private fun onClearClick() {
        currentNumber = ""
        previousNumber = ""
        currentOperator = ""
        isNewCalculation = true
        lastOperatorPressed = false
        updateDisplay("0")
    }

    private fun onDecimalClick() {
        if (isNewCalculation) {
            currentNumber = "0."
            isNewCalculation = false
        } else if (!currentNumber.contains(".") && currentNumber.isNotEmpty()) {
            currentNumber += "."
        } else if (currentNumber.isEmpty()) {
            currentNumber = "0."
        }

        lastOperatorPressed = false
        updateDisplay(currentNumber)
    }

    private fun onPlusMinusClick() {
        if (currentNumber.isNotEmpty() && currentNumber != "0") {
            currentNumber = if (currentNumber.startsWith("-")) {
                currentNumber.substring(1)
            } else {
                "-$currentNumber"
            }
            updateDisplay(formatNumber(currentNumber.toDouble()))
        }
    }

    private fun onPercentClick() {
        if (currentNumber.isNotEmpty()) {
            try {
                val number = currentNumber.toDouble()
                val result = number / 100
                currentNumber = result.toString()
                updateDisplay(formatNumber(result))
            } catch (e: Exception) {
                showError("Erro no cálculo de porcentagem")
            }
        }
    }

    private fun updateDisplay(text: String) {
        tvDisplay.text = text
    }

    private fun formatNumber(number: Double): String {
        // Se é um número inteiro, mostra sem casas decimais
        return if (number == number.toInt().toDouble()) {
            number.toInt().toString()
        } else {
            decimalFormat.format(number)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        onClearClick() // Limpa a calculadora após o erro
    }
}