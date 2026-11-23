protocol add(a, b, c) { transmit a + b + c }
report add(1, 2, 3) # expect: 6
unit total = add
report total(4, 5, 6) # expect: 15
