(define find : (List<num> num -> num) (lambda (lst: List<num> n: num)
               (if (null? lst)
                 -1
                 (if (= (car lst) n)
                   0
                   (+ 1 (find (cdr lst) n))))))

(define next-not : (List<num> num -> num) (lambda (lst: List<num> x: num)
                                            (if (null? lst)
                                              0
                                              (if (= (car lst) x)
                                                (+ 1 (next-not (cdr lst) x))
                                                0)))
  )

(define remove-before : (List<num> num -> List<num>) (lambda (lst: List<num> pos: num)
                                                       (if (null? lst)
                                                         (list : num)
                                                         (if (= pos 0)
                                                           lst
                                                           (remove-before (cdr lst) (- pos 1))))
                                                       ))

(define append : (List<num> List<num> -> List<num>) (lambda (lst1: List<num> lst2: List<num>) (if (null? lst1) lst2 (if (null? lst2) lst1 (cons (car lst1) (append (cdr lst1) lst2))))))

(define compression : (List<num> -> List<num>) (lambda (lst: List<num>)
                                                 (if (null? lst)
                                                   (list : num)
                                                   (if (null? (cdr lst))
                                                     lst
                                                     (if (= (car lst) (car (cdr lst)))
                                                       (append (list : num (car lst))
                                                         (compression
                                                           (remove-before
                                                             (cdr lst)
                                                             (next-not
                                                               (cdr lst)
                                                               (car lst)
                                                               )
                                                             )
                                                           )
                                                         )
                                                       (append (list : num (car lst)) (compression (cdr lst))))))
                                                 )
  )



(> 1 2) // bool
(> 1 1) // bool
(> 1 0) // bool
(> 1 #t) // Type error: The two expressions should have the same type (> 1.0 #t)
(> 1 "hello") // Type error: The two expressions should have the same type (> 1.0 "hello")
// similar as <, =

(null? 1) // Type error: The null? expects an expression of type List, found num in (null? 1.0)
(null? #t) // Type error: The null? expects an expression of type List, found bool in (null? #t)
(null? "hello") // Type error: The null? expects an expression of type List, found string in (null? "hello")
(null? (list : num 1 2 3)) // bool
(null? (list : num)) // bool

(car 1) // Type error: The car expect an expression of type Pair, foundstring in (car 1)
(car #t) // Type error: The car expect an expression of type Pair, foundstring in (car #t)
(car "hello") // Type error: The car expect an expression of type Pair, foundstring in (car "hello")
(car (list : num 1 2 3)) // num

(cdr 1) // Type error: The car expect an expression of type Pair, foundstring in (cdr 1)
(cdr #t) // Type error: The car expect an expression of type Pair, foundstring in (cdr #t)
(cdr "hello") // Type error: The car expect an expression of type Pair, foundstring in (cdr "hello")
(cdr (list : num 1 2 3)) // List<num>

(ref : num 1) // (Ref num)
(ref : bool #t) // (Ref bool)
(ref : (num -> num) (lambda (x: num) x)) // (Ref (num -> num))
(ref : num #t) // Type error: The reference expression expect a reference type num found bool in (ref #t)
(ref : num "hello") // Type error: The reference expression expect a reference type num found string in (ref "hello")

(deref (ref: bool #t)) // bool
(deref (ref: bool 45)) // Type error: The reference expression expect a reference type bool found num in (ref 45.0)
(deref (ref: (num -> num) (lambda (x: num) x))) // (num -> num)