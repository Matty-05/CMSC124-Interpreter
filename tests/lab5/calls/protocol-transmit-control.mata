protocol find() {
    unit i = 0
    while (i < 3) {
        if (i == 1) {
            transmit i + 40
        }
        i = i + 1
    }
    transmit 99
}
report find() # expect: 41
report "continued" # expect: continued
