package varlang;
import static varlang.AST.*;
import static varlang.Value.*;

import java.util.List;
import java.util.ArrayList;

import varlang.AST.AddExp;
import varlang.AST.NumExp;
import varlang.AST.DivExp;
import varlang.AST.MultExp;
import varlang.AST.Program;
import varlang.AST.SubExp;
import varlang.AST.VarExp;
import varlang.AST.Visitor;
import varlang.Env.EmptyEnv;
import varlang.Env.ExtendEnv;
import varlang.Env.GlobalEnv;

public class Evaluator implements Visitor<Value> {
	Env initEnv = initialEnv(); //New for definelang

	Value valueOf(Program p) {
			return (Value) p.accept(this, initEnv);
	}

	@Override
	public Value visit(AddExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 0;
		boolean en = false;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			if (intermediate.isEncrypted()) {
				en = true;
			}
			result += intermediate.v(); //Semantics of AddExp in terms of the target language.
		}
		NumVal val = new NumVal(result);
		val.setEncrypted(en);
		return val;
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
	public Value visit(DivExp e, Env env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v(); 
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result / rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(MultExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 1;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			result *= intermediate.v(); //Semantics of MultExp.
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(Program p, Env env) {
		try {
			for(DefineDecl d: p.decls())
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
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result - rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(VarExp e, Env env) {
		try {
			return env.getEncryptedValue(e.name());
		} catch (Env.LookupException err) {
			return env.get(e.name());
		}
	}

	@Override
	public Value visit(LetExp e, Env env) { // New for varlang.
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();
		List<Value> values = new ArrayList<Value>(value_exps.size());

		for(Exp exp : value_exps)
			values.add((Value)exp.accept(this, env));

		Env new_env = env;
		for (int i = 0; i < names.size(); i++)
			new_env = new ExtendEnv(new_env, names.get(i), values.get(i));

		return (Value) e.body().accept(this, new_env);
	}
	@Override
	public Value visit(DefineDecl e, Env env) { // New for definelang.
		String name = e.name();
		Exp value_exp = e.value_exp();
		Value value = (Value) value_exp.accept(this, env);
		((GlobalEnv) initEnv).extend(name, value);
		return new Value.UnitVal();
	}

	@Override
	public Value visit(LeteExp e, Env env) {
		double result = 0;
		for (int i = 0; i < e.names().size(); i++) {
			Exp name_exp = e.names().get(i);
			if (!(name_exp instanceof AST.IdExp)) {
				return new DynamicError("Error: Expected Identifier");
			}
			String name = ((IdExp) name_exp)._name;
			Exp value_exp = e._value_exps.get(i);
			Exp encrypted_exp = e._encrypted_value_exps.get(i);
			if (!(value_exp instanceof AST.NumExp) || !(encrypted_exp instanceof AST.NumExp)) {
				return new DynamicError("Error: Expected Number");
			} else {
				Value value = (Value) value_exp.accept(this, env);
				result = result + ((NumVal) value).v();
				env.setEncryptedValue(name, value);
			}
			Value value = (Value) encrypted_exp.accept(this, env);
			result = result + ((NumVal) value).v();
		}
		// fixme: `(lete ((x 1 20)) (dec 10 x))` has incorrect result now
		Value res = (Value) e.body().accept(this, env);
		if (res instanceof DynamicError) {
			return res;
		}
		if (res.isEncrypted()) {
			return res;
		} else {
			return new NumVal(result);
		}
	}

	@Override
	public Value visit(IdExp e, Env env) {
		return null;
	}

	@Override
	public Value visit(DecExp e, Env env) {
		List<Exp> list = e.all();
		if (list.size() != 2) {
			return new DynamicError("Only accept 2 operands");
		}
		if (!(list.get(0) instanceof AST.NumExp)) {
			return new DynamicError("Error: Expected Number");
		}
		if (!(list.get(1) instanceof AST.VarExp)) {
			return new DynamicError("Error: Expected Inf");
		}
		Value value = env.getEncryptedValue(((AST.VarExp)list.get(1)).name());
		value.setEncrypted(true);
		return value;
	}

	private Env initialEnv() {
		GlobalEnv initEnv = new GlobalEnv();
		return initEnv;
	}

	@Override
	public Value visit(ModExp e, Env env) {
		List<Exp> operands = e.all();

		List<Integer> values = new ArrayList<>();
		for (int i = 0; i < operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			if (i > 0) {
				if (rVal.v() == 0 || rVal.v() % 1 > 0) {
					return new DynamicError("Zero or float operator for mod is not permitted on RHS.");
				}
			} else {
				if (rVal.v() % 1 > 0) {
					return new DynamicError("Float operator for mod is not permitted. ");
				}
			}
			values.add((int) rVal.v());
		}

		int result = values.get(0);
		for (int i = 1; i < values.size(); i++) {
			result = mod(result, values.get(i));
		}

		return new NumVal(result);
	}

	private int mod(int a, int b) {

		return a % b;

	}
}
