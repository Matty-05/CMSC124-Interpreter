unit i = 0
unit last = void
while (i < 2) {
    unit step = i * 10
    last = step
    i = i + 1
}
report last  # expect: 10
