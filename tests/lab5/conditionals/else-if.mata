if (inactive) {
    report 1 / 0
} else if (active) {
    report 2 # expect: 2
} else {
    report 1 / 0
}
