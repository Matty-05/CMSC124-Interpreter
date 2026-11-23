unit step = 10
unit i = 0
while (i < 2) {
    unit step = step + 1
    report step  # expect: 11
                 # expect: 11
    i = i + 1
}
report step  # expect: 10
