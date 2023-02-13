grammar VarLang;

import VarLangBasic; //Import all rules from VarLang grammar.
 
 // New elements in the Grammar of this Programming Language
 //  - grammar rules start with lowercase

// We are redefining programs to be zero or more define declarations 
// followed by an optional expression.
 program returns [Program ast]        
 		locals [ArrayList<DefineDecl> defs, Exp expr]
 		@init { $defs = new ArrayList<DefineDecl>(); $expr = new UnitExp(); } :
		// (def=definedecl { $defs.add($def.ast); } )*
		(def=definedecl { $defs.add($def.ast); } )*
        (e=exp { $expr = $e.ast; } )? 
		{ $ast = new Program($defs, $expr); }
		;


 exp returns [Exp ast]: 
		v=varexp { $ast = $v.ast; }
		| n=numexp { $ast = $n.ast; }
        | a=addexp { $ast = $a.ast; }
        | s=subexp { $ast = $s.ast; }
        | m=multexp { $ast = $m.ast; }
        | d=divexp { $ast = $d.ast; }
        | l=letexp { $ast = $l.ast; }
        | md=modexp { $ast = $md.ast; }
        | le=leteexp { $ast = $le.ast; }
        | de=decexp { $ast = $de.ast; }
        ;

 expp returns [Exp ast]:
        n=numexp { $ast = $n.ast; }
        | id=idexp { $ast = $id.ast; }
        ;

// This is the new define macro grammar.


// (define (macro_name argument1, argument2, ...) expression)


// Example 1

// (define (square x) (* x x))
// (square 2)

// Example 2

// (define (pressure n R T V) (/ (* n R T) V))
// (pressure 2 8.3146 273 0.0224)

 definedecl returns [DefineDecl ast] :
 		'(' Define 
 			id=Identifier
 			e=exp
 			')' { $ast = new DefineDecl($id.text, $e.ast); }
    ;

 modexp returns [ModExp ast]
        locals [ArrayList<Exp> list= new ArrayList<Exp>()]:
        '(' '%'
            e=exp { $list.add($e.ast); }
          		    ( e=exp { $list.add($e.ast); } )+
         ')' { $ast = new ModExp($list); }
    ;

 leteexp returns [LeteExp ast]
        locals [ArrayList<Exp> names = new ArrayList<Exp>(), ArrayList<Exp> value_exps = new ArrayList<Exp>(), ArrayList<Exp> encrypted_value_exps = new ArrayList<Exp>() ] :
 		'(' Lete
 			'(' ( '(' id=expp e=exp de=exp ')' { $names.add($id.ast); $value_exps.add($e.ast); $encrypted_value_exps.add($de.ast); } )+  ')'
 			body=exp
 			')' { $ast = new LeteExp($names, $value_exps, $encrypted_value_exps, $body.ast); }
 		;

 decexp returns [DecExp ast]
        locals [ArrayList<Exp> list= new ArrayList<Exp>()]:
        '(' 'dec'
            e=exp { $list.add($e.ast); }
          		    ( e=exp { $list.add($e.ast); } )+
         ')' { $ast = new DecExp($list); }
    ;

 idexp returns [IdExp ast]:
        id=Identifier { $ast = new IdExp($id.text); }
    ;
