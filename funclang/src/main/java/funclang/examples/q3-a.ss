(define total (lambda (lst)
                (if (null? lst)
                  0
                  (+ (length (car lst)) (total (cdr lst))))
                ))
