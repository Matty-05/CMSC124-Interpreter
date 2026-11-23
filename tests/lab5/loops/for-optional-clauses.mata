# every clause can be left out; here only the condition is given
unit i = 0
for (, i < 3,) {
    report i  # expect: 0
              # expect: 1
              # expect: 2
    i = i + 1
}
# an initializer can be a plain expression that reuses an outer unit
for (i = 10, i < 12, i = i + 1) report i  # expect: 10
                                          # expect: 11
report i  # expect: 12
