protocol even(n) {
    if (n == 0) transmit active
    transmit odd(n - 1)
}
protocol odd(n) {
    if (n == 0) transmit inactive
    transmit even(n - 1)
}
report even(6) # expect: active
report odd(6) # expect: inactive
