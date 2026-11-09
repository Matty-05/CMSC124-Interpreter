unit i = 5
while (i < 3) {
    report "never"
}
report "skipped"  # expect: skipped
