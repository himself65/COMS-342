(let 
    ((x (ref 342))) 
    (   
        (lambda (x y) 
            (deref x)
        ) 
        x (free x)
    )
)