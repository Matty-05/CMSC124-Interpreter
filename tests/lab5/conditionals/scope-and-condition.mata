unit hp = 10
if ((hp = hp - 1) > 0) {
    unit hp = 2
    report hp # expect: 2
}
report hp # expect: 9
