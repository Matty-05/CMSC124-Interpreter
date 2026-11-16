unit first = void
unit second = void
for (unit i = 0, i < 2, i = i + 1) {
    protocol read() { transmit i }
    if (i == 0) first = read else second = read
}
report first() # expect: 2
report second() # expect: 2
