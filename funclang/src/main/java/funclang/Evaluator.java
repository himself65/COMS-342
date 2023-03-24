package funclang;

import funclang.Env.ExtendEnv;
import funclang.Env.GlobalEnv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static funclang.AST.*;
import static funclang.Value.*;

public class Evaluator implements Visitor<Value> {

  Printer.Formatter ts = new Printer.Formatter();

  Env initEnv = initialEnv(); // New for definelang

  Value valueOf(Program p) {
    return (Value) p.accept(this, initEnv);
  }

  @Override
  public Value visit(AddExp e, Env env) {
    List<Exp> operands = e.all();
    double result = 0;
    for (Exp exp : operands) {
      NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
      result += intermediate.v(); // Semantics of AddExp in terms of the target language.
    }
    return new NumVal(result);
  }

  @Override
  public Value visit(ConjExp e, Env env) {
    List<Exp> operands = e.all();
    boolean result = true;
    for (Exp exp : operands) {
      BoolVal intermediate = (BoolVal) exp.accept(this, env); // Dynamic type-checking
      result = result && intermediate.v(); // Semantics of ConjExp in terms of the target language.
    }
    return new BoolVal(result);
  }

  @Override
  public Value visit(DisjExp e, Env env) {
    List<Exp> operands = e.all();
    boolean result = false;
    for (Exp exp : operands) {
      BoolVal intermediate = (BoolVal) exp.accept(this, env); // Dynamic type-checking
      result = result || intermediate.v(); // Semantics of DisjExp in terms of the target language.
    }
    return new BoolVal(result);
  }

  @Override
  public Value visit(UnitExp e, Env env) {
    return new UnitVal();
  }

  @Override
  public Value visit(NumExp e, Env env) {
    return new NumVal(e.v());
  }

  @Override
  public Value visit(StrExp e, Env env) {
    return new StringVal(e.v());
  }

  @Override
  public Value visit(BoolExp e, Env env) {
    return new BoolVal(e.v());
  }

  @Override
  public Value visit(DivExp e, Env env) {
    List<Exp> operands = e.all();
    NumVal lVal = (NumVal) operands.get(0).accept(this, env);
    double result = lVal.v();
    for (int i = 1; i < operands.size(); i++) {
      NumVal rVal = (NumVal) operands.get(i).accept(this, env);
      result = result / rVal.v();
    }
    return new NumVal(result);
  }

  @Override
  public Value visit(MultExp e, Env env) {
    List<Exp> operands = e.all();
    double result = 1;
    for (Exp exp : operands) {
      NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
      result *= intermediate.v(); // Semantics of MultExp.
    }
    return new NumVal(result);
  }

  @Override
  public Value visit(Program p, Env env) {
    try {
      for (DefineDecl d : p.decls())
        d.accept(this, initEnv);
      return (Value) p.e().accept(this, initEnv);
    } catch (ClassCastException e) {
      return new DynamicError(e.getMessage());
    }
  }

  @Override
  public Value visit(SubExp e, Env env) {
    List<Exp> operands = e.all();
    NumVal lVal = (NumVal) operands.get(0).accept(this, env);
    double result = lVal.v();
    for (int i = 1; i < operands.size(); i++) {
      NumVal rVal = (NumVal) operands.get(i).accept(this, env);
      result = result - rVal.v();
    }
    return new NumVal(result);
  }

  @Override
  public Value visit(VarExp e, Env env) {
    // Previously, all variables had value 42. New semantics.
    return env.get(e.name());
  }

  @Override
  public Value visit(LetExp e, Env env) { // New for varlang.
    List<String> names = e.names();
    List<Exp> value_exps = e.value_exps();
    List<Value> values = new ArrayList<Value>(value_exps.size());

    for (Exp exp : value_exps)
      values.add((Value) exp.accept(this, env));

    Env new_env = env;
    for (int index = 0; index < names.size(); index++)
      new_env = new ExtendEnv(new_env, names.get(index), values.get(index));

    return (Value) e.body().accept(this, new_env);
  }

  @Override
  public Value visit(DefineDecl e, Env env) { // New for definelang.
    String name = e.name();
    Exp value_exp = e.value_exp();
    Value value = (Value) value_exp.accept(this, env);
    ((GlobalEnv) initEnv).extend(name, value);
    return new UnitVal();
  }

