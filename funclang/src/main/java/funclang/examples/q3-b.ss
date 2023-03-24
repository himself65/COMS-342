(define longest
  (lambda (lst)
    (if (null? lst)
      0
      (if
        (> (length (car lst)) (longest (cdr lst)))
        (length (car lst))
        (longest (cdr lst))
        )
      )
    )
  )
