for (unit i = 0, i < 2, i = i + 1) {
    for (unit j = 0, j < 2, j = j + 1) {
        report i * 10 + j  # expect: 0
                           # expect: 1
                           # expect: 10
                           # expect: 11
    }
}