  @Override
  public Value visit(LambdaExp e, Env env) {
    // Create a function value with three components:
    // 1. formal parameters of the function - e.formals()
    // 2. actual body of the function - e.body()
    // 3. mapping from the free variables in the function body to their values.
    return new FunVal(env, e.formals(), e.body());
  }

  @Override
  public Value visit(CallExp e, Env env) { // New for funclang.
    Object result = e.operator().accept(this, env);
    if (!(result instanceof FunVal))
      return new DynamicError("Operator not a function in call " + ts.visit(e, env));
    FunVal operator = (FunVal) result; // Dynamic checking
    List<Exp> operands = e.operands();

    // Call-by-value semantics
    List<Value> actuals = new ArrayList<Value>(operands.size());
    for (Exp exp : operands)
      actuals.add((Value) exp.accept(this, env));

    List<String> formals = operator.formals();
    if (formals.size() != actuals.size())
      return new DynamicError("Argument mismatch in call " + ts.visit(e, env));

    Env fun_env = operator.env();
    for (int index = 0; index < formals.size(); index++)
      fun_env = new ExtendEnv(fun_env, formals.get(index), actuals.get(index));

    return (Value) operator.body().accept(this, fun_env);
  }

  @Override
  public Value visit(IfExp e, Env env) { // New for funclang.
    Object result = e.conditional().accept(this, env);
    if (!(result instanceof BoolVal))
      return new DynamicError("Condition not a boolean in expression " + ts.visit(e, env));
    BoolVal condition = (BoolVal) result; // Dynamic checking

    if (condition.v())
      return (Value) e.then_exp().accept(this, env);
    else
      return (Value) e.else_exp().accept(this, env);
  }

  @Override
  public Value visit(LessExp e, Env env) { // New for funclang.
    NumVal first = (NumVal) e.first_exp().accept(this, env);
    NumVal second = (NumVal) e.second_exp().accept(this, env);
    return new BoolVal(first.v() < second.v());
  }

  @Override
  public Value visit(EqualExp e, Env env) { // New for funclang.
    NumVal first = (NumVal) e.first_exp().accept(this, env);
    NumVal second = (NumVal) e.second_exp().accept(this, env);
    return new BoolVal(first.v() == second.v());
  }

  // String, Boolean, Number
  static boolean isSame(Value a, Value b) {
    if (a instanceof StringVal && b instanceof StringVal) {
      return ((StringVal) a).v().equals(((StringVal) b).v());
    } else if (a instanceof BoolVal && b instanceof BoolVal) {
      return ((BoolVal) a).v() == ((BoolVal) b).v();
    } else if (a instanceof NumVal && b instanceof NumVal) {
      return ((NumVal) a).v() == ((NumVal) b).v();
    } else if (a instanceof UnitVal && b instanceof UnitVal) {
      return true;
    } else if (a instanceof DynamicError && b instanceof DynamicError) {
      return true;
    } else if (a instanceof PairVal && b instanceof PairVal) {
      if (((PairVal) a).fst() instanceof StringVal && ((PairVal) b).fst() instanceof StringVal) {
        return ((StringVal) ((PairVal) a).fst()).v().equals(((StringVal) ((PairVal) b).fst()).v()) && isSame(((PairVal) a).snd(), ((PairVal) b).snd());
      } else if (((PairVal) a).fst() instanceof BoolVal && ((PairVal) b).fst() instanceof BoolVal) {
        return ((BoolVal) ((PairVal) a).fst()).v() == ((BoolVal) ((PairVal) b).fst()).v() && isSame(((PairVal) a).snd(), ((PairVal) b).snd());
      } else if (((PairVal) a).fst() instanceof NumVal && ((PairVal) b).fst() instanceof NumVal) {
        return ((NumVal) ((PairVal) a).fst()).v() == ((NumVal) ((PairVal) b).fst()).v() && isSame(((PairVal) a).snd(), ((PairVal) b).snd());
      } else if (((PairVal) a).fst() instanceof PairVal && ((PairVal) b).fst() instanceof PairVal) {
        return isSame(((PairVal) a).fst(), ((PairVal) b).fst()) && isSame(((PairVal) a).snd(), ((PairVal) b).snd());
      } else {
        return false;
      }
    } else {
      return a instanceof Null && b instanceof Null;
    }
  }

