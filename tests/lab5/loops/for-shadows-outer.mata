# the loop unit lives in its own scope, so the outer i is untouched
unit i = "outer"
for (unit i = 0, i < 2, i = i + 1) report i  # expect: 0
                                             # expect: 1
report i  # expect: outer
