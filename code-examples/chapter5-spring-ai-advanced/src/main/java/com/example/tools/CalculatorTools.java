/**
 * Calculator Tools Class
 * Provides basic mathematical operations to solve AI calculation accuracy issues
 */
package com.example.tools;

import org.springframework.stereotype.Component;

@Component
public class CalculatorTools {

    /**
     * Basic mathematical operations
     * @param operation Operation type (add, subtract, multiply, divide)
     * @param a First number
     * @param b Second number
     * @return Operation result
     */
    public String calculate(String operation, double a, double b) {
        try {
            double result;

            if ("add".equalsIgnoreCase(operation)) {
                result = a + b;
            } else if ("subtract".equalsIgnoreCase(operation)) {
                result = a - b;
            } else if ("multiply".equalsIgnoreCase(operation)) {
                result = a * b;
            } else if ("divide".equalsIgnoreCase(operation)) {
                if (b == 0) {
                    return "Error: Cannot divide by zero";
                }
                result = a / b;
            } else {
                return "Unsupported operation: " + operation;
            }

            return String.format("%.2f %s %.2f = %.2f", a, getOperationSymbol(operation), b, result);

        } catch (Exception e) {
            return "Calculation error: " + e.getMessage();
        }
    }

    /**
     * Complex mathematical expressions
     * @param expression Mathematical expression (e.g., "2 + 3 * 4")
     * @return Calculation result
     */
    public String evaluateExpression(String expression) {
        try {
            // This can be integrated with a math expression parser
            // For now, just a placeholder
            return "Expression evaluation for '" + expression + "' is under development";
        } catch (Exception e) {
            return "Expression evaluation error: " + e.getMessage();
        }
    }

    private String getOperationSymbol(String operation) {
        if ("add".equalsIgnoreCase(operation)) {
            return "+";
        } else if ("subtract".equalsIgnoreCase(operation)) {
            return "-";
        } else if ("multiply".equalsIgnoreCase(operation)) {
            return "*";
        } else if ("divide".equalsIgnoreCase(operation)) {
            return "/";
        } else {
            return "?";
        }
    }
}