  @Override
  public Value visit(EqualQExp e, Env env) {
    Value first = (Value) e.first_exp().accept(this, env);
    Value second = (Value) e.second_exp().accept(this, env);
    boolean same = isSame(first, second);
    return new Value.BoolVal(same);
  }

  @Override
  public Value visit(GreaterExp e, Env env) { // New for funclang.
    NumVal first = (NumVal) e.first_exp().accept(this, env);
    NumVal second = (NumVal) e.second_exp().accept(this, env);
    return new BoolVal(first.v() > second.v());
  }

  @Override
  public Value visit(CarExp e, Env env) {
    PairVal pair = (PairVal) e.arg().accept(this, env);
    return pair.fst();
  }

  @Override
  public Value visit(CdrExp e, Env env) {
    PairVal pair = (PairVal) e.arg().accept(this, env);
    return pair.snd();
  }

  @Override
  public Value visit(ConsExp e, Env env) {
    Value first = (Value) e.fst().accept(this, env);
    Value second = (Value) e.snd().accept(this, env);
    return new PairVal(first, second);
  }

  @Override
  public Value visit(ListExp e, Env env) { // New for funclang.
    List<Exp> elemExps = e.elems();
    int length = elemExps.size();
    if (length == 0)
      return new Null();

    // Order of evaluation: left to right e.g. (list (+ 3 4) (+ 5 4))
    Value[] elems = new Value[length];
    for (int i = 0; i < length; i++)
      elems[i] = (Value) elemExps.get(i).accept(this, env);

    Value result = new Null();
    for (int i = length - 1; i >= 0; i--)
      result = new PairVal(elems[i], result);
    return result;
  }

  @Override
  public Value visit(NullExp e, Env env) {
    Value val = (Value) e.arg().accept(this, env);
    return new BoolVal(val instanceof Null);
  }

  @Override
  public Value visit(LengthStrExp e, Env env) {
    Object val = e.getStrExpr().accept(this, env);
    if (val instanceof StringVal) {
      String string = ((StringVal) val).v();
      return new NumVal(string.length() - 2); // - 2 to remove double quotes
    }
    return new DynamicError("Parameter for length was not a string.");
  }

  public Value visit(EvalExp e, Env env) {
    StringVal programText = (StringVal) e.code().accept(this, env);
    Program p = _reader.parse(programText.v());
    return (Value) p.accept(this, env);
  }

  public Value visit(ReadExp e, Env env) {
    StringVal fileName = (StringVal) e.file().accept(this, env);
    try {
      String text = Reader.readFile("" + System.getProperty("user.dir") + File.separator + fileName.v());
      return new StringVal(text);
    } catch (IOException ex) {
      return new DynamicError(ex.getMessage());
    }
  }

  private Env initialEnv() {
    GlobalEnv initEnv = new GlobalEnv();

    /*
     * Procedure: (read <filename>). Following is same as (define read (lambda
     * (file) (read file)))
     */
    List<String> formals = new ArrayList<>();
    formals.add("file");
    Exp body = new ReadExp(new VarExp("file"));
    FunVal readFun = new FunVal(initEnv, formals, body);
    initEnv.extend("read", readFun);

    /*
     * Procedure: (require <filename>). Following is same as (define require (lambda
     * (file) (eval (read file))))
     */
    formals = new ArrayList<>();
    formals.add("file");
    body = new EvalExp(new ReadExp(new VarExp("file")));
    FunVal requireFun = new FunVal(initEnv, formals, body);
    initEnv.extend("require", requireFun);

    /* Add new built-in procedures here */
    formals = new ArrayList<>();
    formals.add("str");
    body = new LengthStrExp(new VarExp("str"));
    FunVal lengthFun = new FunVal(initEnv, formals, body);
    initEnv.extend("length", lengthFun);

    return initEnv;
  }

  Reader _reader;

  public Evaluator(Reader reader) {
    _reader = reader;
  }
}
