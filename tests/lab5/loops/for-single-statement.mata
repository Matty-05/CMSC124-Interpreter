unit total = 0
for (unit i = 1, i < 5, i = i + 1) total = total + i
report total  # expect: 10
