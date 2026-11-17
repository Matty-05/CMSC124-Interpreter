unit x = 100
protocol update(x) {
    x = x + 1
    transmit x
}
report update(5) # expect: 6
report update(5) # expect: 6
report x # expect: 100
