if (active) {
    if (inactive) {
        report 1 / 0
    } else {
        report 2 # expect: 2
    }
} else {
    report 1 / 0
}
