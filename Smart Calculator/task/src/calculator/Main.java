package calculator;

import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigInteger;

enum ErrorCode {
    NONE,
    UNKNOWN_COMMAND,
    INVALID_EXPRESSION,
    INVALID_IDENTIFIER,
    INVALID_ASSIGNMENT,
    UNDEFINED_VARIABLE
}

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Pattern numberPattern = Pattern.compile("[-+]?\\d+");
    private static final Pattern validVariable = Pattern.compile("[a-zA-Z]+");
    private static final Pattern expressionPattern = Pattern.compile(
            "([a-zA-Z]+|\\d+|[-+*/()^])"
    );

    private static final Map<String, BigInteger> variables = new TreeMap<>();

    private static ErrorCode currentError = ErrorCode.NONE;

    private static void processCommand(String in) {
        if (in.equals("/help")) {
            System.out.println(
                    "The program evaluates sums and subtractions of numbers"
            );
        } else {
            currentError = ErrorCode.UNKNOWN_COMMAND;
        }
    }

    private static BigInteger getValue(String in) {
        Matcher variableMatcher = validVariable.matcher(in);
        Matcher numberMatcher = numberPattern.matcher(in);
        if (variableMatcher.matches()) {
            if (variables.containsKey(in)) {
                return variables.get(in);
            } else {
                currentError = ErrorCode.UNDEFINED_VARIABLE;
                return BigInteger.ZERO;
            }
        } else if (numberMatcher.matches()) {
            return new BigInteger(in);
        } else {
            currentError = ErrorCode.INVALID_EXPRESSION;
            return BigInteger.ZERO;
        }
    }

    private static void processAssignment(String in) {
        String[] arr = in.split("=");
        if (arr.length != 2) {
            currentError = ErrorCode.INVALID_ASSIGNMENT;
            return;
        }

        String varName = arr[0].trim();
        Matcher variableMatcher = validVariable.matcher(varName);
        if (!variableMatcher.matches()) {
            currentError = ErrorCode.INVALID_IDENTIFIER;
            return;
        }

        String assignment = arr[1].trim();
        BigInteger value = processExpression(assignment);
        if (currentError == ErrorCode.NONE) variables.put(varName, value);
    }

    private static int getPriority(String in) {
        switch (in) {
            case "(":
                return 3;
            case "^":
                return 2;
            case "*":
            case "/":
                return 1;
            case "+":
            case "-":
            default:
                return 0;
        }
    }

    private static String infixToPostfix(String in) {
        Stack<String> stack = new Stack<>();

        if (in.startsWith("+")) in = in.replaceFirst("\\+", "");
        if (in.startsWith("-")) in = in.replaceFirst("-", "0-");

        in = in.replaceAll("[-]{2}", "+");
        in = in.replaceAll("[+]+[-]", "-");
        in = in.replaceAll("[+]+", "+");

        Matcher expressionMatcher = expressionPattern.matcher(in);
        StringBuilder expression = new StringBuilder();

        while (expressionMatcher.find()) {
            String current = in.substring(
                    expressionMatcher.start(), expressionMatcher.end()
            );

            Matcher numMatcher = numberPattern.matcher(current);
            Matcher varMatcher = validVariable.matcher(current);

            if (numMatcher.matches() || varMatcher.matches()) {
                expression.append(getValue(current)).append(" ");
            } else {
                if (current.equals("(")) {
                    stack.push(current);
                } else if (current.equals(")")) {
                    while (!stack.empty() && !stack.peek().equals("(")) {
                        expression.append(stack.pop()).append(" ");
                    }

                    if (stack.empty()) {
                        currentError = ErrorCode.INVALID_EXPRESSION;
                        return "";
                    } else {
                        stack.pop();
                    }
                } else if (stack.empty() || stack.peek().equals("(")) {
                    stack.push(current);
                } else if (getPriority(stack.peek()) < getPriority(current)) {
                    stack.push(current);
                } else {
                    while (!stack.empty() && !stack.peek().equals("(") &&
                            getPriority(stack.peek()) >= getPriority(current)) {
                        expression.append(stack.pop()).append(" ");
                    }

                    stack.push(current);
                }
            }
        }

        while (!stack.empty()) {
            if (stack.peek().equals("(")) currentError = ErrorCode.INVALID_EXPRESSION;
            expression.append(stack.pop()).append(" ");
        }

        return expression.toString();
    }

    private static BigInteger doOperation(String op, BigInteger a, BigInteger b) {
        switch(op) {
            case "+":
                return a.add(b);
            case "-":
                return a.subtract(b);
            case "*":
                return a.multiply(b);
            case "/":
                return a.divide(b);
            case "^":
                return a.pow(b.intValue());
            default:
                currentError = ErrorCode.INVALID_EXPRESSION;
                return BigInteger.ZERO;
        }
    }

    private static BigInteger processExpression(String in) {
        Stack<BigInteger> stack = new Stack<>();
        String[] expressionMember = infixToPostfix(in).split(" ");

        for (String s : expressionMember) {
            Matcher numMatcher = numberPattern.matcher(s);
            if (numMatcher.matches()) {
                stack.push(new BigInteger(s));
            } else {
                if (stack.size() >= 2) {
                    BigInteger a = stack.pop();
                    BigInteger b = stack.pop();
                    stack.push(doOperation(s, b, a));
                } else {
                    currentError = ErrorCode.INVALID_EXPRESSION;
                }
            }
        }

        if (stack.size() == 1) return stack.pop();
        else {
            currentError = ErrorCode.INVALID_EXPRESSION;
            return BigInteger.ZERO;
        }
    }

    private static void sendError() {
        switch(currentError) {
            case UNKNOWN_COMMAND:
                System.out.println("Unknown Command");
                break;
            case INVALID_EXPRESSION:
                System.out.println("Invalid Expression");
                break;
            case INVALID_ASSIGNMENT:
                System.out.println("Invalid assignment");
                break;
            case INVALID_IDENTIFIER:
                System.out.println("Invalid identifier");
                break;
            case UNDEFINED_VARIABLE:
                System.out.println("Unknown variable");
                break;
        }
    }

    public static void main(String[] args) {
        String in = scanner.nextLine();
        while(!in.equals("/exit")) {
            currentError = ErrorCode.NONE;
            if (in.isEmpty()) {
                in = scanner.nextLine();
                continue;
            }

            if (in.startsWith("/")) {
                processCommand(in);
            }
            else if (in.contains("=")) {
                processAssignment(in);
            }
            else {
                BigInteger ans = processExpression(in);
                if (currentError == ErrorCode.NONE) {
                    System.out.println(ans);
                }
            }

            sendError();

            in = scanner.nextLine();
        }
        System.out.println("Bye!");
    }
}
