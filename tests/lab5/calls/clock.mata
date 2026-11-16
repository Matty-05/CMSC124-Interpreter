clock()
report clock() > 0  # expect: active
report clock() <= clock()  # expect: active
report clock  # expect: <native clock>
unit now = clock
report now() > 0  # expect: active
