protocol outer() {
    protocol inner() { transmit 7 }
    report inner() # expect: 7
    transmit 9
}
report outer() # expect: 9
