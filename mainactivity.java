package com.example.mycalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextDisplay;
    private final StringBuilder currentInput = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextDisplay = findViewById(R.id.editTextDisplay);
    }

    public void onButtonClick(View view) {
        Button button = (Button) view;
        currentInput.append(button.getText().toString());
        updateDisplay();
    }

    public void onEqualsButtonClick(View view) {
        String expression = currentInput.toString();
        try {
            double result = evaluateExpression(expression);
            currentInput.setLength(0);
            currentInput.append(result);
            updateDisplay();
        } catch (Exception e) {
            currentInput.setLength(0);
            currentInput.append("Error");
            updateDisplay();
        }
    }

    public void onClearButtonClick(View view) {
        currentInput.setLength(0);
        updateDisplay();
    }

    public void onDeleteButtonClick(View view) {
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            updateDisplay();
        }
    }

    private void updateDisplay() {
        editTextDisplay.setText(currentInput.toString());
    }

    private double evaluateExpression(String expression) {
        // Use regex to replace any non-numeric or non-operator characters
        String sanitizedExpression = expression.replaceAll("[^0-9+\\-*/.]", "");

        // Check for division by zero
        if (sanitizedExpression.contains("/0"))
            throw new ArithmeticException("Division by zero");

        // Use eval method to evaluate the expression
        return eval(sanitizedExpression);
    }

    private double eval(final String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ')
                    nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length())
                    throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+'))
                        x += parseTerm();
                    else if (eat('-'))
                        x -= parseTerm();
                    else
                        return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*'))
                        x *= parseFactor();
                    else if (eat('/'))
                        x /= parseFactor();
                    else
                        return x;
                }
            }

            double parseFactor() {
                if (eat('+'))
                    return parseFactor();
                if (eat('-'))
                    return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.')
                        nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }
}
