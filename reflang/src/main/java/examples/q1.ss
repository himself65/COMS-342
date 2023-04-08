(define loc1 (ref 12))
(define loc2 (ref (+ 1 2)))
(define loc3 (ref 3))

(define pairNode (lambda (fst snd) (lambda (op) (if op fst snd))))
(define node (lambda (x) (pairNode x (ref (list)))))
(define getFst (lambda (p) (p #t)))
(define getSnd (lambda (p) (p #f)))

(define find
  (lambda (hd x)
    (if (= (getFst hd) x)
      hd
      (if (pair? (getSnd hd))
        (find (getSnd hd) x)
        (if (null? (deref (getSnd hd)))
          (list)
          (find (car (deref (getSnd hd))) x)
          )
        )
      )))

(define insert
  (lambda (lst x n)
    (if (null? (find lst x))
      (lst)
      (if (= (getFst lst) x)
        (set! (getSnd lst) n)
        (insert (getSnd lst) x n)
        )
      )
    )
  )
