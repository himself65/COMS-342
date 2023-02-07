package arithlang;
import static arithlang.AST.*;
import static arithlang.Value.*;

import java.util.List;

public class Evaluator implements Visitor<Value> {
    private NumVal record = new NumVal("0");
    Printer.Formatter ts = new Printer.Formatter();
	
    Value valueOf(Program p) {
        // Value of a program in this language is the value of the expression
        return (Value) p.accept(this);
    }
	
    @Override
    public Value visit(AddExp e) {
        List<Exp> operands = e.all();
        String result = "";
        if (operands.size() != 2) {
            throw new NullPointerException("each operator only can take two operands");
        }
        NumVal exp0 = (NumVal) operands.get(0).accept(this);
        NumVal exp1 = (NumVal) operands.get(1).accept(this);
        String x = exp0.v();
        String y = exp1.v();
        switch (x) {
            case "0":
                return new NumVal(y);
            case "p":
                if (y.equals("0") || y.equals("p")) {
                    return new NumVal("p");
                } else {
                    return new NumVal("u");
                }
            case "n":
                if (y.equals("0") || y.equals("n")) {
                    return new NumVal("n");
                } else {
                    return new NumVal("u");
                }
            default:
                return new NumVal("u");
        }
    }

    @Override
    public Value visit(NumExp e) {
        return new NumVal(e.v());
    }

    @Override
    public Value visit(MultExp e) {
        List<Exp> operands = e.all();
        NumVal exp0 = (NumVal) operands.get(0).accept(this);
        NumVal exp1 = (NumVal) operands.get(1).accept(this);
        String x = exp0.v();
        String y = exp1.v();
        switch (x) {
            case "0":
                return new NumVal("0");
            case "p":
                return new NumVal(y);
            case "n":
                if (y.equals("0") || y.equals("u")) {
                    return new NumVal(y);
                } else if (y.equals("p")){
                    return new NumVal("n");
                } else {
                    return new NumVal("p");
                }
            default:
                if (y.equals("0")) {
                    return new NumVal("0");
                } else {
                    return new NumVal("u");
                }
        }
    }

    @Override
    public Value visit(Program p) {
        return (Value) p.e().accept(this);
    }

    @Override
    public Value visit(SubExp e) {
        List<Exp> operands = e.all();
        NumVal exp0 = (NumVal) operands.get(0).accept(this);
        NumVal exp1 = (NumVal) operands.get(1).accept(this);
        String x = exp0.v();
        String y = exp1.v();
        switch (x) {
            case "0":
                return new NumVal(y);
            case "p":
                if (y.equals("0") || y.equals("n")) {
                    return new NumVal("n");
                } else {
                    return new NumVal("u");
                }
            case "n":
                if (y.equals("0") || y.equals("p")) {
                    return new NumVal("p");
                } else {
                    return new NumVal("u");
                }
            default:
                return new NumVal("u");
        }
    }
}
