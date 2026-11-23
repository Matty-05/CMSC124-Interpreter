unit i = 0
while (i < 1) {
    unit inner = 5
    i = i + 1
}
report inner  # expect runtime error: Undefined variable 'inner'.
