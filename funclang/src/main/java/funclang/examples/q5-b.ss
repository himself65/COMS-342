(define find (lambda (lst n)
               (if (null? lst)
                 -1
                 (if (= (car lst) n)
                   0
                   (+ 1 (find (cdr lst) n))))))

(define next-not (lambda (lst x)
                   (if (null? lst)
                     0
                     (if (equal? (car lst) x)
                       (+ 1 (next-not (cdr lst) x))
                       0)))
  )

(define remove-before (lambda (lst pos)
                        (if (null? lst)
                          (list)
                          (if (= pos 0)
                            lst
                            (remove-before (cdr lst) (- pos 1))))
                        ))
(define append (lambda (lst1 lst2) (if (null? lst1) lst2 (if (null? lst2) lst1 (cons (car lst1) (append (cdr lst1) lst2))))))

(define compression (lambda (lst)
                      (if (null? lst)
                        (list)
                        (if (null? (cdr lst))
                          lst
                          (if (equal? (car lst) (car (cdr lst)))
                            (append (list (car lst))
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
                            (append (list (car lst)) (compression (cdr lst))))))
                      )
  )