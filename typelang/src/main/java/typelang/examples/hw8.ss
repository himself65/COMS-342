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
