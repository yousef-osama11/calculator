package com.example.calculator

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var firstNumber: String
    lateinit var secondNumber: String
    var currentText: String = "0"
    var currentOperation: Operation = Operation.Plus

    var enableErase: Boolean = true
    var availableDeletedCharactersNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        addCallBacks()
    }

    private fun addCallBacks() {
        binding.buttonPlus.setOnClickListener {
            currentOperation = Operation.Plus
            onClickOperation()
        }
        binding.buttonMinus.setOnClickListener {
            currentOperation = Operation.Minus
            onClickOperation()
        }
        binding.buttonMultiply.setOnClickListener {
            currentOperation = Operation.Mul
            onClickOperation()
        }
        binding.division.setOnClickListener {
            currentOperation = Operation.Div
            onClickOperation()
        }
        binding.allClear.setOnClickListener {
            enableErase = false
            availableDeletedCharactersNumber = 0
            clearResult()
        }
        binding.erase.setOnClickListener {
            if (currentText.equals("Error") || binding.mainText.text.equals("Error")) {
                currentText = "0"
                binding.mainText.text = currentText
            }
            else if (enableErase){
                availableDeletedCharactersNumber -=1
                deleteLastDigit()
            }
        }
        binding.buttonEqual.setOnClickListener {
            enableErase = false
            availableDeletedCharactersNumber = 0
            doCalculation()
        }
        binding.plusMinus.setOnClickListener {
            availableDeletedCharactersNumber +=1
            onClickRevertSignButton()
        }
        binding.percent.setOnClickListener {
            currentOperation = Operation.Percent
            onClickOperation()
        }
    }

    private fun doCalculation() {
        try {
            if (currentText.startsWith("-")) {
                currentText = "0$currentText"
            }
            var result = calculateExpression(currentText).toString()
            if (result.endsWith(".0"))
                result = result.dropLast(2)
            if (result.equals("NaN") || result.equals("Infinity") || result.equals("-Infinity")) {
                result = "0"
            }
            binding.mainText.text = result
            if (currentText.startsWith("0-")) {
                currentText = currentText.drop(1)
            }
            if (currentText.endsWith("."))
                currentText = currentText.dropLast(1)
            binding.subText.text = currentText
            currentText = result
        } catch (e: Exception) {
            binding.mainText.text = "Error"
        }
    }

    private fun calculateExpression(expression: String): Double {
        val modifiedExpression = expression.replace(" ", "")
        val tokens = mutableListOf<String>()
        var number = ""

        if (modifiedExpression.first() == '.') {
            number += '0'
        }

        for ((index, ch) in modifiedExpression.withIndex()) {
            if (ch.isDigit() || ch == '.') {
                number += ch
            } else {
                if (number.isNotEmpty()) {
                    tokens.add(number)
                    number = ""
                }
                if (ch == '%') {
                    val lastIndex = tokens.lastIndex
                    val percentValue = tokens[lastIndex].toDouble()

                    val nextOp = if (index + 1 < modifiedExpression.length) modifiedExpression[index + 1] else null

                    if (nextOp == 'x' || nextOp == '/' || (lastIndex > 0 && (tokens[lastIndex - 1] == "x" || tokens[lastIndex - 1] == "/"))) {
                        tokens[lastIndex] = (percentValue / 100).toString()
                    } else {
                        tokens[lastIndex] = (percentValue).toString() + "%"
                    }
                } else {
                    tokens.add(ch.toString())
                }
            }
        }
        if (number.isNotEmpty()) tokens.add(number)

        var i = 0
        while (i < tokens.size) {
            if (tokens[i] == "x" || tokens[i] == "/") {
                val left = tokens[i - 1].let {
                    if (it.endsWith("%")) (tokens[i - 2].toDouble() * (it.dropLast(1).toDouble() / 100)).toString()
                    else it
                }.toDouble()

                val right = tokens[i + 1].let {
                    if (it.endsWith("%")) (it.dropLast(1).toDouble() / 100) else it.toDouble()
                }

                val result = when (tokens[i]) {
                    "x" -> left * right
                    "/" -> left / right
                    else -> 0.0
                }
                tokens[i - 1] = result.toString()
                tokens.removeAt(i)
                tokens.removeAt(i)
                i--
            }
            i++
        }

        var result = tokens[0].toDouble()
        i = 1
        while (i < tokens.size) {
            val op = tokens[i]
            var next = tokens[i + 1]
            if (next.endsWith("%")) {
                val perc = next.dropLast(1).toDouble()
                next = (result * (perc / 100)).toString()
            }
            val nextVal = next.toDouble()
            when (op) {
                "+" -> result += nextVal
                "-" -> result -= nextVal
            }
            i += 2
        }

        return result
    }
    private fun clearResult() {
        binding.mainText.text = "0"
        binding.subText.text = ""
        firstNumber = ""
        secondNumber = ""
        currentText = "0"
        currentOperation = Operation.Plus
    }

    private fun deleteLastDigit() {
        currentText = currentText.dropLast(1)
        binding.mainText.text = currentText
        if (currentText.isEmpty()) {
            binding.mainText.text = "0"
            currentText = "0"
        }
    }

    private fun fixCurrentText(){
        if (currentText.equals("Error") || binding.mainText.text.equals("Error")) {
            currentText = "0"
            binding.mainText.text = currentText
        }
    }

    private fun onClickOperation() {
        fixCurrentText()
        enableErase = true
        availableDeletedCharactersNumber +=3
        if (currentText.last() == '+' || currentText.last() == '-' || currentText.last() == 'x' || currentText.last() == '/') {
            currentText = currentText.dropLast(1)
        }
        if (!currentOperation.value.equals("%"))
            currentText = currentText + " " + currentOperation.value + " "
        else currentText = currentText + currentOperation.value
        binding.mainText.text = currentText
    }

    private fun doCurrentOperation(): Double {
        return when (currentOperation) {
            Operation.Plus -> firstNumber.toDouble() + secondNumber.toDouble()
            Operation.Minus -> firstNumber.toDouble() - secondNumber.toDouble()
            Operation.Mul -> firstNumber.toDouble() * secondNumber.toDouble()
            Operation.Div -> firstNumber.toDouble() / secondNumber.toDouble()
            Operation.Percent -> firstNumber.toDouble() % secondNumber.toDouble()
        }
    }

    fun onClickNumber(view: View) {
        fixCurrentText()
        enableErase = true
        availableDeletedCharactersNumber +=1
        if (binding.mainText.text.toString().equals("0")) {
            binding.mainText.text = ""
        }
        val newDigits = (view as androidx.appcompat.widget.AppCompatButton).text.toString()
        val oldDigits = binding.mainText.text
        currentText = oldDigits.toString() + newDigits
        binding.mainText.text = currentText
    }

    private fun onClickRevertSignButton() {
        if (!currentText.startsWith("0")) {
            if (currentText.startsWith("-")) {
                currentText = currentText.drop(1)
            } else {
                currentText = "-$currentText"
            }
            binding.mainText.text = currentText
        }
    }
}

enum class Operation(val value: String) {
    Plus("+"),
    Minus("-"),
    Mul("x"),
    Div("/"),
    Percent("%")
}