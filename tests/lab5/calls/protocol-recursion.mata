protocol fib(n) {
    if (n <= 1) transmit n
    transmit fib(n - 1) + fib(n - 2)
}
report fib(0) # expect: 0
report fib(10) # expect: 55
