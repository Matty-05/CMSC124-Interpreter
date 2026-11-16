protocol first() {
    for (unit i = 0, i < 3, i = i + 1) {
        if (i == 1) transmit i
    }
}
report first() # expect: 1
