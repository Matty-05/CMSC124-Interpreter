unit total = 0
unit k = 1
while (k <= 4) {
    total = total + k
    k = k + 1
}
report total  # expect: 10
