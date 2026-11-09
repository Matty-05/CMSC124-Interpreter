unit i = 0
while (i < 3) {
    report i  # expect: 0
              # expect: 1
              # expect: 2
    i = i + 1
}
report i  # expect: 3
