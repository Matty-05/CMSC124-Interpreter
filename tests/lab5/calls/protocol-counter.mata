protocol makeCounter() {
    unit count = 0
    protocol increment() {
        count = count + 1
        transmit count
    }
    transmit increment
}
unit counter = makeCounter()
report counter() # expect: 1
report counter() # expect: 2
report counter() # expect: 3
unit other = makeCounter()
report other() # expect: 1
report counter() # expect: 4
