package com.holik.parser;

import com.holik.analyzer.Lexeme;
import com.holik.analyzer.LexemeValue;
import com.holik.expression.*;
import com.holik.operation.Operation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Parser {

    public Expression parse(List<LexemeValue> lexemes) {
        var lexer = new Lexer(lexemes);
        lexer.next();

        return parseExpr(lexer);
    }

    private Expression parseExpr(Lexer lexer) {
        return parseBinary(lexer, 1);
    }

    private Expression parseBinary(Lexer lexer, int prec1) {
        var lhs = parseUnary(lexer);
        for (int prec = precedence(lexer.token().getLexeme()); prec >= prec1; prec--) {
            while (precedence(lexer.token().getLexeme()) == prec) {
                var lexeme = lexer.token().getLexeme();
                Operation operation = getOperation(lexeme);
                lexer.next();
                var rhs = parseBinary(lexer, prec + 1);
                lhs = new Binary(lhs, rhs, operation);
            }
        }
        return lhs;
    }

    private static Operation getOperation(Lexeme lexeme) {
        Operation operation;
        if (lexeme.equals(Lexeme.PLUS)) {
            operation = Operation.ADDITION;
        } else if (lexeme.equals(Lexeme.MINUS)) {
            operation = Operation.SUBTRACTION;
        } else if (lexeme.equals(Lexeme.MULTIPLY)) {
            operation = Operation.MULTIPLICATION;
        } else if (lexeme.equals(Lexeme.DIVIDE)) {
            operation = Operation.DIVISION;
        } else {
            throw new RuntimeException("Unexpected operation: " + lexeme);
        }
        return operation;
    }

    private Expression parseUnary(Lexer lexer) {
        if (lexer.token().getLexeme().equals(Lexeme.PLUS)) {
            var operation = Operation.ADDITION;
            lexer.next();
            return new Unary(parseUnary(lexer), operation);
        } else if (lexer.token().getLexeme().equals(Lexeme.MINUS)) {
            var operation = Operation.SUBTRACTION;
            lexer.next();
            return new Unary(parseUnary(lexer), operation);
        }
        return parsePrimary(lexer);
    }

    private Expression parsePrimary(Lexer lexer) {
        if (lexer.token().getLexeme().equals(Lexeme.VARIABLE)) {
            var variable = lexer.token().getValue();
            lexer.next();
            return new Variable(variable);
        } else if (lexer.token().getLexeme().equals(Lexeme.FUNCTION_NAME)) {
            var functionName = lexer.token().getValue();
            lexer.next();
            lexer.next();
            var args = new ArrayList<Expression>();
            if (!lexer.token().getLexeme().equals(Lexeme.CLOSING_BRACKET)) {
                while (true) {
                    args.add(parseExpr(lexer));
                    if (!lexer.token().getLexeme().equals(Lexeme.COMMA)) {
                        break;
                    }
                    lexer.next();
                }

                if (!lexer.token().getLexeme().equals(Lexeme.CLOSING_BRACKET)) {
                    throw new RuntimeException("Invalid function closure, expected ')', got " + lexer.token().getValue());
                }
            }
            lexer.next();
            return new Call(args, functionName);
        } else if (lexer.token().getLexeme().equals(Lexeme.CONSTANT)) {
            var constant = lexer.token().getValue();
            lexer.next();
            return new Constant(constant);
        } else if (lexer.token().getLexeme().equals(Lexeme.OPENING_BRACKET)) {
            lexer.next();
            var expression = parseExpr(lexer);
            if (!lexer.token().getLexeme().equals(Lexeme.CLOSING_BRACKET)) {
                throw new RuntimeException("Expected ')', got " + lexer.token().getValue());
            }
            lexer.next();
            return expression;
        }
        throw new RuntimeException("Unexpected " + lexer.token().getValue());
    }

    private int precedence(Lexeme operation) {
        if (operation.equals(Lexeme.MULTIPLY) || operation.equals(Lexeme.DIVIDE)) {
            return 2;
        } else if (operation.equals(Lexeme.PLUS) || operation.equals(Lexeme.MINUS)) {
            return 1;
        }
        return 0;
    }
}
