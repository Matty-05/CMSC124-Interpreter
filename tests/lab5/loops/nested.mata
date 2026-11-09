unit row = 0
while (row < 2) {
    unit col = 0
    while (col < 2) {
        report row * 10 + col  # expect: 0
                               # expect: 1
                               # expect: 10
                               # expect: 11
        col = col + 1
    }
    row = row + 1
}
